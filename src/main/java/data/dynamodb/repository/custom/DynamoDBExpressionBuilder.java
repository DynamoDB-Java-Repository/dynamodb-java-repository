package data.dynamodb.repository.custom;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;

import java.util.Map;

public class DynamoDBExpressionBuilder {

    private final StringBuilder query = new StringBuilder();

    public static DynamoDBExpressionBuilder builder() {
        return new DynamoDBExpressionBuilder();
    }

    public DynamoDBExpressionBuilder contains(String field, String attribute) {
        this.query.append(" ( contains(").append(field).append(", ").append(attribute).append(") ) ");
        return this;
    }

    public DynamoDBExpressionBuilder beginsWith(String field, String attribute) {
        this.query.append(" ( begins_with(").append(field).append(", ").append(attribute).append(" ) ) ");
        return this;
    }

    public DynamoDBExpressionBuilder in(String field, String... attributes) {
        this.query.append(" ( ").append(field).append(" in (");
        this.query.append(String.join(", ", attributes));
        this.query.append(") ) ");
        return this;
    }

    public DynamoDBExpressionBuilder between(String field, String from, String to) {
        this.query.append(" ( ").append(field).append(" between ").append(from).append(" and ").append(to).append(" ) ");
        return this;
    }

    public DynamoDBExpressionBuilder attributeNotExists(String attribute) {
        this.query.append(" ( ").append("attribute_not_exists(").append(attribute).append(") ) ");
        return this;
    }

    public DynamoDBExpressionBuilder attributeExists(String attribute) {
        this.query.append(" ( ").append("attribute_exists(").append(attribute).append(") ) ");
        return this;
    }

    public DynamoDBExpressionBuilder and() {
        this.query.append(" and ");
        return this;
    }

    public DynamoDBExpressionBuilder or() {
        this.query.append(" or ");
        return this;
    }

    public String build() {
        return this.query.toString();
    }

    public DynamoDBScanExpression buildScanExpression(Map<String, AttributeValue> attributes) {
        return new DynamoDBScanExpression()
                .withFilterExpression(this.query.toString())
                .withExpressionAttributeValues(attributes);
    }

}
