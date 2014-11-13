/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.apache.shiro.authz.AuthorizationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;

import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Optional;
import com.google.common.cache.Cache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.position.PositionOrTrade;
import com.opengamma.core.security.Security;
import com.opengamma.service.ServiceContext;
import com.opengamma.service.ThreadLocalServiceContext;
import com.opengamma.sesame.Environment;
import com.opengamma.sesame.cache.CacheInvalidator;
import com.opengamma.sesame.cache.CacheProvider;
import com.opengamma.sesame.cache.CachingProxyDecorator;
import com.opengamma.sesame.cache.ExecutingMethodsThreadLocal;
import com.opengamma.sesame.cache.MethodInvocationKey;
import com.opengamma.sesame.config.FunctionArguments;
import com.opengamma.sesame.config.FunctionModelConfig;
import com.opengamma.sesame.config.NonPortfolioOutput;
import com.opengamma.sesame.config.ViewColumn;
import com.opengamma.sesame.config.ViewConfig;
import com.opengamma.sesame.function.AvailableImplementations;
import com.opengamma.sesame.function.AvailableOutputs;
import com.opengamma.sesame.function.InvalidInputFunction;
import com.opengamma.sesame.function.InvokableFunction;
import com.opengamma.sesame.function.PermissionDeniedFunction;
import com.opengamma.sesame.function.scenarios.FilteredScenarioDefinition;
import com.opengamma.sesame.function.scenarios.ScenarioDefinition;
import com.opengamma.sesame.graph.CompositeNodeDecorator;
import com.opengamma.sesame.graph.FunctionBuilder;
import com.opengamma.sesame.graph.FunctionModel;
import com.opengamma.sesame.graph.Graph;
import com.opengamma.sesame.graph.GraphBuilder;
import com.opengamma.sesame.graph.GraphModel;
import com.opengamma.sesame.graph.NodeDecorator;
import com.opengamma.sesame.marketdata.CycleMarketDataFactory;
import com.opengamma.sesame.proxy.ExceptionWrappingProxy;
import com.opengamma.sesame.proxy.MetricsProxy;
import com.opengamma.sesame.trace.CallGraph;
import com.opengamma.sesame.trace.Tracer;
import com.opengamma.sesame.trace.TracingProxy;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.result.Result;

/**
 * View is the main class for running calculations over a portfolio and producing results.
 */
public class View {

  private static final Logger s_logger = LoggerFactory.getLogger(View.class);

  private final Graph _graph;
  private final ViewConfig _viewConfig;
  private final ExecutorService _executor;
  private final FunctionModelConfig _systemDefaultConfig;
  private final List<String> _columnNames;
  private final GraphModel _graphModel;

  /**
   * Provider that supplies a cache to this view. A new cache is requested at the start of each calculation
   * cycle and used for the duration of the cycle.
   */
  private final CacheProvider _factoryCacheProvider;

  private final Optional<MetricRegistry> _metricRegistry;
  private final ComponentMap _componentMap;
  private final CacheInvalidator _cacheInvalidator;

  /**
   * Thread local variable used to expose the cache for the current cycle to the caching proxy.
   * In order to ensure consistency in the calculations, the same cache must be used for the whole
   * of a calculation cycle. Therefore the caching proxy must have a way to get hold of the correct
   * cache when it is handling a method call. There is no way to communicate an ID for the cycle
   * between the view and the proxy, so the cache is stored in a thread local which is used by
   * the cache provider.
   */
  private final ThreadLocal<Cache<MethodInvocationKey, Object>> _cacheThreadLocal = new ThreadLocal<>();

