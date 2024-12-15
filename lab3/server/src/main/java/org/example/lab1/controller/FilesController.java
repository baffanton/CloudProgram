package org.example.lab1.controller;


import lombok.RequiredArgsConstructor;
import org.example.lab1.service.BucketService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/s3/files")
@RequiredArgsConstructor
public class FilesController {
    private final BucketService bucketService;

    @GetMapping()
    public ResponseEntity<List<String>> getListFiles() {
        return ResponseEntity.ok(this.bucketService.getListFiles());
    }

    @GetMapping("/{fileName}")
    public ResponseEntity<Resource> getFile(@PathVariable("fileName") String fileName) throws IOException {
        var byteArray = this.bucketService.getFile(fileName);

        var headers = new HttpHeaders();
        headers.add(
                HttpHeaders.CONTENT_DISPOSITION,
                String.format("attachment; filename=\"%s\"", fileName)
        );

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(byteArray.contentLength())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(byteArray);
    }

    @DeleteMapping("/{fileName}")
    public ResponseEntity<Void> deleteFile(@PathVariable("fileName") String fileName) {
        this.bucketService.deleteFile(fileName);

        return ResponseEntity.accepted().build();
    }

    @PostMapping("/upload")
    public ResponseEntity<Void> uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
        this.bucketService.uploadFile(file);

        return ResponseEntity.accepted().build();
    }
}
