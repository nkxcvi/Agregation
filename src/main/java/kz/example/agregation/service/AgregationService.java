package kz.example.agregation.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import kz.example.agregation.dto.postRequest.AggregationDto;
import kz.example.agregation.dto.postRequest.AggregationUnitDto;
import kz.example.agregation.dto.postRequest.ExcelDataDto;
import kz.example.agregation.dto.postResponce.AgregationResponceDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import static io.restassured.RestAssured.*;

import io.restassured.response.Response;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2 // Аннотация для использования логирования
@RequiredArgsConstructor
@Service
public class AgregationService {

    public Object send(AggregationDto aggregationDto, String omsId, String clientToken) {
        log.info("Начинается выполнение метода send с параметрами: omsId={}, clientToken={}", omsId, clientToken);

        Object response = suzMethodAgregation(aggregationDto, omsId, clientToken);

        log.info("Метод send завершён.");
        return response;
    }

    public Object suzMethodAgregation(AggregationDto aggregationDto, String omsId, String clientToken) {
        log.info("Отправка запроса в SUZ с omsId={} и clientToken={}", omsId, clientToken);

        // Логируем тело запроса
        log.debug("Тело запроса: {}", aggregationDto);

        // Установка базовых параметров для RestAssured
        baseURI = "https://suzcloud.stage.ismet.kz/api/v2/";
        basePath = "tobacco/aggregation";

        // Выполняем запрос
        Response response = given().log().all()
                .queryParam("omsId", omsId)
                .header("clientToken", clientToken)
                .header("Content-Type", "application/json")
                .body(aggregationDto)
                .when().post()
                .then()
                .extract().response();

        // Логируем ответ
        String place = response.asPrettyString();
        log.debug("Ответ от SUZ: {}", place);

        JsonObject jsonObject1 = JsonParser.parseString(place).getAsJsonObject();

        if (jsonObject1.has("omsId") || jsonObject1.has("reportId")) {
            log.info("Успешный ответ с omsId и reportId получен.");
            return new AgregationResponceDto("Агрегация успешно сформирована", jsonObject1.get("omsId").getAsString(), jsonObject1.get("reportId").getAsString());
        } else {
            log.warn("Ответ от SUZ не содержит ожидаемых данных.");
            return place;
        }
    }

    public List<AggregationUnitDto> aggregationUnitDtoArrayList(List<ExcelDataDto> dataDtoList, int count) {
        log.info("Начинается создание списка AggregationUnitDto, count={}", count);

        Map<String, List<String>> unitSerialNumberMap = new HashMap<>();

        for (ExcelDataDto data : dataDtoList) {
            String unitSerialNumber = data.getColumnB().substring(0, 25).replaceAll("\"", "\\\"");
            String sntin = data.getColumnA().substring(0, 21).replaceAll("\"", "\\\"");

            unitSerialNumberMap.computeIfAbsent(unitSerialNumber, k -> new ArrayList<>()).add(sntin);
        }

        log.debug("unitSerialNumberMap: {}", unitSerialNumberMap);

        List<AggregationUnitDto> aggregationUnitDtoList = new ArrayList<>();

        for (Map.Entry<String, List<String>> entry : unitSerialNumberMap.entrySet()) {
            AggregationUnitDto aggregationUnitDto = new AggregationUnitDto(
                    entry.getValue().size(),
                    "AGGREGATION",
                    count,
                    entry.getValue(),
                    entry.getKey()
            );

            aggregationUnitDtoList.add(aggregationUnitDto);
        }

        log.info("Создание списка AggregationUnitDto завершено.");
        return aggregationUnitDtoList;
    }

    public List<ExcelDataDto> readSpecificColumns(MultipartFile filePath) {
        log.info("Чтение данных из файла Excel: {}", filePath.getOriginalFilename());

        List<ExcelDataDto> dataList = new ArrayList<>();
        try (InputStream inputStream = filePath.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                Cell cellA = row.getCell(0);
                String cellAa = "";
                Cell cellB = row.getCell(1);
                String cellBb = "";

                if (cellA != null && cellA.getCellType() == CellType.STRING) {
                    cellAa = cellA.getStringCellValue();
                }
                if (cellB != null && cellB.getCellType() == CellType.STRING) {
                    cellBb = cellB.getStringCellValue();
                }

                dataList.add(new ExcelDataDto(cellAa, cellBb));
                log.debug("Прочитана строка - A: {}, B: {}", cellAa, cellBb);
            }

            log.info("Чтение данных из файла Excel завершено.");

        } catch (IOException e) {
            log.error("Ошибка при чтении файла Excel", e);
            throw new RuntimeException("Ошибка при чтении файла Excel", e);
        }

        return dataList;
    }
}
