/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import com.opengamma.analytics.financial.commodity.derivative.AgricultureForward;
import com.opengamma.analytics.financial.commodity.derivative.AgricultureFuture;
import com.opengamma.analytics.financial.commodity.derivative.AgricultureFutureOption;
import com.opengamma.analytics.financial.commodity.derivative.EnergyForward;
import com.opengamma.analytics.financial.commodity.derivative.EnergyFuture;
import com.opengamma.analytics.financial.commodity.derivative.EnergyFutureOption;
import com.opengamma.analytics.financial.commodity.derivative.MetalForward;
import com.opengamma.analytics.financial.commodity.derivative.MetalFuture;
import com.opengamma.analytics.financial.commodity.derivative.MetalFutureOption;
import com.opengamma.analytics.financial.credit.cds.ISDACDSDerivative;
import com.opengamma.analytics.financial.equity.future.derivative.CashSettledFuture;
import com.opengamma.analytics.financial.equity.future.derivative.EquityFuture;
import com.opengamma.analytics.financial.equity.future.derivative.EquityIndexDividendFuture;
import com.opengamma.analytics.financial.equity.future.derivative.EquityIndexFuture;
import com.opengamma.analytics.financial.equity.future.derivative.IndexFuture;
import com.opengamma.analytics.financial.equity.future.derivative.VolatilityIndexFuture;
import com.opengamma.analytics.financial.equity.option.EquityIndexFutureOption;
import com.opengamma.analytics.financial.equity.option.EquityIndexOption;
import com.opengamma.analytics.financial.equity.option.EquityOption;
import com.opengamma.analytics.financial.equity.variance.EquityVarianceSwap;
import com.opengamma.analytics.financial.forex.derivative.Forex;
import com.opengamma.analytics.financial.forex.derivative.ForexNonDeliverableForward;
import com.opengamma.analytics.financial.forex.derivative.ForexNonDeliverableOption;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionDigital;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionSingleBarrier;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.analytics.financial.forex.derivative.ForexSwap;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponIborRatchet;
import com.opengamma.analytics.financial.interestrate.bond.definition.BillSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BillTransaction;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondCapitalIndexedSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondCapitalIndexedTransaction;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedTransaction;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondIborSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondIborTransaction;
import com.opengamma.analytics.financial.interestrate.cash.derivative.Cash;
import com.opengamma.analytics.financial.interestrate.cash.derivative.DepositCounterpart;
import com.opengamma.analytics.financial.interestrate.cash.derivative.DepositIbor;
import com.opengamma.analytics.financial.interestrate.cash.derivative.DepositZero;
import com.opengamma.analytics.financial.interestrate.fra.derivative.ForwardRateAgreement;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuture;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFutureOptionPremiumSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFutureOptionPremiumTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.DeliverableSwapFuturesSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.FederalFundsFutureSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.FederalFundsFutureTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionPremiumSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionPremiumTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureTransaction;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationYearOnYearInterpolation;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationYearOnYearMonthly;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationZeroCouponInterpolation;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationZeroCouponInterpolationGearing;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationZeroCouponMonthly;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationZeroCouponMonthlyGearing;
import com.opengamma.analytics.financial.interestrate.payments.ForexForward;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorCMS;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorCMSSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponArithmeticAverageON;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponCMS;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixedCompounding;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborAverage;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborCompounding;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborCompoundingSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborGearing;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponOIS;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCompoundingCoupon;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionBermudaFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionCashFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.varianceswap.VarianceSwap;

/**
 * 
 * @param <DATA_TYPE> The type of the data
 * @param <RESULT_TYPE> The return type of the calculation
 */
public interface InstrumentDerivativeVisitor<DATA_TYPE, RESULT_TYPE> {

  // Two arguments

  RESULT_TYPE visitBondFixedSecurity(BondFixedSecurity bond, DATA_TYPE data);

  RESULT_TYPE visitBondFixedTransaction(BondFixedTransaction bond, DATA_TYPE data);

  RESULT_TYPE visitBondIborSecurity(BondIborSecurity bond, DATA_TYPE data);

  RESULT_TYPE visitBondIborTransaction(BondIborTransaction bond, DATA_TYPE data);

  RESULT_TYPE visitBillSecurity(BillSecurity bill, DATA_TYPE data);

  RESULT_TYPE visitBillTransaction(BillTransaction bill, DATA_TYPE data);

