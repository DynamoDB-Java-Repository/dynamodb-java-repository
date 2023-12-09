package data.dynamodb.repository.common.exception;

public class IdMustNotBeNullException extends RuntimeException {

    public IdMustNotBeNullException() {
        super("Id must not be null.");
    }
}
