package ai.stapi.axonsystem.dynamic.aggregate.exceptions;

public class CannotProvideDynamicAggregateConfiguration extends Exception {

  public CannotProvideDynamicAggregateConfiguration(String aggregateType) {
    super(
        String.format(
            "Cannot provide Dynamic Aggregate Configuration for '%s', because it does not exists.",
            aggregateType
        )
    );
  }

  public CannotProvideDynamicAggregateConfiguration(String aggregateType, Throwable cause) {
    super(
        String.format(
            "Cannot provide Dynamic Aggregate Configuration for '%s', because it does not exists.\n Cause: %s",
            aggregateType,
            cause.getMessage()
        ),
        cause
    );
  }
}
