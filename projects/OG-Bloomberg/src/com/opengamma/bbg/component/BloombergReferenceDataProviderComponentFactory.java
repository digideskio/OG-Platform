/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.bbg.component;

import java.util.LinkedHashMap;
import java.util.Map;

import net.sf.ehcache.CacheManager;

import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.opengamma.bbg.BloombergConnector;
import com.opengamma.bbg.BloombergReferenceDataProvider;
import com.opengamma.bbg.EHCachingReferenceDataProvider;
import com.opengamma.bbg.MongoDBCachingReferenceDataProvider;
import com.opengamma.bbg.ReferenceDataProvider;
import com.opengamma.bbg.referencedata.cache.MongoDBPermanentErrorCachingReferenceDataProvider;
import com.opengamma.component.ComponentInfo;
import com.opengamma.component.ComponentRepository;
import com.opengamma.component.factory.AbstractComponentFactory;
import com.opengamma.util.mongo.MongoConnector;

/**
 * Component factory for the Bloomberg reference data provider.
 */
@BeanDefinition
public class BloombergReferenceDataProviderComponentFactory extends AbstractComponentFactory {

  /**
   * The classifier that the factory should publish under.
   */
  @PropertyDefinition(validate = "notNull")
  private String _classifier;
  /**
   * The Bloomberg connector.
   */
  @PropertyDefinition(validate = "notNull")
  private BloombergConnector _bloombergConnector;
  /**
   * The Mongo connector.
   * If a Mongo connector is specified, then it is used for caching.
   */
  @PropertyDefinition
  private MongoConnector _mongoConnector;
  /**
   * The cache manager.
   * If a Mongo connector is specified, then this is not used.
   * If a Mongo connector is not specified and this is, then EH cache is used.
   */
  @PropertyDefinition
  private CacheManager _cacheManager;

  //-------------------------------------------------------------------------
  @Override
  public void init(ComponentRepository repo, LinkedHashMap<String, String> configuration) throws Exception {
    final ReferenceDataProvider provider = initReferenceDataProvider(repo);
    final ComponentInfo info = new ComponentInfo(ReferenceDataProvider.class, getClassifier());
    repo.registerComponent(info, provider);
  }

  /**
   * Creates the provider.
   * 
   * @param repo  the repository, not null
   * @return the provider, not null
   */
  protected ReferenceDataProvider initReferenceDataProvider(ComponentRepository repo) {
    BloombergConnector bloombergConnector = getBloombergConnector();
    MongoConnector mongoConnector = getMongoConnector();
    CacheManager cacheManager = getCacheManager();
    if (mongoConnector != null) {
      MongoDBPermanentErrorCachingReferenceDataProvider underlying = new MongoDBPermanentErrorCachingReferenceDataProvider(bloombergConnector, mongoConnector);
      repo.registerLifecycle(underlying);
      return new MongoDBCachingReferenceDataProvider(underlying, mongoConnector);
      
    } else if (cacheManager != null) {
      BloombergReferenceDataProvider underlying = new BloombergReferenceDataProvider(bloombergConnector);
      repo.registerLifecycle(underlying);
      return new EHCachingReferenceDataProvider(underlying, cacheManager);
      
    } else {
      return new BloombergReferenceDataProvider(bloombergConnector);
    }
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code BloombergReferenceDataProviderComponentFactory}.
   * @return the meta-bean, not null
   */
  public static BloombergReferenceDataProviderComponentFactory.Meta meta() {
    return BloombergReferenceDataProviderComponentFactory.Meta.INSTANCE;
  }
  static {
    JodaBeanUtils.registerMetaBean(BloombergReferenceDataProviderComponentFactory.Meta.INSTANCE);
  }

  @Override
  public BloombergReferenceDataProviderComponentFactory.Meta metaBean() {
    return BloombergReferenceDataProviderComponentFactory.Meta.INSTANCE;
  }

  @Override
  protected Object propertyGet(String propertyName, boolean quiet) {
    switch (propertyName.hashCode()) {
      case -281470431:  // classifier
        return getClassifier();
      case 2061648978:  // bloombergConnector
        return getBloombergConnector();
      case 224118201:  // mongoConnector
        return getMongoConnector();
      case -1452875317:  // cacheManager
        return getCacheManager();
    }
    return super.propertyGet(propertyName, quiet);
  }

  @Override
  protected void propertySet(String propertyName, Object newValue, boolean quiet) {
    switch (propertyName.hashCode()) {
      case -281470431:  // classifier
        setClassifier((String) newValue);
        return;
      case 2061648978:  // bloombergConnector
        setBloombergConnector((BloombergConnector) newValue);
        return;
      case 224118201:  // mongoConnector
        setMongoConnector((MongoConnector) newValue);
        return;
      case -1452875317:  // cacheManager
        setCacheManager((CacheManager) newValue);
        return;
    }
    super.propertySet(propertyName, newValue, quiet);
  }