  View(ViewConfig viewConfig,
       ExecutorService executor,
       FunctionModelConfig systemDefaultConfig,
       FunctionBuilder functionBuilder,
       EnumSet<FunctionService> services,
       ComponentMap componentMap,
       Set<Class<?>> inputTypes,
       AvailableOutputs availableOutputs,
       AvailableImplementations availableImplementations,
       CacheProvider factoryCacheProvider,
       CacheInvalidator cacheInvalidator,
       Optional<MetricRegistry> metricRegistry) {

    _cacheInvalidator = ArgumentChecker.notNull(cacheInvalidator, "cacheInvalidator");
    _componentMap = ArgumentChecker.notNull(componentMap, "componentMap");
    _viewConfig = ArgumentChecker.notNull(viewConfig, "viewConfig");
    _executor = ArgumentChecker.notNull(executor, "executor");
    _systemDefaultConfig = ArgumentChecker.notNull(systemDefaultConfig, "systemDefaultConfig");
    _factoryCacheProvider = ArgumentChecker.notNull(factoryCacheProvider, "factoryCacheProvider");
    _metricRegistry = ArgumentChecker.notNull(metricRegistry, "metricRegistry");
    _columnNames = columnNames(_viewConfig);

    ExecutingMethodsThreadLocal executingMethods = new ExecutingMethodsThreadLocal();

    // Provider that supplies the _cache field to the caching decorators
    // the field is updated with a cache from _factoryCacheProvider at the start of each cycle
    CacheProvider cacheProvider = new CacheProvider() {
      @Override
      public Cache<MethodInvocationKey, Object> get() {
        return _cacheThreadLocal.get();
      }
    };
    NodeDecorator decorator = createNodeDecorator(services, cacheProvider, executingMethods);

    s_logger.debug("building graph model");
    GraphBuilder graphBuilder = new GraphBuilder(availableOutputs,
                                                 availableImplementations,
                                                 componentMap.getComponentTypes(),
                                                 systemDefaultConfig,
                                                 decorator);
    _graphModel = graphBuilder.build(viewConfig, inputTypes);
    s_logger.debug("graph model complete, building graph");
    _graph = _graphModel.build(componentMap, functionBuilder);
    s_logger.debug("graph complete");
  }

  private NodeDecorator createNodeDecorator(EnumSet<FunctionService> services,
                                            CacheProvider cacheProvider,
                                            ExecutingMethodsThreadLocal executingMethods) {


    ImmutableList.Builder<NodeDecorator> decorators = new ImmutableList.Builder<>();

    // Build up the proxies to be used from the outermost
    // to the innermost

    // Timing/tracing sits outside of caching so the actual
    // time taken for a request is reported. This can also
    // report on whether came from the cache or were
    // calculated (if cache there will be no child calls).
    // Only allow one tracing proxy but pick the most
    // comprehensive one
    if (services.contains(FunctionService.TRACING)) {
      decorators.add(TracingProxy.INSTANCE);
    }

    // Caching proxy memoizes requests as required so that
    // expensive calculations are not performed more
    // frequently than they need to be
    if (services.contains(FunctionService.CACHING)) {
      decorators.add(new CachingProxyDecorator(cacheProvider, executingMethods));
    }

    // Metrics records time taken to execute each function. This
    // sits inside the caching layer as we're interested in how
    // long the actual calculation takes not how long it takes to
    // get from the cache
    if (services.contains(FunctionService.METRICS)) {
      if (_metricRegistry.isPresent()) {
        decorators.add(new MetricsProxy(_metricRegistry.get()));
      } else {
        // This should be prevented by the ViewFactoryComponentFactory but is
        // here in case of programmatic misconfiguration
        s_logger.warn("Unable to create metrics proxy as no metrics repository has been configured");
      }
    }

    // Ensure we always have the exception wrapping behaviour so
    // methods returning Result<?> return Failure if an exception
    // is thrown internally.
    decorators.add(ExceptionWrappingProxy.INSTANCE);
    return CompositeNodeDecorator.compose(decorators.build());
  }

  /**
   * Runs a single calculation cycle, blocking until the results are available.
   *
   * @param cycleArguments settings for running the cycle including valuation time and market data source
   * @return the calculation results
   */
  public Results run(CycleArguments cycleArguments) {
    return run(cycleArguments, Collections.emptyList());
  }

