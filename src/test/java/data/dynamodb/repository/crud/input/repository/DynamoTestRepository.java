package data.dynamodb.repository.crud.input.repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import data.dynamodb.repository.crud.DynamoDBCrudRepository;
import data.dynamodb.repository.crud.input.model.DynamoTestModel;

public class DynamoTestRepository extends DynamoDBCrudRepository<DynamoTestModel, String> {
    public DynamoTestRepository(DynamoDBMapper dynamoDBMapper) {
        super(dynamoDBMapper);
    }
}
