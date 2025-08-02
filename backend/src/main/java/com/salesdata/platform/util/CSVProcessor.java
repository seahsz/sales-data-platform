package com.salesdata.platform.util;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.salesdata.platform.fileupload.dto.CSVProcessingResult;
import com.salesdata.platform.fileupload.dto.SalesRecordCSV;
import com.salesdata.platform.salesdata.dto.SalesDataEntity;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CSVProcessor {

  public CSVProcessingResult processCSVFile(
      InputStream inputStream, Long userId, Long fileUploadId) {
    List<SalesDataEntity> successfulRecords = new ArrayList<>();
    List<String> errorMessages = new ArrayList<>();
    int totalRows = 0;

    log.info("Starting CSV processing for User: {}, FileUpload: {}", userId, fileUploadId);

    try (InputStreamReader reader = new InputStreamReader(inputStream)) {

      // Use OpenCSV to parse CSV with bean mapping
      CsvToBean<SalesRecordCSV> csvToBean =
          new CsvToBeanBuilder<SalesRecordCSV>(reader)
              .withType(SalesRecordCSV.class)
              .withIgnoreLeadingWhiteSpace(true)
              .withIgnoreEmptyLine(true)
              .build();

      Iterator<SalesRecordCSV> iterator = csvToBean.iterator();
      int lineNumber = 2; // Start at 2 because line 1 is header

      while (iterator.hasNext()) {
        totalRows++;

        try {
          SalesRecordCSV csvRecord = iterator.next();

          // Validate the record based on our custom validators
          csvRecord.validate();

          // Convert to entity and add to successful records
          SalesDataEntity salesDataEntity = csvRecord.toSalesDataEntity(userId, fileUploadId);
          successfulRecords.add(salesDataEntity);

        } catch (IllegalArgumentException e) {
          String errorMsg = String.format("Line %d: %s", lineNumber, e.getMessage());
          errorMessages.add(errorMsg);
          log.warn("Validation error on line {}: {}", lineNumber, e.getMessage());

        } catch (Exception e) {
          String errorMsg =
              String.format("Line %d: Unexpected error - %s", lineNumber, e.getMessage());
          errorMessages.add(errorMsg);
          log.error("Unexpected error processing line {}: {}", lineNumber, errorMsg, e);
        }

        lineNumber++;

        // Log progress for larger files
        if (totalRows % 1000 == 0) {
          log.info("Processed {} rows so far...", totalRows);
        }
      }
    } catch (IOException e) {
      log.error("Error reading CSV input stream", e);
      return new CSVProcessingResult(
          successfulRecords, errorMessages, totalRows, "Error reading CSV file: " + e.getMessage());

    } catch (Exception e) {
      log.error("Unexpected error processing CSV input stream", e);
      return new CSVProcessingResult(
          successfulRecords,
          errorMessages,
          totalRows,
          "Unexpected error processing CSV file: " + e.getMessage());
    }

    // Check if we have any data
    if (totalRows == 0) {
      log.warn("CSV file contains no data rows");
      return new CSVProcessingResult(
          successfulRecords, errorMessages, totalRows, "CSV file contains no data rows");
    }

    // Log final results
    log.info(
        "CSV processing completed. Total: {}, Success: {}, Failed: {}",
        totalRows,
        successfulRecords.size(),
        errorMessages.size());

    return new CSVProcessingResult(successfulRecords, errorMessages, totalRows, null);
  }

  /** Validate CSV file format by checking first few records Uses iterator for memory efficiency */
  public boolean isValidCSVFormat(InputStream inputStream) {
    try (InputStreamReader reader = new InputStreamReader(inputStream)) {

      CsvToBean<SalesRecordCSV> csvToBean =
          new CsvToBeanBuilder<SalesRecordCSV>(reader)
              .withType(SalesRecordCSV.class)
              .withIgnoreLeadingWhiteSpace(true)
              .withIgnoreEmptyLine(true)
              .build();

      // Use iterator to check only first few records
      Iterator<SalesRecordCSV> iterator = csvToBean.iterator();
      int checkCount = 0;

      while (iterator.hasNext() && checkCount < 5) {
        SalesRecordCSV csvRecord = iterator.next();
        csvRecord.validate();
        checkCount++;
      }

      return true;

    } catch (Exception e) {
      log.warn("CSV format validation failed - {}", e.getMessage());
      return false;
    }
  }

  /** Get row count efficiently using iterator */
  public int getRowCount(InputStream inputStream) {
    try (InputStreamReader reader = new InputStreamReader(inputStream)) {

      CsvToBean<SalesRecordCSV> csvToBean =
          new CsvToBeanBuilder<SalesRecordCSV>(reader)
              .withType(SalesRecordCSV.class)
              .withIgnoreLeadingWhiteSpace(true)
              .withIgnoreEmptyLine(true)
              .build();

      Iterator<SalesRecordCSV> iterator = csvToBean.iterator();
      int count = 0;

      while (iterator.hasNext()) {
        iterator.next();
        count++;
      }

      return count;

    } catch (Exception e) {
      log.error("Error counting rows in CSV", e);
      return 0;
    }
  }
}