  RESULT_TYPE visitGenericAnnuity(Annuity<? extends Payment> genericAnnuity, DATA_TYPE data);

  RESULT_TYPE visitFixedCouponAnnuity(AnnuityCouponFixed fixedCouponAnnuity, DATA_TYPE data);

  RESULT_TYPE visitAnnuityCouponIborRatchet(AnnuityCouponIborRatchet annuity, DATA_TYPE data);

  RESULT_TYPE visitFixedCouponSwap(SwapFixedCoupon<?> swap, DATA_TYPE data);

  RESULT_TYPE visitFixedCompoundingCouponSwap(SwapFixedCompoundingCoupon<?> swap, DATA_TYPE data);

  RESULT_TYPE visitSwaptionCashFixedIbor(SwaptionCashFixedIbor swaption, DATA_TYPE data);

  RESULT_TYPE visitSwaptionPhysicalFixedIbor(SwaptionPhysicalFixedIbor swaption, DATA_TYPE data);

  RESULT_TYPE visitSwaptionBermudaFixedIbor(SwaptionBermudaFixedIbor swaption, DATA_TYPE data);

  RESULT_TYPE visitForexForward(ForexForward fx, DATA_TYPE data);

  RESULT_TYPE visitCash(Cash cash, DATA_TYPE data);

  RESULT_TYPE visitFixedPayment(PaymentFixed payment, DATA_TYPE data);

  RESULT_TYPE visitCouponCMS(CouponCMS payment, DATA_TYPE data);

  RESULT_TYPE visitCapFloorIbor(CapFloorIbor payment, DATA_TYPE data);

  RESULT_TYPE visitCapFloorCMS(CapFloorCMS payment, DATA_TYPE data);

  RESULT_TYPE visitCapFloorCMSSpread(CapFloorCMSSpread payment, DATA_TYPE data);

  RESULT_TYPE visitForwardRateAgreement(ForwardRateAgreement fra, DATA_TYPE data);

  RESULT_TYPE visitBondCapitalIndexedSecurity(BondCapitalIndexedSecurity<?> bond, DATA_TYPE data);

  RESULT_TYPE visitBondCapitalIndexedTransaction(BondCapitalIndexedTransaction<?> bond, DATA_TYPE data);

  RESULT_TYPE visitCDSDerivative(ISDACDSDerivative cds, DATA_TYPE data);

  // One argument

  RESULT_TYPE visitBondFixedSecurity(BondFixedSecurity bond);

  RESULT_TYPE visitBondFixedTransaction(BondFixedTransaction bond);

  RESULT_TYPE visitBondIborSecurity(BondIborSecurity bond);

  RESULT_TYPE visitBondIborTransaction(BondIborTransaction bond);

  RESULT_TYPE visitBillSecurity(BillSecurity bill);

  RESULT_TYPE visitBillTransaction(BillTransaction bill);

  RESULT_TYPE visitGenericAnnuity(Annuity<? extends Payment> genericAnnuity);

  RESULT_TYPE visitFixedCouponAnnuity(AnnuityCouponFixed fixedCouponAnnuity);

  RESULT_TYPE visitAnnuityCouponIborRatchet(AnnuityCouponIborRatchet annuity);

  RESULT_TYPE visitFixedCouponSwap(SwapFixedCoupon<?> swap);

  RESULT_TYPE visitFixedCompoundingCouponSwap(SwapFixedCompoundingCoupon<?> swap);

  RESULT_TYPE visitSwaptionCashFixedIbor(SwaptionCashFixedIbor swaption);

  RESULT_TYPE visitSwaptionPhysicalFixedIbor(SwaptionPhysicalFixedIbor swaption);

  RESULT_TYPE visitSwaptionBermudaFixedIbor(SwaptionBermudaFixedIbor swaption);

  RESULT_TYPE visitForexForward(ForexForward fx);

  RESULT_TYPE visitCash(Cash cash);

  RESULT_TYPE visitFixedPayment(PaymentFixed payment);

  RESULT_TYPE visitCouponCMS(CouponCMS payment);

  RESULT_TYPE visitCapFloorIbor(CapFloorIbor payment);

  RESULT_TYPE visitCapFloorCMS(CapFloorCMS payment);

  RESULT_TYPE visitCapFloorCMSSpread(CapFloorCMSSpread payment);

  RESULT_TYPE visitForwardRateAgreement(ForwardRateAgreement fra);

  RESULT_TYPE visitBondCapitalIndexedSecurity(BondCapitalIndexedSecurity<?> bond);

