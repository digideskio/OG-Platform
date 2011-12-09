/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.spring;

import java.util.Map;

import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.opengamma.masterdb.engine.stats.DbFunctionCostsMaster;
import com.opengamma.util.db.DbConnector;
import com.opengamma.util.spring.SpringFactoryBean;

/**
 * Spring factory bean to create the database function costs master.
 */
@BeanDefinition
public class DbFunctionCostsMasterFactoryBean extends SpringFactoryBean<DbFunctionCostsMaster> {

  /**
   * The database connector.
   */
  @PropertyDefinition
  private DbConnector _dbConnector;

  /**
   * Creates an instance.
   */
  public DbFunctionCostsMasterFactoryBean() {
    super(DbFunctionCostsMaster.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public DbFunctionCostsMaster createObject() {
    DbFunctionCostsMaster master = new DbFunctionCostsMaster(getDbConnector());
    return master;
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code DbFunctionCostsMasterFactoryBean}.
   * @return the meta-bean, not null
   */
  @SuppressWarnings("unchecked")
  public static DbFunctionCostsMasterFactoryBean.Meta meta() {
    return DbFunctionCostsMasterFactoryBean.Meta.INSTANCE;
  }
  static {
    JodaBeanUtils.registerMetaBean(DbFunctionCostsMasterFactoryBean.Meta.INSTANCE);
  }

  @Override
  public DbFunctionCostsMasterFactoryBean.Meta metaBean() {
    return DbFunctionCostsMasterFactoryBean.Meta.INSTANCE;
  }

  @Override
  protected Object propertyGet(String propertyName, boolean quiet) {
    switch (propertyName.hashCode()) {
      case 39794031:  // dbConnector
        return getDbConnector();
    }
    return super.propertyGet(propertyName, quiet);
  }

  @Override
  protected void propertySet(String propertyName, Object newValue, boolean quiet) {
    switch (propertyName.hashCode()) {
      case 39794031:  // dbConnector
        setDbConnector((DbConnector) newValue);
        return;
    }
    super.propertySet(propertyName, newValue, quiet);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      DbFunctionCostsMasterFactoryBean other = (DbFunctionCostsMasterFactoryBean) obj;
      return JodaBeanUtils.equal(getDbConnector(), other.getDbConnector()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash += hash * 31 + JodaBeanUtils.hashCode(getDbConnector());
    return hash ^ super.hashCode();
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the database connector.
   * @return the value of the property
   */
  public DbConnector getDbConnector() {
    return _dbConnector;
  }

  /**
   * Sets the database connector.
   * @param dbConnector  the new value of the property
   */
  public void setDbConnector(DbConnector dbConnector) {
    this._dbConnector = dbConnector;
  }

  /**
   * Gets the the {@code dbConnector} property.
   * @return the property, not null
   */
  public final Property<DbConnector> dbConnector() {
    return metaBean().dbConnector().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code DbFunctionCostsMasterFactoryBean}.
   */
  public static class Meta extends SpringFactoryBean.Meta<DbFunctionCostsMaster> {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code dbConnector} property.
     */
    private final MetaProperty<DbConnector> _dbConnector = DirectMetaProperty.ofReadWrite(
        this, "dbConnector", DbFunctionCostsMasterFactoryBean.class, DbConnector.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<Object>> _map = new DirectMetaPropertyMap(
      this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "dbConnector");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 39794031:  // dbConnector
          return _dbConnector;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends DbFunctionCostsMasterFactoryBean> builder() {
      return new DirectBeanBuilder<DbFunctionCostsMasterFactoryBean>(new DbFunctionCostsMasterFactoryBean());
    }

    @Override
    public Class<? extends DbFunctionCostsMasterFactoryBean> beanType() {
      return DbFunctionCostsMasterFactoryBean.class;
    }

    @Override
    public Map<String, MetaProperty<Object>> metaPropertyMap() {
      return _map;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code dbConnector} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<DbConnector> dbConnector() {
      return _dbConnector;
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
