package br.com.multiprodutora.ticketeria.application.upload.controller;

import br.com.multiprodutora.ticketeria.application.upload.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/api")
public class FileUploadController {


    private static final Logger log = LoggerFactory.getLogger(FileUploadController.class);

    @Autowired
    private StorageService storageService;


    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            String filePath = storageService.store(file);
            Map<String, String> response = new HashMap<>();
            response.put("filePath", filePath);
            log.info("Upload realizado com sucesso: {} = {}", response, file.getResource());
            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            log.error("Ocorreu ao fazer um upload na imagem: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