  RESULT_TYPE visitBondCapitalIndexedTransaction(BondCapitalIndexedTransaction<?> bond);

  RESULT_TYPE visitCDSDerivative(ISDACDSDerivative cds);

  // -----     Coupons     -----

  RESULT_TYPE visitCouponFixed(CouponFixed payment, DATA_TYPE data);

  RESULT_TYPE visitCouponFixed(CouponFixed payment);

  RESULT_TYPE visitCouponFixedCompounding(CouponFixedCompounding payment, DATA_TYPE data);

  RESULT_TYPE visitCouponFixedCompounding(CouponFixedCompounding payment);

  RESULT_TYPE visitCouponIbor(CouponIbor payment, DATA_TYPE data);

  RESULT_TYPE visitCouponIbor(CouponIbor payment);

  RESULT_TYPE visitCouponIborAverage(CouponIborAverage payment, DATA_TYPE data);

  RESULT_TYPE visitCouponIborAverage(CouponIborAverage payment);

  RESULT_TYPE visitCouponIborSpread(CouponIborSpread payment, DATA_TYPE data);

  RESULT_TYPE visitCouponIborSpread(CouponIborSpread payment);

  RESULT_TYPE visitCouponIborGearing(CouponIborGearing payment);

  RESULT_TYPE visitCouponIborGearing(CouponIborGearing payment, DATA_TYPE data);

  RESULT_TYPE visitCouponIborCompounding(CouponIborCompounding payment);

  RESULT_TYPE visitCouponIborCompounding(CouponIborCompounding payment, DATA_TYPE data);

  RESULT_TYPE visitCouponIborCompoundingSpread(CouponIborCompoundingSpread payment);

  RESULT_TYPE visitCouponIborCompoundingSpread(CouponIborCompoundingSpread payment, DATA_TYPE data);

  RESULT_TYPE visitCouponOIS(CouponOIS payment, DATA_TYPE data);

  RESULT_TYPE visitCouponOIS(CouponOIS payment);

  RESULT_TYPE visitCouponArithmeticAverageON(CouponArithmeticAverageON payment, DATA_TYPE data);

  RESULT_TYPE visitCouponArithmeticAverageON(CouponArithmeticAverageON payment);

  // -----     Swap     -----

  RESULT_TYPE visitSwap(Swap<?, ?> swap, DATA_TYPE data);

  RESULT_TYPE visitSwap(Swap<?, ?> swap);

  // -----     Inflation     -----

  RESULT_TYPE visitCouponInflationZeroCouponMonthly(CouponInflationZeroCouponMonthly coupon, DATA_TYPE data);

  RESULT_TYPE visitCouponInflationZeroCouponMonthly(CouponInflationZeroCouponMonthly coupon);

  RESULT_TYPE visitCouponInflationZeroCouponMonthlyGearing(CouponInflationZeroCouponMonthlyGearing coupon, DATA_TYPE data);

  RESULT_TYPE visitCouponInflationZeroCouponMonthlyGearing(CouponInflationZeroCouponMonthlyGearing coupon);

  RESULT_TYPE visitCouponInflationZeroCouponInterpolation(CouponInflationZeroCouponInterpolation coupon, DATA_TYPE data);

  RESULT_TYPE visitCouponInflationZeroCouponInterpolation(CouponInflationZeroCouponInterpolation coupon);

  RESULT_TYPE visitCouponInflationZeroCouponInterpolationGearing(CouponInflationZeroCouponInterpolationGearing coupon, DATA_TYPE data);

  RESULT_TYPE visitCouponInflationZeroCouponInterpolationGearing(CouponInflationZeroCouponInterpolationGearing coupon);

  RESULT_TYPE visitCouponInflationYearOnYearMonthly(CouponInflationYearOnYearMonthly coupon, DATA_TYPE data);

  RESULT_TYPE visitCouponInflationYearOnYearMonthly(CouponInflationYearOnYearMonthly coupon);

  RESULT_TYPE visitCouponInflationYearOnYearInterpolation(CouponInflationYearOnYearInterpolation coupon, DATA_TYPE data);

  RESULT_TYPE visitCouponInflationYearOnYearInterpolation(CouponInflationYearOnYearInterpolation coupon);

  // -----     Futures   -----

  RESULT_TYPE visitCashSettledFuture(CashSettledFuture future, DATA_TYPE data);

  RESULT_TYPE visitCashSettledFuture(CashSettledFuture future);

