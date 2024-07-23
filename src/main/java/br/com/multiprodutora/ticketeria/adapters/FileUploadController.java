package br.com.multiprodutora.ticketeria.adapters;

import br.com.multiprodutora.ticketeria.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class FileUploadController {

    @Autowired
    private StorageService storageService;

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            String filePath = storageService.store(file);
            Map<String, String> response = new HashMap<>();
            response.put("filePath", filePath);
            HttpHeaders headers = new HttpHeaders();
            headers.set("ngrok-skip-browser-warning", "true");
            return ResponseEntity.ok().headers(headers).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