  /**
   * Runs a single calculation cycle, blocking until the results are available.
   *
   * @param cycleArguments settings for running the cycle including valuation time and market data source
   * @param inputs the inputs to the calculation, e.g. trades, positions, securities
   * @return the calculation results
   */
  public Results run(CycleArguments cycleArguments, List<?> inputs) {
    Instant start = Instant.now();
    long startInitialization = System.nanoTime();
    long startExecution;
    long startResultsBuild;

    // get a cache from the factory that will be used for the duration of this calculation cycle.
    // store it in a field so it's available to the caching decorators via the provider
    Cache<MethodInvocationKey, Object> cache = _factoryCacheProvider.get();
    ThreadLocalCache threadLocalCache = new ThreadLocalCache(_cacheThreadLocal, cache);

    ServiceContext originalContext = ThreadLocalServiceContext.getInstance();
    CycleInitializer cycleInitializer = cycleArguments.isCaptureInputs() ?
        // this uses the shared cache but creates a new graph with a new FunctionBuilder and therefore will never
        // share any entries with any other views. effectively this fills up the shared cache with rubbish entries
        // that will never be used after this cycle completes.
        // if that turns out to be a problem the cache builder could be passed into this class and used to create
        // an empty cache that's only used for this cycle.
        // would need to create a new graph builder and model instead of reusing _graphModel
        new CapturingCycleInitializer(originalContext, _componentMap, cycleArguments,
                                      _graphModel, _viewConfig, inputs) :
        new StandardCycleInitializer(originalContext, cycleArguments.getCycleMarketDataFactory(), _graph);

    List<Task> tasks = new ArrayList<>();
    Graph graph = cycleInitializer.getGraph();
    CycleMarketDataFactory marketDataFactory = cycleInitializer.getCycleMarketDataFactory();
    ScenarioDefinition scenario = _viewConfig.getScenarioDefinition();
    tasks.addAll(portfolioTasks(marketDataFactory, cycleArguments, inputs, graph, scenario, threadLocalCache));
    tasks.addAll(nonPortfolioTasks(marketDataFactory, cycleArguments, graph, scenario, threadLocalCache));
    List<Future<TaskResult>> futures;

    try {
      // Create a new version of the context with our wrapped components
      // Using the with method means we don't need to provide other
      // items e.g. VersionCorrectionProvider
      // TODO - ultimately we will want to set VersionCorrection here
      ThreadLocalServiceContext.init(cycleInitializer.getServiceContext());
      startExecution = System.nanoTime();
      futures = _executor.invokeAll(tasks);
    } catch (InterruptedException e) {
      throw new OpenGammaRuntimeException("Interrupted", e);
    } finally {
      startResultsBuild = System.nanoTime();
      // Switch the service context back now all the work is done
      ThreadLocalServiceContext.init(originalContext);
    }

    ResultBuilder resultsBuilder = Results.builder(inputs, _columnNames);
    for (Future<TaskResult> future : futures) {
      try {
        TaskResult result = future.get();
        result.addToResults(resultsBuilder);
      } catch (InterruptedException | ExecutionException e) {
        s_logger.warn("Failed to get result from task", e);
      }
    }
    return cycleInitializer.complete(
        resultsBuilder.build(start, startExecution, startInitialization, startResultsBuild));
  }

  /**
   * Returns the {@link FunctionModel} of the function used to calculate the value in a column.
   * @param columnName the name of the column
   * @param inputType type of input (i.e. the security, trade or position type) for the row
   * @return the function model or null if there isn't one for the specified input type
   * @throws IllegalArgumentException if the column name isn't found
   */
  public FunctionModel getFunctionModel(String columnName, Class<?> inputType) {
    return _graphModel.getFunctionModel(columnName, inputType);
  }

  /**
   * Returns the {@link FunctionModel} of the function used to calculate a non-portfolio output.
   * @param outputName The name of the output
   * @return the function model
   * @throws IllegalArgumentException if the output name isn't found
   */
  public FunctionModel getFunctionModel(String outputName) {
    return _graphModel.getFunctionModel(outputName);
  }

