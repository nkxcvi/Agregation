package kz.example.agregation.dto.postRequest;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ExcelDataDto {
    private String columnA;
    private String columnB;
}
