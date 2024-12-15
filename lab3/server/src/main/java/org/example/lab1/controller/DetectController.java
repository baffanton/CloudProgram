package org.example.lab1.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.lab1.dto.Label;
import org.example.lab1.service.BucketService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/cv/detect")
@RequiredArgsConstructor
public class DetectController {
    private final BasicStroke stroke = new BasicStroke(5);
    private final RestTemplate restTemplate;
    private final BucketService bucketService;
    private final ObjectMapper objectMapper;
    @Value("${detect-url}")
    private String url;
    @Value("${cv.meta-info}")
    private String meta;

    @GetMapping("/{fileName}")
    public ResponseEntity<Void> detectOnImage(@PathVariable("fileName") String fileName) throws IOException {
        var file = this.bucketService.getFile(fileName);
        var extension = fileName.split("\\.")[1];
        var label = this.detectOnImage(file);
        var result = this.drawDetect(file, label.coordination(), extension);
        this.bucketService.uploadFile(result, this.imageName(label, extension));

        return ResponseEntity.accepted().build();
    }

    private Label detectOnImage(ByteArrayResource file) throws JsonProcessingException {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        var map = new LinkedMultiValueMap<String, Object>();
        map.add("file", file);
        map.add("meta", this.meta);

        var request = new HttpEntity<>(map, headers);
        var response = restTemplate.postForEntity(
                this.url,
                request,
                String.class
        );

        var node = objectMapper.readTree(response.getBody());
        var labelData = node
                .get("body")
                .get("object_labels")
                .get(0)
                .get("labels")
                .get(0);

        return objectMapper.treeToValue(labelData, Label.class);
    }

    private byte[] drawDetect(ByteArrayResource file, List<Integer> measures, String extension) throws IOException {
        var image = ImageIO.read(file.getInputStream());
        var graphics = image.createGraphics();

        graphics.setColor(Color.RED);
        graphics.setStroke(stroke);
        graphics.drawRect(
                measures.get(0),
                measures.get(1),
                measures.get(2),
                measures.get(3)
        );
        graphics.dispose();

        var outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, extension, outputStream);

        return outputStream.toByteArray();
    }

    private String imageName(Label label, String extension) {
        return String.format("%s-detected.%s", label.name(), extension);
    }
}