  private List<Task> portfolioTasks(CycleMarketDataFactory marketDataFactory,
                                    CycleArguments cycleArguments,
                                    List<?> inputs,
                                    Graph graph,
                                    ScenarioDefinition scenarioDefinition,
                                    ThreadLocalCache cache) {
    // create tasks for the portfolio outputs
    int colIndex = 0;
    List<Task> portfolioTasks = Lists.newArrayList();
    for (ViewColumn column : _viewConfig.getColumns()) {
      FilteredScenarioDefinition filteredDef = scenarioDefinition.filter(column.getName());
      Environment env =
          new EngineEnvironment(cycleArguments.getValuationTime(column.getName()), marketDataFactory, _cacheInvalidator);
      Environment columnEnv = env.withScenarioDefinition(filteredDef);
      Map<Class<?>, InvokableFunction> functions = graph.getFunctionsForColumn(column.getName());

      int rowIndex = 0;
      for (Object input : inputs) {
        // the function that is determined from the input
        InvokableFunction function;
        // the input to the function that is determined, which can be a security when the input is a position or trade
        Object functionInput;
        // try the type of the input
        InvokableFunction inputFunction = functions.get(input.getClass());
        if (inputFunction != null) {
          function = inputFunction;
          functionInput = input;
        } else if (input instanceof PositionOrTrade) {
          // extract the security from the position or trade
          try {
            Security security = ((PositionOrTrade) input).getSecurity();
            if (security == null) {
              function = new InvalidInputFunction(
                  "Position or trade does not contain a security, column: " + column + " type: " + input.getClass().getName());
              functionInput = input;
            } else {
              function = functions.get(security.getClass());
              if (function == null) {
                function = new InvalidInputFunction(
                    "No function found for security, column: " + column + " type: " + input.getClass().getName());
              }
              functionInput = security;
            }
          } catch (AuthorizationException ex) {
            function = new PermissionDeniedFunction(ex.getMessage());
            functionInput = input;
          }
        } else {
          // input is not known by the configuration
          function = new InvalidInputFunction(
              "No function found for input, column: " + column + " type: " + input.getClass().getName());
          functionInput = input;
        }
        Tracer tracer = Tracer.create(cycleArguments.traceType(rowIndex, colIndex));

        FunctionModelConfig columnConfig = column.getFunctionConfig(functionInput.getClass());
        FunctionModelConfig functionModelConfig =
            columnConfig.mergedWith(_viewConfig.getDefaultConfig(), _systemDefaultConfig);

        Class<?> implType = function.getUnderlyingReceiver().getClass();
        Class<?> declaringType = function.getDeclaringClass();
        FunctionArguments args =
            cycleArguments.getFunctionArguments().mergedWith(functionModelConfig.getFunctionArguments(implType),
                                                             functionModelConfig.getFunctionArguments(declaringType));
        portfolioTasks.add(new PortfolioTask(columnEnv, functionInput, args, rowIndex++,
                                             colIndex, function, tracer, cache));
      }
      colIndex++;
    }
    return portfolioTasks;
  }

  // create tasks for the non-portfolio outputs
  private List<Task> nonPortfolioTasks(CycleMarketDataFactory marketDataFactory,
                                       CycleArguments cycleArguments,
                                       Graph graph,
                                       ScenarioDefinition scenarioDefinition,
                                       ThreadLocalCache cache) {
    List<Task> tasks = Lists.newArrayList();
    for (NonPortfolioOutput output : _viewConfig.getNonPortfolioOutputs()) {
      InvokableFunction function = graph.getNonPortfolioFunction(output.getName());
      Tracer tracer = Tracer.create(cycleArguments.traceType(output.getName()));

      FunctionModelConfig outputConfig = output.getOutput().getFunctionModelConfig();
      FunctionModelConfig functionModelConfig =
          outputConfig.mergedWith(_viewConfig.getDefaultConfig(), _systemDefaultConfig);

      Class<?> implType = function.getUnderlyingReceiver().getClass();
      Class<?> declaringType = function.getDeclaringClass();
      FunctionArguments args =
          cycleArguments.getFunctionArguments().mergedWith(functionModelConfig.getFunctionArguments(implType),
                                                           functionModelConfig.getFunctionArguments(declaringType));
      // create an environment with scenario arguments filtered for the output
      FilteredScenarioDefinition filteredDef = scenarioDefinition.filter(output.getName());
      Environment env =
          new EngineEnvironment(cycleArguments.getValuationTime(output.getName()), marketDataFactory, _cacheInvalidator);
      Environment outputEnv = env.withScenarioDefinition(filteredDef);
      tasks.add(new NonPortfolioTask(outputEnv, args, output.getName(), function, tracer, cache));
    } return tasks;
  }

  private static List<String> columnNames(ViewConfig viewConfig) {
    List<String> columnNames = Lists.newArrayListWithCapacity(viewConfig.getColumns().size());
    for (ViewColumn column : viewConfig.getColumns()) {
      String columnName = column.getName();
      columnNames.add(columnName);
    }
    return columnNames;
  }

  // TODO run() variants that take:
  //   1) cycle and listener (for multiple execution)
  //   2) listener (for single async or infinite execution)
  //   3) new list of inputs

