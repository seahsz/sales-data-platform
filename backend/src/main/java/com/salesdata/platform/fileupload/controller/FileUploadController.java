package com.salesdata.platform.fileupload.controller;

import com.salesdata.platform.fileupload.FileUploadService;
import com.salesdata.platform.util.CSVProcessor;
import com.salesdata.platform.util.JwtUtil;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "http://localhost:3000")
public class FileUploadController {

  private final FileUploadService fileUploadService;
  private final CSVProcessor csvProcessor;
  private final JwtUtil jwtUtil;

  /** Upload a CSV file */
  @PostMapping("/upload")
  public ResponseEntity<Map<String, Object>> uploadFile(
      @RequestParam("file") MultipartFile file, @RequestHeader("Authorization") String authHeader) {

    return null;
  }
}
