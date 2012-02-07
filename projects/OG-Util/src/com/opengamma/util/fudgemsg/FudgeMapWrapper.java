/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.fudgemsg;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectBean;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;

/**
 * A wrapper for a map that ensures transfer by Fudge.
 * <p>
 * Fudge does not handle transfer of maps on their own very well,
 * but does handle them when wrapped in this class.
 * <p>
 * Due to Joda-Bean limitations, this class is not generified.
 */
@BeanDefinition
public class FudgeMapWrapper extends DirectBean {

  /**
   * The map pairs.
   */
  @PropertyDefinition(validate = "notNull")
  private final List<Pair<?, ?>> _pairs = new ArrayList<Pair<?, ?>>();

  /**
   * Creates an instance.
   * 
   * @param map  the map, not null
   * @return the wrapped map, not null
   */
  public static FudgeMapWrapper of(Map<?, ?> map) {
    return new FudgeMapWrapper(map);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates an instance.
   */
  private FudgeMapWrapper() {
  }

  /**
   * Creates an instance.
   * 
   * @param map  the map, not null
   */
  public FudgeMapWrapper(Map<?, ?> map) {
    for (Entry<?, ?> entry : map.entrySet()) {
      getPairs().add(ObjectsPair.of(entry.getKey(), entry.getValue()));
    }
  }

  /**
   * Gets the map.
   * 
   * @return the map, not null
   */
  @SuppressWarnings({"unchecked", "rawtypes" })
  public Map getMap() {
    Map map = new LinkedHashMap();
    for (Object obj : getPairs()) {
      Pair pair = (Pair) obj;
      map.put(pair.getFirst(), pair.getSecond());
    }
    return map;
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code FudgeMapWrapper}.
   * @return the meta-bean, not null
   */
  public static FudgeMapWrapper.Meta meta() {
    return FudgeMapWrapper.Meta.INSTANCE;
  }
  static {
    JodaBeanUtils.registerMetaBean(FudgeMapWrapper.Meta.INSTANCE);
  }

  @Override
  public FudgeMapWrapper.Meta metaBean() {
    return FudgeMapWrapper.Meta.INSTANCE;
  }

  @Override
  protected Object propertyGet(String propertyName, boolean quiet) {
    switch (propertyName.hashCode()) {
      case 106428633:  // pairs
        return getPairs();
    }
    return super.propertyGet(propertyName, quiet);
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void propertySet(String propertyName, Object newValue, boolean quiet) {
    switch (propertyName.hashCode()) {
      case 106428633:  // pairs
        setPairs((List<Pair<?, ?>>) newValue);
        return;
    }
    super.propertySet(propertyName, newValue, quiet);
  }

  @Override
  protected void validate() {
    JodaBeanUtils.notNull(_pairs, "pairs");
    super.validate();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      FudgeMapWrapper other = (FudgeMapWrapper) obj;
      return JodaBeanUtils.equal(getPairs(), other.getPairs());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash += hash * 31 + JodaBeanUtils.hashCode(getPairs());
    return hash;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the map pairs.
   * @return the value of the property, not null
   */
  public List<Pair<?, ?>> getPairs() {
    return _pairs;
  }

  /**
   * Sets the map pairs.
   * @param pairs  the new value of the property
   */
  public void setPairs(List<Pair<?, ?>> pairs) {
    this._pairs.clear();
    this._pairs.addAll(pairs);
  }

  /**
   * Gets the the {@code pairs} property.
   * @return the property, not null
   */
  public final Property<List<Pair<?, ?>>> pairs() {
    return metaBean().pairs().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code FudgeMapWrapper}.
   */
  public static class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code pairs} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<Pair<?, ?>>> _pairs = DirectMetaProperty.ofReadWrite(
        this, "pairs", FudgeMapWrapper.class, (Class) List.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<Object>> _map = new DirectMetaPropertyMap(
        this, null,
        "pairs");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 106428633:  // pairs
          return _pairs;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends FudgeMapWrapper> builder() {
      return new DirectBeanBuilder<FudgeMapWrapper>(new FudgeMapWrapper());
    }

    @Override
    public Class<? extends FudgeMapWrapper> beanType() {
      return FudgeMapWrapper.class;
    }

    @Override
    public Map<String, MetaProperty<Object>> metaPropertyMap() {
      return _map;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code pairs} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<List<Pair<?, ?>>> pairs() {
      return _pairs;
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
