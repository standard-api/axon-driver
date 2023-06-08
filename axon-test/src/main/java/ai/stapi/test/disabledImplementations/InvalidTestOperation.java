package ai.stapi.test.disabledImplementations;

public class InvalidTestOperation extends RuntimeException {

  private InvalidTestOperation(String becauseMessage) {
    super("Invalid operation during test, " + becauseMessage);
  }

  public static InvalidTestOperation becauseTestCaseDoesntAllowSendingCommands() {
    return new InvalidTestOperation("because TestCase doesnt allow sending commands.");
  }

  public static InvalidTestOperation becauseTestCaseDoesntAllowPublishingEvents() {
    return new InvalidTestOperation("because TestCase doesnt allow publishing events.");
  }

  public static InvalidTestOperation becauseTestCaseDoesntAllowQuerying() {
    return new InvalidTestOperation("because TestCase doesnt allow querying.");
  }
}
