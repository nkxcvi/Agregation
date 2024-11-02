package kz.example.agregation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import kz.example.agregation.dto.postRequest.AggregationDto;
import kz.example.agregation.dto.postRequest.AggregationUnitDto;
import kz.example.agregation.dto.postRequest.ExcelDataDto;
import kz.example.agregation.service.AgregationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "main_methods")
@RestController
@RequestMapping("/agregation")
@RequiredArgsConstructor
@Log4j2 // Аннотация для использования логирования
public class AgregationController {

    private final AgregationService agregationService;

    @Operation(summary = "Send aggregation data", description = "Handles aggregation data and returns the response.")
    @PostMapping(value = "/send", consumes = "multipart/form-data")
    public Object send(
            @RequestParam(value = "file") MultipartFile file,
            @Parameter(description = "ИИН/БИН") @RequestParam("participantId") String participantId,
            @Parameter(description = "Номер производственной линии") @RequestParam("productionLineId") String productionLineId,
            @Parameter(description = "Идентификатор производственного заказа") @RequestParam("productionOrderId") String productionOrderId,
            @Parameter(description = "Количество потребительских в группой упавоке") @RequestParam("stinsCount") int count,
            @Parameter(description = "OMS ID") @RequestParam("omsId") String omsId,
            @Parameter(description = "Client Token") @RequestParam("clientToken") String clientToken) {

        log.info("Получен запрос на отправку агрегированных данных от participantId={}, productionLineId={}, productionOrderId={}",
                participantId, productionLineId, productionOrderId);

        // Читаем данные из Excel
        List<ExcelDataDto> dataDtoList = agregationService.readSpecificColumns(file);
        log.debug("Данные из Excel успешно прочитаны: {}", dataDtoList);

        // Генерируем AggregationUnitDto
        List<AggregationUnitDto> aggregationUnitDtoList = agregationService.aggregationUnitDtoArrayList(dataDtoList, count);
        log.debug("Сформирован список AggregationUnitDto: {}", aggregationUnitDtoList);

        // Создаем AggregationDto
        AggregationDto aggregationDto = new AggregationDto(aggregationUnitDtoList, participantId, productionLineId, productionOrderId);
        log.debug("Создан AggregationDto: {}", aggregationDto);

        // Отправляем в метод
        Object response = agregationService.suzMethodAgregation(aggregationDto, omsId, clientToken);
        log.info("Ответ от suzMethodAgregation: {}", response);

        return response;
    }
}
