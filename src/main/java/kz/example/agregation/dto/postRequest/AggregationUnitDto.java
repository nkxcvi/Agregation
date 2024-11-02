package kz.example.agregation.dto.postRequest;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class AggregationUnitDto {
    private int aggregatedItemsCount;
    private String aggregationType;
    private int aggregationUnitCapacity;
    private List<String> sntins;
    private String unitSerialNumber;
}
