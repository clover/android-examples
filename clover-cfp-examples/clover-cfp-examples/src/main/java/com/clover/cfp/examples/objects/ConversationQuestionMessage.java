package com.clover.cfp.examples.objects;

/**
 * Created by glennbedwell on 5/4/17.
 */
public class ConversationQuestionMessage extends PayloadMessage{
  public final String message;
  public ConversationQuestionMessage(String message) {
    super("ConversationQuestionMessage", MessageType.CONVERSATION_QUESTION);
    this.message = message;
  }
}