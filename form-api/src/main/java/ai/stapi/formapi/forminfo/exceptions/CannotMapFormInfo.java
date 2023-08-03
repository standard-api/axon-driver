package ai.stapi.formapi.forminfo.exceptions;

public class CannotMapFormInfo extends RuntimeException {

    private static final String MSG = "Cannot map form info, because ";

    private CannotMapFormInfo(String becauseMessage) {
        super(MSG + becauseMessage);
    }

    private CannotMapFormInfo(String becauseMessage, Throwable cause) {
        super(MSG + becauseMessage, cause);
    }

    public static CannotMapFormInfo becauseThereWasNoCommandHandlerDefinitionForProvidedOperation(
        String operationId
    ) {
        return new CannotMapFormInfo(
            String.format(
                "there was not command handler definition with provided operation.%nOperation name: '%s'",
                operationId
            )
        );
    }

    public static CannotMapFormInfo becauseThereWasNoCommandHandlerDefinitionForProvidedOperation(
        String operationId,
        Throwable cause
    ) {
        return new CannotMapFormInfo(
            String.format(
                "there was not command handler definition with provided operation.%nOperation name: '%s'",
                operationId
            ),
            cause
        );
    }
}
