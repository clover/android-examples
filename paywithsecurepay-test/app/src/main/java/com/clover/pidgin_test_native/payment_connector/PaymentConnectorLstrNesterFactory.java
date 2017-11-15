package com.clover.pidgin_test_native.payment_connector;

import com.clover.paywithsecurepay_pidgin_test.LstrNester;
import com.clover.paywithsecurepay_pidgin_test.LstrNesterFactory;

/**
 * Created by connor on 10/31/17.
 */
public class PaymentConnectorLstrNesterFactory implements LstrNesterFactory {
  @Override
  public LstrNester make() {
    return new PaymentConnectorLstrNester();
  }
}
