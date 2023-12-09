package data.dynamodb.repository.common.exception;

public class DynamoDBMapperConfigMustNotBeNullException extends RuntimeException {

    public DynamoDBMapperConfigMustNotBeNullException() {
        super("DynamoDBMapperConfig must not be null.");
    }
}
