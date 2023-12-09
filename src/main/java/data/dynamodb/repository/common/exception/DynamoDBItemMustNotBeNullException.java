package data.dynamodb.repository.common.exception;

public class DynamoDBItemMustNotBeNullException extends RuntimeException {
    public DynamoDBItemMustNotBeNullException() {
        super("DynamoDB Item must not be null.");
    }
}
