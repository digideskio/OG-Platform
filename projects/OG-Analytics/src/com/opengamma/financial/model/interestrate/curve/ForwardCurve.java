/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.interestrate.curve;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.math.curve.ConstantDoublesCurve;
import com.opengamma.math.curve.Curve;
import com.opengamma.math.curve.FunctionalDoublesCurve;
import com.opengamma.math.function.Function;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.integration.RungeKuttaIntegrator1D;

/**
 * 
 */
public class ForwardCurve {
  private static final RungeKuttaIntegrator1D INTEGRATOR = new RungeKuttaIntegrator1D();
  private final Curve<Double, Double> _fwdCurve;
  private final Curve<Double, Double> _drift;
  private final double _spot;

  private ForwardCurve(final Curve<Double, Double> fwdCurve, final Curve<Double, Double> driftCurve) {
    Validate.notNull(fwdCurve, "null fwdCurve");
    Validate.notNull(driftCurve, "null driftCurve");
    _fwdCurve = fwdCurve;
    _drift = driftCurve;
    _spot = _fwdCurve.getYValue(0.0);
  }

  public ForwardCurve(final Curve<Double, Double> fwdCurve) {
    Validate.notNull(fwdCurve, "curve");
    _fwdCurve = fwdCurve;

    final Function1D<Double, Double> drift = new Function1D<Double, Double>() {
      private final double _eps = 1e-3;

      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final Double t) {

        final double up = Math.log(_fwdCurve.getYValue(t + _eps));

        if (t < _eps) {
          final double mid = Math.log(_fwdCurve.getYValue(t));
          return (up - mid) / _eps;
        }
        final double down = Math.log(_fwdCurve.getYValue(t - _eps));
        return (up - down) / 2 / _eps;
      }

    };

    _drift = FunctionalDoublesCurve.from(drift);
    _spot = _fwdCurve.getYValue(0.0);
  }

  /**
   * Forward curve with zero drift (i.e. curve is constant)
   * @param spot The spot rate
   */
  public ForwardCurve(final double spot) {
    _fwdCurve = ConstantDoublesCurve.from(spot);
    _drift = ConstantDoublesCurve.from(0.0);
    _spot = spot;
  }

  /**
   * Forward curve with constant drift
   * @param spot  The spot
   * @param drift The drift
   */
  public ForwardCurve(final double spot, final double drift) {
    _drift = ConstantDoublesCurve.from(drift);
    final Function1D<Double, Double> fwd = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(final Double t) {
        return spot * Math.exp(drift * t);
      }
    };
    _fwdCurve = new FunctionalDoublesCurve(fwd);
    _spot = spot;
  }

  /**
   * Forward curve with functional drift.
   * <b>Warning</b> This will be slow if you want access to the forward at many times
   * @param spot The spot rate
   * @param driftCurve The drift curve
   */
  public ForwardCurve(final double spot, final Curve<Double, Double> driftCurve) {
    Validate.notNull(driftCurve, "null driftCurve");
    _drift = driftCurve;
    final Function1D<Double, Double> driftFunc = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(final Double t) {
        return driftCurve.getYValue(t);
      }
    };

    //TODO cache integration results. REVIEW emcleod 22-6-2011 There's no point in thinking about caching until we know that this is a bottleneck
    final Function1D<Double, Double> fwd = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(final Double t) {
        @SuppressWarnings("synthetic-access")
        final double temp = INTEGRATOR.integrate(driftFunc, 0.0, t);
        return spot * Math.exp(temp);
      }
    };
    _fwdCurve = new FunctionalDoublesCurve(fwd);
    _spot = spot;
  }

  public ForwardCurve(final Function1D<Double, Double> func) {
    this(FunctionalDoublesCurve.from(func));
  }

  public Curve<Double, Double> getForwardCurve() {
    return _fwdCurve;
  }

  public double getForward(final double t) {
    return _fwdCurve.getYValue(t);
  }

  /**
   * Gets the drift.
   * @return the drift
   */
  public Curve<Double, Double> getDriftCurve() {
    return _drift;
  }

  public double getDrift(final double t) {
    return _drift.getYValue(t);
  }

  public double getSpot() {
    return _spot;
  }

  /**
   * Shift the forward curve by a fractional amount, shift, such that the new curve F'(T) = (1 + shift) * F(T), has
   * an unchanged drift.
   * @param shift The fractional shift amount, i.e. 0.1 will produce a curve 10% larger than the original
   * @return The shifted curve
   */
  public ForwardCurve withFractionalShift(final double shift) {
    Validate.isTrue(shift > -1, "shift must be > -1");

    final Function<Double, Double> func = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... t) {
        return (1 + shift) * _fwdCurve.getYValue(t[0]);
      }
    };
    return new ForwardCurve(FunctionalDoublesCurve.from(func), _drift);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _drift.hashCode();
    result = prime * result + _fwdCurve.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_spot);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final ForwardCurve other = (ForwardCurve) obj;
    if (!ObjectUtils.equals(_drift, other._drift)) {
      return false;
    }
    if (!ObjectUtils.equals(_fwdCurve, other._fwdCurve)) {
      return false;
    }
    if (Double.doubleToLongBits(_spot) != Double.doubleToLongBits(other._spot)) {
      return false;
    }
    return true;
  }

}
