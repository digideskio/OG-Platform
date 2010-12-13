/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.position;

import java.util.Set;

import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.PublicSPI;

/**
 * A position in a security held in a portfolio.
 * <p>
 * A position is fundamentally a quantity of a security.
 * For example, a position might be 50 shares of OpenGamma.
 * <p>
 * It differs from a trade in that it may represent the combined result from a set of trades.
 * The collection of trades is optional and if present may be incomplete.
 */
@PublicSPI
public interface Position extends PositionOrTrade {

  /**
   * Gets the unique identifier of the node within the portfolio this position is immediately under.
   * 
   * @return the unique identifier
   */
  UniqueIdentifier getPortfolioNode();

  /**
   * Gets the trades immediate children of this position.
   * <p>
   * The set of trades is optional and if present may be incomplete.
   * 
   * @return the trades, unmodifiable, not null
   */
  Set<Trade> getTrades();

}
