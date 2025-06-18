package com.capturecat.client.upload;

import com.capturecat.client.upload.config.S3Properties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;

@Slf4j
@Component
@Profile({"dev", "prod"})
@RequiredArgsConstructor
public class S3FileUploader extends AbstractFileUploader {

    private final S3Client s3Client;
    private final S3Properties s3Properties;

    @Override
    public String upload(MultipartFile file) throws IOException {
        String key = buildKey(s3Properties.dirPrefix(), file);

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(s3Properties.bucket())
                .key(key)
                .contentType(file.getContentType())
                .build();

        try {
            s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));
        } catch (IOException e) {
            throw new UploadException(ErrorCode.S3_UPLOAD_FAILED_IO, e);
        } catch (SdkException e) {
            throw new UploadException(ErrorCode.S3_DOWNLOAD_FAILED_SDK, e);
        }

        return String.join("/", s3Properties.urlPrefix(), key);
    }


}
