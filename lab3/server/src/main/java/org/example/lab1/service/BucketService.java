package org.example.lab1.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.utils.IoUtils;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BucketService {
    private final S3Client client;
    @Value("${bucket-name}")
    private String bucketName;


    public List<String> getListFiles() {
        var request = ListObjectsV2Request
                .builder()
                .bucket(this.bucketName)
                .build();
        var response = this.client.listObjectsV2Paginator(request);

        return response
                .stream()
                .map(ListObjectsV2Response::contents)
                .flatMap(List::stream)
                .map(S3Object::key)
                .toList();
    }

    public ByteArrayResource getFile(String key) throws IOException {
        var request = GetObjectRequest
                .builder()
                .bucket(this.bucketName)
                .key(key)
                .build();

        var responseInputStream = client.getObject(request);

        return new ByteArrayResource(IoUtils.toByteArray(responseInputStream)) {
            @Override
            public String getFilename() {
                return key;
            }
        };
    }

    public void uploadFile(MultipartFile file) throws IOException {
        var request = PutObjectRequest.builder()
                .bucket(this.bucketName)
                .key(file.getOriginalFilename())
                .build();

        this.client.putObject(request, RequestBody.fromBytes(file.getBytes()));
    }

    public void uploadFile(byte[] file, String fileName) throws IOException {
        var request = PutObjectRequest.builder()
                .bucket(this.bucketName)
                .key(fileName)
                .build();

        this.client.putObject(request, RequestBody.fromBytes(file));
    }

    public void deleteFile(String fileName) {
        var request = DeleteObjectRequest.builder()
                .bucket(this.bucketName)
                .key(fileName)
                .build();

        this.client.deleteObject(request);
    }
}
