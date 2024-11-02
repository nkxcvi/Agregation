package kz.example.agregation.dto.postRequest;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
@Data
@AllArgsConstructor
public class AggregationDto {
    private List<AggregationUnitDto> aggregationUnits;
    private String participantId;
    private String productionLineId;
    private String productionOrderId;
}