  //----------------------------------------------------------
  private interface TaskResult {

    void addToResults(ResultBuilder resultBuilder);
  }

  //----------------------------------------------------------
  private abstract static class Task implements Callable<TaskResult> {

    private final Environment _env;
    private final Object _input;
    private final InvokableFunction _invokableFunction;
    private final Tracer _tracer;
    private final FunctionArguments _args;
    private final ThreadLocalCache _cache;

    private Task(Environment env,
                 Object input,
                 FunctionArguments args,
                 InvokableFunction invokableFunction,
                 Tracer tracer,
                 ThreadLocalCache cache) {
      _env = env;
      _input = input;
      _args = args;
      _invokableFunction = invokableFunction;
      _tracer = tracer;
      _cache = cache;
    }

    @Override
    public TaskResult call() throws Exception {
      TracingProxy.start(_tracer);
      Result<?> result = invokeFunction();
      CallGraph callGraph = TracingProxy.end();
      return createResult(result, callGraph);
    }

    private Result<?> invokeFunction() {
      // try-with-resources requires the declaration of variable even if it's not used in the body of the block
      try (ThreadLocalCache ignore = _cache.set()) {
        Object retVal = _invokableFunction.invoke(_env, _input, _args);
        return retVal instanceof Result ? (Result<?>) retVal : Result.success(retVal);
      } catch (Exception e) {
        s_logger.warn("Failed to execute function", e);
        return Result.failure(e);
      }
    }

    protected abstract TaskResult createResult(Result<?> result, CallGraph callGraph);
  }

  //----------------------------------------------------------
  private static final class PortfolioTask extends Task {

    private final int _rowIndex;
    private final int _columnIndex;

    private PortfolioTask(Environment env,
                          Object input,
                          FunctionArguments args,
                          int rowIndex,
                          int columnIndex,
                          InvokableFunction invokableFunction,
                          Tracer tracer,
                          ThreadLocalCache cache) {
      super(env, input, args, invokableFunction, tracer, cache);
      _rowIndex = rowIndex;
      _columnIndex = columnIndex;
    }

    @Override
    protected TaskResult createResult(final Result<?> result, final CallGraph callGraph) {
      return new TaskResult() {
        @Override
        public void addToResults(ResultBuilder resultBuilder) {
          resultBuilder.add(_rowIndex, _columnIndex, result, callGraph);
        }
      };
    }
  }

  //----------------------------------------------------------
  private static final class NonPortfolioTask extends Task {

    private final String _outputValueName;

    private NonPortfolioTask(Environment env,
                             FunctionArguments args,
                             String outputValueName,
                             InvokableFunction invokableFunction,
                             Tracer tracer,
                             ThreadLocalCache cache) {
      super(env, null, args, invokableFunction, tracer, cache);
      _outputValueName = ArgumentChecker.notEmpty(outputValueName, "outputValueName");
    }

    @Override
    protected TaskResult createResult(final Result<?> result, final CallGraph callGraph) {
      return new TaskResult() {
        @Override
        public void addToResults(ResultBuilder resultBuilder) {
          resultBuilder.add(_outputValueName, result, callGraph);
        }
      };
    }
  }

  /**
   * Auto-closable wrapper around a cache and a thread local variable that holds it.
   * <p>
   * When {@link #set()} is called the cache is assigned to the thread local value and when {@link #close()}
   * is called it is removed. The intention is that tasks which execute functions should set the cache in a
   * try-with-resources block and execute the function in the body of the block.
   */
  private static class ThreadLocalCache implements AutoCloseable {

    private final ThreadLocal<Cache<MethodInvocationKey, Object>> _threadLocal;
    private final Cache<MethodInvocationKey, Object> _cache;

    private ThreadLocalCache(ThreadLocal<Cache<MethodInvocationKey, Object>> threadLocal,
                             Cache<MethodInvocationKey, Object> cache) {
      _threadLocal = threadLocal;
      _cache = cache;
    }

    /**
     * Assigns the cache to the thread local variable.
     *
     * @return this
     */
    private ThreadLocalCache set() {
      _threadLocal.set(_cache);
      return this;
    }

    /**
     * Removes the cache from the thread local variable.
     */
    @Override
    public void close() {
      _threadLocal.remove();
    }
  }
}
