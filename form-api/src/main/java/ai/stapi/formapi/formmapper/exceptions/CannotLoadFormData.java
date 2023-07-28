package ai.stapi.formapi.formmapper.exceptions;

public class CannotLoadFormData extends RuntimeException {

    private static final String MSG = "Cannot load data for form, because ";

    private CannotLoadFormData(String becauseMessage) {
        super(MSG + becauseMessage);
    }

    private CannotLoadFormData(String becauseMessage, Throwable cause) {
        super(MSG + becauseMessage, cause);
    }

    public static CannotLoadFormData becauseThereWasNoCommandHandlerDefinitionForProvidedOperation(
        String operationId
    ) {
        return new CannotLoadFormData(
            String.format(
                "there was not command handler definition with provided operation.%nOperation name: '%s'",
                operationId
            )
        );
    }

    public static CannotLoadFormData becauseThereWasNoCommandHandlerDefinitionForProvidedOperation(
        String operationId,
        Throwable cause
    ) {
        return new CannotLoadFormData(
            String.format(
                "there was not command handler definition with provided operation.%nOperation name: '%s'",
                operationId
            ),
            cause
        );
    }

    public static CannotLoadFormData becauseAggregateByProvidedResourceIdWasNotFound(
        String resourceId,
        Throwable cause
    ) {
        return new CannotLoadFormData(
            String.format(
                "no aggregate with provided resource id was found.%nResource id: '%s'",
                resourceId
            ),
            cause
        );
    }
}
