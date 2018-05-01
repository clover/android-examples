package com.clover.cfp.examples.objects;

/**
 * Created by glennbedwell on 5/4/17.
 */
public class CustomerInfoMessage extends PayloadMessage {
  public final CustomerInfo customerInfo;

  public CustomerInfoMessage(CustomerInfo customerInfo) {
    super("CustomerInfoMessage", MessageType.CUSTOMER_INFO);
    this.customerInfo = customerInfo;
  }

}
