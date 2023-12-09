package data.dynamodb.repository.custom;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.local.main.ServerRunner;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import data.dynamodb.repository.crud.input.model.DynamoTestModel;
import data.dynamodb.repository.crud.input.repository.DynamoTestRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DynamoDBExpressionBuilderTest {


    private DynamoTestRepository dynamoTestRepository;
    private AmazonDynamoDB amazonDynamoDB;

    @BeforeAll
    void setUp() throws Exception {
        var dynamoDBProxyServer = ServerRunner.createServerFromCommandLineArgs(
                new String[]{"-inMemory", "-port", "9999"}
        );
        dynamoDBProxyServer.start();
        createAmazonDynamoDBClient();

        dynamoTestRepository = new DynamoTestRepository(new DynamoDBMapper(amazonDynamoDB));
    }

    @BeforeEach
    void setUpTable() {
        createTables();
    }

    @AfterEach
    void cleanUpTable() {
        deleteTable();
    }

    private void createAmazonDynamoDBClient() {
        this.amazonDynamoDB = AmazonDynamoDBClientBuilder.standard()
                .withEndpointConfiguration(
                        new AwsClientBuilder.EndpointConfiguration("http://localhost:9999", "us-west-2"))
                .build();
    }

    private void createTables() {
        var mapper = new DynamoDBMapper(this.amazonDynamoDB);
        var tableRequest = mapper.generateCreateTableRequest(DynamoTestModel.class);
        tableRequest.setProvisionedThroughput(new ProvisionedThroughput(1L, 1L));
        amazonDynamoDB.createTable(tableRequest);


    }

    private void deleteTable() {
        var mapper = new DynamoDBMapper(this.amazonDynamoDB);
        var tableRequest = mapper.generateDeleteTableRequest(DynamoTestModel.class);
        amazonDynamoDB.deleteTable(tableRequest);
    }


    @Test
    void givenDynamoDBCustomQueryBuilderBuild_WhenAtLeastAConditionIsApplied_ThenShouldReturnTheQueryContainingTheseConditions() {
        var query = DynamoDBExpressionBuilder.builder()
                .contains("test", ":test")
                .and()
                .contains("test", ":test2")
                .or()
                .contains("test", "test3")
                .and()
                .in("otherField", ":param01", ":param02", "param03")
                .or()
                .attributeNotExists("this_attribute_should_not_exists")
                .and()
                .attributeExists("this_should_exists")
                .and()
                .between("someRangeField", ":init", ":end")
                .build();

        String expected = " ( contains(test, :test) )  " +
                "and  ( contains(test, :test2) )  " +
                "or  ( contains(test, test3) )  " +
                "and  ( otherField in (:param01, :param02, param03) )  " +
                "or  ( attribute_not_exists(this_attribute_should_not_exists) )  " +
                "and  ( attribute_exists(this_should_exists) )  " +
                "and  ( someRangeField between :init and :end ) ";

        Assertions.assertEquals(expected, query);
    }

    @Test
    void givenScanFilter_WhenTheresContainsOrInOrBeginsWith_ThenResultShouldReturnMatchingFilters() {

        Map<String, AttributeValue> attributes = new HashMap<>();
        attributes.put(":fruit0", new AttributeValue().withS("tomato"));
        attributes.put(":fruit1", new AttributeValue().withS("jackfruit"));
        attributes.put(":fruit2", new AttributeValue().withS("avocado"));
        attributes.put(":fruit3", new AttributeValue().withS("drago"));

        var query = DynamoDBExpressionBuilder.builder()
                .contains("valueFieldTest", ":fruit0")
                .or()
                .in("valueFieldTest", ":fruit1", ":fruit2")
                .or()
                .beginsWith("valueFieldTest", ":fruit3");


        dynamoTestRepository.saveAll(Arrays.asList(
                DynamoTestModel.builder().value("mango").build(),
                DynamoTestModel.builder().value("avocado").build(),
                DynamoTestModel.builder().value("other fruit").build(),
                DynamoTestModel.builder().value("other other fruit").build(),
                DynamoTestModel.builder().value("jackfruit").build(),
                DynamoTestModel.builder().value("tomato").build(),
                DynamoTestModel.builder().value("dragon fruit").build()
        ));

        var scanResult = dynamoTestRepository.scanBy(query.buildScanExpression(attributes));

        var results = scanResult.getResults();

        Assertions.assertEquals(4, scanResult.getCount());

        Assertions.assertTrue(results.stream().anyMatch(fruit -> fruit.getValue().equals("avocado")));
        Assertions.assertTrue(results.stream().anyMatch(fruit -> fruit.getValue().equals("jackfruit")));
        Assertions.assertTrue(results.stream().anyMatch(fruit -> fruit.getValue().equals("tomato")));
        Assertions.assertTrue(results.stream().anyMatch(fruit -> fruit.getValue().equals("tomato")));
        Assertions.assertTrue(results.stream().anyMatch(fruit -> fruit.getValue().equals("dragon fruit")));

    }

    @Test
    void givenScanFilter_WhenItsVerifyingIfAttributesExistsOrDont_ThenResultShouldReturnMatchingFilters() {

        Map<String, AttributeValue> attributes = new HashMap<>();
        attributes.put(":fruit0", new AttributeValue().withS("dr"));

        // If there's some value that starts with "dr", then must return given that "id" exists.
        var query = DynamoDBExpressionBuilder.builder()
                .beginsWith("valueFieldTest", ":fruit0")
                .and()
                .attributeExists("id");

        dynamoTestRepository.save(DynamoTestModel.builder().value("dragon fruit").build());
        var scanResult = dynamoTestRepository.scanBy(query.buildScanExpression(attributes));

        Assertions.assertEquals(1, scanResult.getCount());
        Assertions.assertEquals("dragon fruit", scanResult.getResults().get(0).getValue());


        // It will return nothing, even that exists a dragon fruit. This because we are expecting that there's no attribute "id",
        // but it actually exists.
        var query2 = DynamoDBExpressionBuilder.builder()
                .beginsWith("valueFieldTest", ":fruit0")
                .and()
                .attributeNotExists("id");

        var scanResult2 = dynamoTestRepository.scanBy(query2.buildScanExpression(attributes));

        Assertions.assertEquals(0, scanResult2.getCount());
        Assertions.assertEquals(0, scanResult2.getResults().size());


    }
}
