package com.clover.native_pidgin_test.payment_connector;

import com.clover.pidgin_test_native_lib.LstrNester;
import com.clover.pidgin_test_native_lib.LstrNesterFactory;

/**
 * Created by connor on 10/31/17.
 */
public class PaymentConnectorLstrNesterFactory implements LstrNesterFactory {
  @Override
  public LstrNester make() {
    return new PaymentConnectorLstrNester();
  }
}