  @Override
  protected void validate() {
    JodaBeanUtils.notNull(_classifier, "classifier");
    JodaBeanUtils.notNull(_bloombergConnector, "bloombergConnector");
    super.validate();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      BloombergReferenceDataProviderComponentFactory other = (BloombergReferenceDataProviderComponentFactory) obj;
      return JodaBeanUtils.equal(getClassifier(), other.getClassifier()) &&
          JodaBeanUtils.equal(getBloombergConnector(), other.getBloombergConnector()) &&
          JodaBeanUtils.equal(getMongoConnector(), other.getMongoConnector()) &&
          JodaBeanUtils.equal(getCacheManager(), other.getCacheManager()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash += hash * 31 + JodaBeanUtils.hashCode(getClassifier());
    hash += hash * 31 + JodaBeanUtils.hashCode(getBloombergConnector());
    hash += hash * 31 + JodaBeanUtils.hashCode(getMongoConnector());
    hash += hash * 31 + JodaBeanUtils.hashCode(getCacheManager());
    return hash ^ super.hashCode();
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the classifier that the factory should publish under.
   * @return the value of the property, not null
   */
  public String getClassifier() {
    return _classifier;
  }

  /**
   * Sets the classifier that the factory should publish under.
   * @param classifier  the new value of the property, not null
   */
  public void setClassifier(String classifier) {
    JodaBeanUtils.notNull(classifier, "classifier");
    this._classifier = classifier;
  }

  /**
   * Gets the the {@code classifier} property.
   * @return the property, not null
   */
  public final Property<String> classifier() {
    return metaBean().classifier().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the Bloomberg connector.
   * @return the value of the property, not null
   */
  public BloombergConnector getBloombergConnector() {
    return _bloombergConnector;
  }

  /**
   * Sets the Bloomberg connector.
   * @param bloombergConnector  the new value of the property, not null
   */
  public void setBloombergConnector(BloombergConnector bloombergConnector) {
    JodaBeanUtils.notNull(bloombergConnector, "bloombergConnector");
    this._bloombergConnector = bloombergConnector;
  }

  /**
   * Gets the the {@code bloombergConnector} property.
   * @return the property, not null
   */
  public final Property<BloombergConnector> bloombergConnector() {
    return metaBean().bloombergConnector().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the Mongo connector.
   * If a Mongo connector is specified, then it is used for caching.
   * @return the value of the property
   */
  public MongoConnector getMongoConnector() {
    return _mongoConnector;
  }

  /**
   * Sets the Mongo connector.
   * If a Mongo connector is specified, then it is used for caching.
   * @param mongoConnector  the new value of the property
   */
  public void setMongoConnector(MongoConnector mongoConnector) {
    this._mongoConnector = mongoConnector;
  }

  /**
   * Gets the the {@code mongoConnector} property.
   * If a Mongo connector is specified, then it is used for caching.
   * @return the property, not null
   */
  public final Property<MongoConnector> mongoConnector() {
    return metaBean().mongoConnector().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the cache manager.
   * If a Mongo connector is specified, then this is not used.
   * If a Mongo connector is not specified and this is, then EH cache is used.
   * @return the value of the property
   */
  public CacheManager getCacheManager() {
    return _cacheManager;
  }

  /**
   * Sets the cache manager.
   * If a Mongo connector is specified, then this is not used.
   * If a Mongo connector is not specified and this is, then EH cache is used.
   * @param cacheManager  the new value of the property
   */
  public void setCacheManager(CacheManager cacheManager) {
    this._cacheManager = cacheManager;
  }

  /**
   * Gets the the {@code cacheManager} property.
   * If a Mongo connector is specified, then this is not used.
   * If a Mongo connector is not specified and this is, then EH cache is used.
   * @return the property, not null
   */
  public final Property<CacheManager> cacheManager() {
    return metaBean().cacheManager().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code BloombergReferenceDataProviderComponentFactory}.
   */
  public static class Meta extends AbstractComponentFactory.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code classifier} property.
     */
    private final MetaProperty<String> _classifier = DirectMetaProperty.ofReadWrite(
        this, "classifier", BloombergReferenceDataProviderComponentFactory.class, String.class);
    /**
     * The meta-property for the {@code bloombergConnector} property.
     */
    private final MetaProperty<BloombergConnector> _bloombergConnector = DirectMetaProperty.ofReadWrite(
        this, "bloombergConnector", BloombergReferenceDataProviderComponentFactory.class, BloombergConnector.class);
    /**
     * The meta-property for the {@code mongoConnector} property.
     */
    private final MetaProperty<MongoConnector> _mongoConnector = DirectMetaProperty.ofReadWrite(
        this, "mongoConnector", BloombergReferenceDataProviderComponentFactory.class, MongoConnector.class);
    /**
     * The meta-property for the {@code cacheManager} property.
     */
    private final MetaProperty<CacheManager> _cacheManager = DirectMetaProperty.ofReadWrite(
        this, "cacheManager", BloombergReferenceDataProviderComponentFactory.class, CacheManager.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
      this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "classifier",
        "bloombergConnector",
        "mongoConnector",
        "cacheManager");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -281470431:  // classifier
          return _classifier;
        case 2061648978:  // bloombergConnector
          return _bloombergConnector;
        case 224118201:  // mongoConnector
          return _mongoConnector;
        case -1452875317:  // cacheManager
          return _cacheManager;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends BloombergReferenceDataProviderComponentFactory> builder() {
      return new DirectBeanBuilder<BloombergReferenceDataProviderComponentFactory>(new BloombergReferenceDataProviderComponentFactory());
    }

    @Override
    public Class<? extends BloombergReferenceDataProviderComponentFactory> beanType() {
      return BloombergReferenceDataProviderComponentFactory.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code classifier} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> classifier() {
      return _classifier;
    }

    /**
     * The meta-property for the {@code bloombergConnector} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<BloombergConnector> bloombergConnector() {
      return _bloombergConnector;
    }

    /**
     * The meta-property for the {@code mongoConnector} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<MongoConnector> mongoConnector() {
      return _mongoConnector;
    }

    /**
     * The meta-property for the {@code cacheManager} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<CacheManager> cacheManager() {
      return _cacheManager;
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