  RESULT_TYPE visitBondFuture(BondFuture bondFuture, DATA_TYPE data);

  RESULT_TYPE visitBondFuture(BondFuture future);

  RESULT_TYPE visitInterestRateFutureTransaction(InterestRateFutureTransaction future, DATA_TYPE data);

  RESULT_TYPE visitInterestRateFutureTransaction(InterestRateFutureTransaction future);

  RESULT_TYPE visitInterestRateFutureSecurity(InterestRateFutureSecurity future, DATA_TYPE data);

  RESULT_TYPE visitInterestRateFutureSecurity(InterestRateFutureSecurity future);

  RESULT_TYPE visitFederalFundsFutureSecurity(FederalFundsFutureSecurity future, DATA_TYPE data);

  RESULT_TYPE visitFederalFundsFutureSecurity(FederalFundsFutureSecurity future);

  RESULT_TYPE visitFederalFundsFutureTransaction(FederalFundsFutureTransaction future, DATA_TYPE data);

  RESULT_TYPE visitFederalFundsFutureTransaction(FederalFundsFutureTransaction future);

  RESULT_TYPE visitDeliverableSwapFuturesSecurity(DeliverableSwapFuturesSecurity futures, DATA_TYPE data);

  RESULT_TYPE visitDeliverableSwapFuturesSecurity(DeliverableSwapFuturesSecurity futures);

  // -----     Futures options   -----

  RESULT_TYPE visitBondFutureOptionPremiumSecurity(BondFutureOptionPremiumSecurity option, DATA_TYPE data);

  RESULT_TYPE visitBondFutureOptionPremiumSecurity(BondFutureOptionPremiumSecurity option);

  RESULT_TYPE visitBondFutureOptionPremiumTransaction(BondFutureOptionPremiumTransaction option, DATA_TYPE data);

  RESULT_TYPE visitBondFutureOptionPremiumTransaction(BondFutureOptionPremiumTransaction option);

  RESULT_TYPE visitInterestRateFutureOptionMarginSecurity(InterestRateFutureOptionMarginSecurity option, DATA_TYPE data);

  RESULT_TYPE visitInterestRateFutureOptionMarginSecurity(InterestRateFutureOptionMarginSecurity option);

  RESULT_TYPE visitInterestRateFutureOptionMarginTransaction(InterestRateFutureOptionMarginTransaction option, DATA_TYPE data);

  RESULT_TYPE visitInterestRateFutureOptionMarginTransaction(InterestRateFutureOptionMarginTransaction option);

  RESULT_TYPE visitInterestRateFutureOptionPremiumSecurity(InterestRateFutureOptionPremiumSecurity option, DATA_TYPE data);

  RESULT_TYPE visitInterestRateFutureOptionPremiumSecurity(InterestRateFutureOptionPremiumSecurity option);

  RESULT_TYPE visitInterestRateFutureOptionPremiumTransaction(InterestRateFutureOptionPremiumTransaction option, DATA_TYPE data);

  RESULT_TYPE visitInterestRateFutureOptionPremiumTransaction(InterestRateFutureOptionPremiumTransaction option);

  // -----     Deposit     -----

  RESULT_TYPE visitDepositIbor(DepositIbor deposit, DATA_TYPE data);

  RESULT_TYPE visitDepositIbor(DepositIbor deposit);

  RESULT_TYPE visitDepositCounterpart(DepositCounterpart deposit, DATA_TYPE data);

  RESULT_TYPE visitDepositCounterpart(DepositCounterpart deposit);

  RESULT_TYPE visitDepositZero(DepositZero deposit, DATA_TYPE data);

  RESULT_TYPE visitDepositZero(DepositZero deposit);

  // -----     Forex     -----

  RESULT_TYPE visitForex(Forex derivative, DATA_TYPE data);

  RESULT_TYPE visitForex(Forex derivative);

  RESULT_TYPE visitForexSwap(ForexSwap derivative, DATA_TYPE data);

  RESULT_TYPE visitForexSwap(ForexSwap derivative);

  RESULT_TYPE visitForexOptionVanilla(ForexOptionVanilla derivative, DATA_TYPE data);

  RESULT_TYPE visitForexOptionVanilla(ForexOptionVanilla derivative);

  RESULT_TYPE visitForexOptionSingleBarrier(ForexOptionSingleBarrier derivative, DATA_TYPE data);

  RESULT_TYPE visitForexOptionSingleBarrier(ForexOptionSingleBarrier derivative);

