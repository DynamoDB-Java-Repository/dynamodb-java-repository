package data.dynamodb.repository.common.pagination;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Page<T> {
    private List<T> content;
    private Map<String, AttributeValue> lastEvaluatedItem;
}
