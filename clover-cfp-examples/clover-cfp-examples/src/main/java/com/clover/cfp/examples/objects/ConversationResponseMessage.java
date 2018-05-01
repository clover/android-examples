package com.clover.cfp.examples.objects;

/**
 * Created by glennbedwell on 5/4/17.
 */
public class ConversationResponseMessage extends PayloadMessage{
  public final String message;
  public ConversationResponseMessage(String message) {
    super("ConversationResponseMessage", MessageType.CONVERSATION_RESPONSE);
    this.message = message;
  }
}