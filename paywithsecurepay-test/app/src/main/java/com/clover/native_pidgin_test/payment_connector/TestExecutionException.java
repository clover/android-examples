package com.clover.native_pidgin_test.payment_connector;

/**
 * Created by connor on 11/1/17.
 */
public class TestExecutionException extends RuntimeException{
  public TestExecutionException() {
    super();
  }

  public TestExecutionException(String message) {
    super(message);
  }

  public TestExecutionException(String message, Throwable cause) {
    super(message, cause);
  }

  public TestExecutionException(Throwable cause) {
    super(cause);
  }
}
