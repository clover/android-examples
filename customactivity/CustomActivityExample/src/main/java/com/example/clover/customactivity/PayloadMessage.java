package com.example.clover.customactivity;

public class PayloadMessage {

  private String payloadContent;
  private boolean sentToCustomActivity;

  public PayloadMessage(String payloadContent, boolean sentToCustomActivity){
    this.payloadContent = payloadContent;
    this.sentToCustomActivity = sentToCustomActivity;
  }

  public String getPayloadContent() {
    return payloadContent;
  }

  public void setPayloadContent(String payloadContent) {
    this.payloadContent = payloadContent;
  }

  public boolean isSentToCustomActivity() {
    return sentToCustomActivity;
  }

  public void setSentToCustomActivity(boolean sentToCustomActivity) {
    this.sentToCustomActivity = sentToCustomActivity;
  }
}