  RESULT_TYPE visitForexNonDeliverableForward(ForexNonDeliverableForward derivative, DATA_TYPE data);

  RESULT_TYPE visitForexNonDeliverableForward(ForexNonDeliverableForward derivative);

  RESULT_TYPE visitForexNonDeliverableOption(ForexNonDeliverableOption derivative, DATA_TYPE data);

  RESULT_TYPE visitForexNonDeliverableOption(ForexNonDeliverableOption derivative);

  RESULT_TYPE visitForexOptionDigital(ForexOptionDigital derivative, DATA_TYPE data);

  RESULT_TYPE visitForexOptionDigital(ForexOptionDigital derivative);

  //  -----     Commodity      -----

  RESULT_TYPE visitMetalForward(MetalForward future, DATA_TYPE data);

  RESULT_TYPE visitMetalForward(MetalForward future);

  RESULT_TYPE visitMetalFuture(MetalFuture future, DATA_TYPE data);

  RESULT_TYPE visitMetalFuture(MetalFuture future);

  RESULT_TYPE visitMetalFutureOption(MetalFutureOption future, DATA_TYPE data);

  RESULT_TYPE visitMetalFutureOption(MetalFutureOption future);

  RESULT_TYPE visitAgricultureForward(AgricultureForward future, DATA_TYPE data);

  RESULT_TYPE visitAgricultureForward(AgricultureForward future);

  RESULT_TYPE visitAgricultureFuture(AgricultureFuture future, DATA_TYPE data);

  RESULT_TYPE visitAgricultureFuture(AgricultureFuture future);

  RESULT_TYPE visitAgricultureFutureOption(AgricultureFutureOption future, DATA_TYPE data);

  RESULT_TYPE visitAgricultureFutureOption(AgricultureFutureOption future);

  RESULT_TYPE visitEnergyForward(EnergyForward future, DATA_TYPE data);

  RESULT_TYPE visitEnergyForward(EnergyForward future);

  RESULT_TYPE visitEnergyFuture(EnergyFuture future, DATA_TYPE data);

  RESULT_TYPE visitEnergyFuture(EnergyFuture future);

  RESULT_TYPE visitEnergyFutureOption(EnergyFutureOption future, DATA_TYPE data);

  RESULT_TYPE visitEnergyFutureOption(EnergyFutureOption future);

  //  -----     Equity     -----

  RESULT_TYPE visitEquityFuture(EquityFuture future);

  RESULT_TYPE visitEquityFuture(EquityFuture future, DATA_TYPE data);

  RESULT_TYPE visitIndexFuture(IndexFuture future, DATA_TYPE data);

  RESULT_TYPE visitIndexFuture(IndexFuture future);

  RESULT_TYPE visitEquityIndexFuture(EquityIndexFuture future, DATA_TYPE data);

  RESULT_TYPE visitEquityIndexFuture(EquityIndexFuture future);

  RESULT_TYPE visitEquityIndexDividendFuture(EquityIndexDividendFuture future);

  RESULT_TYPE visitEquityIndexDividendFuture(EquityIndexDividendFuture future, DATA_TYPE data);

  RESULT_TYPE visitVolatilityIndexFuture(VolatilityIndexFuture future, DATA_TYPE data);

  RESULT_TYPE visitVolatilityIndexFuture(VolatilityIndexFuture future);

  RESULT_TYPE visitEquityIndexOption(EquityIndexOption option, DATA_TYPE data);

  RESULT_TYPE visitEquityIndexOption(EquityIndexOption option);

  RESULT_TYPE visitEquityOption(EquityOption option, DATA_TYPE data);

  RESULT_TYPE visitEquityOption(EquityOption option);

  RESULT_TYPE visitEquityIndexFutureOption(EquityIndexFutureOption option, DATA_TYPE data);

  RESULT_TYPE visitEquityIndexFutureOption(EquityIndexFutureOption option);

  //  -----     Variance swaps     -----

  RESULT_TYPE visitVarianceSwap(VarianceSwap varianceSwap);

  RESULT_TYPE visitVarianceSwap(VarianceSwap varianceSwap, DATA_TYPE data);

  RESULT_TYPE visitEquityVarianceSwap(EquityVarianceSwap varianceSwap);

  RESULT_TYPE visitEquityVarianceSwap(EquityVarianceSwap varianceSwap, DATA_TYPE data);

  //  -----     Deprecated     -----

}
