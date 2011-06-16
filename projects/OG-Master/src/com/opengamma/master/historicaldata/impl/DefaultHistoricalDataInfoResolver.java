/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.historicaldata.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.master.historicaldata.HistoricalDataDocument;
import com.opengamma.master.historicaldata.HistoricalDataInfo;
import com.opengamma.master.historicaldata.HistoricalDataInfoResolver;
import com.opengamma.master.historicaldata.HistoricalDataMaster;
import com.opengamma.master.historicaldata.HistoricalDataSearchRequest;
import com.opengamma.master.historicaldata.HistoricalDataSearchResult;
import com.opengamma.util.ArgumentChecker;

/**
 * Simple historical data time-series resolver, returns the best match from
 * the info in the data store.
 * <p>
 * This resolver relies on configuration in the configuration database.
 */
public class DefaultHistoricalDataInfoResolver implements HistoricalDataInfoResolver {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(DefaultHistoricalDataInfoResolver.class);

  /**
   * The master.
   */
  private final HistoricalDataMaster _tsMaster;
  /**
   * The source of configuration.
   */
  private final ConfigSource _configSource;

  /**
   * Creates an instance from a master and configuration source.
   * 
   * @param historicalDataMaster  the historical data master, not null
   * @param configSource  the configuration source, not null
   */
  public DefaultHistoricalDataInfoResolver(HistoricalDataMaster historicalDataMaster, ConfigSource configSource) {
    ArgumentChecker.notNull(historicalDataMaster, "historicalDataMaster");
    ArgumentChecker.notNull(configSource, "configSource");
    _configSource = configSource;
    _tsMaster = historicalDataMaster;
  }

  //-------------------------------------------------------------------------
  @Override
  public HistoricalDataInfo getInfo(IdentifierBundle securityBundle, String configName) {
    ArgumentChecker.notNull(securityBundle, "securityBundle");
    ArgumentChecker.notNull(configName, "configName");
    
    // find time-series
    HistoricalDataSearchRequest searchRequest = new HistoricalDataSearchRequest(securityBundle);
    searchRequest.setDataField(DEFAULT_DATA_FIELD);
    searchRequest.setLoadTimeSeries(false);
    HistoricalDataSearchResult searchResult = _tsMaster.search(searchRequest);
    
    // pick best using rules from configuration
    HistoricalDataInfoConfiguration ruleSet = _configSource.getLatestByName(HistoricalDataInfoConfiguration.class, configName);
    if (ruleSet != null) {
      List<HistoricalDataInfo> infos = extractInfo(searchResult);
      return bestMatch(infos, ruleSet);
    } else {
      s_logger.warn("Unable to resolve time-series info because rules set with name {} can not be loaded from config database", configName);
      return null;
    }
  }

  /**
   * Converts the time-series results to the simpler info object for matching purposes.
   * 
   * @param searchResult  the search result, not null
   * @return the list of info objects, not null
   */
  private List<HistoricalDataInfo> extractInfo(HistoricalDataSearchResult searchResult) {
    List<HistoricalDataDocument> documents = searchResult.getDocuments();
    List<HistoricalDataInfo> infoList = new ArrayList<HistoricalDataInfo>(documents.size());
    for (HistoricalDataDocument document : documents) {
      infoList.add(document.toInfo());
    }
    return infoList;
  }

  /**
   * Choose the best match using the configured rules.
   * 
   * @param infoList  the list of info objects, not null
   * @param ruleSet  the configured rules, not null
   * @return the best match, null if no match
   */
  private HistoricalDataInfo bestMatch(List<HistoricalDataInfo> infoList, HistoricalDataInfoRateProvider ruleSet) {
    if (infoList.isEmpty()) {
      return null;
    }
    TreeMap<Integer, HistoricalDataInfo> scores = new TreeMap<Integer, HistoricalDataInfo>();
    for (HistoricalDataInfo info : infoList) {
      int score = ruleSet.rate(info);
      s_logger.debug("Score: {} for info: {} using rules: {} ", new Object[]{score, info, ruleSet});
      scores.put(score, info);
    }
    return scores.lastEntry().getValue();
  }

}
