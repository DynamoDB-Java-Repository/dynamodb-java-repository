package data.dynamodb.repository.common.pagination;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Pageable {

    private Integer pageSize;

    /**
     * This value points to the next page.
     * Should contain the lastEvaluedKey, returned on Page object.
     * If this field is null or empty, the first page will be queried.
     * */
    private Map<String, AttributeValue> startKey;
}
