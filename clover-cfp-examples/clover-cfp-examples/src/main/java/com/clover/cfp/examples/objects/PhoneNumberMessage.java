package com.clover.cfp.examples.objects;

/**
 * Created by glennbedwell on 5/4/17.
 */
public class PhoneNumberMessage extends PayloadMessage{
  public final String phoneNumber;
  public PhoneNumberMessage(String phoneNumber) {
    super("PhoneNumberMessage", MessageType.PHONE_NUMBER);
    this.phoneNumber = phoneNumber;
  }
}