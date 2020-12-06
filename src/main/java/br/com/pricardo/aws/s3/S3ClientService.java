package br.com.pricardo.aws.s3;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static br.com.pricardo.aws.commons.util.FileUtils.convertMultiPartToFile;
import static java.util.Optional.ofNullable;

@Service
public class S3ClientService {

    private final AmazonS3 s3client;

    private final String bucketName;
    private final String accessKey;
    private final String secretKey;

    public S3ClientService(
            @Value("${aws.s3.bucketName}") String bucketName,
            @Value("${aws.s3.accessKey}") String accessKey,
            @Value("${aws.s3.secretKey}") String secretKey
    ) {
        this.bucketName = bucketName;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.s3client = initializeCredentials();

        if (!s3client.doesBucketExistV2(bucketName))
            throw new IllegalBucketNameException(String.format("Bucket %s already exists.", bucketName));
    }

    private AmazonS3 initializeCredentials() {
        final AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        return AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(Regions.SA_EAST_1)
                .build();
    }

    public S3ObjectData upload(MultipartFile multipartFile) {
        final LocalDateTime creatAt = LocalDateTime.now();

        String fileName = String.format("%s-%s",
                creatAt.format(DateTimeFormatter.ofPattern("ddMMyyyyHHmmss")),
                multipartFile.getOriginalFilename().replaceAll(" ", "_"));

        try {
            final File file = convertMultiPartToFile(multipartFile);
            s3client.putObject(new PutObjectRequest(bucketName, fileName, file));
        } catch (AmazonServiceException | IOException e) {
            // TODO: adicionar logger e remover sysout
            System.err.println(e.getMessage());
            System.exit(1);
        }

        return new S3ObjectData(fileName, bucketName, creatAt);
    }

    public List<S3ObjectData> consultar() {
        System.out.format("Objects in S3 bucket %s \n", bucketName);
        ListObjectsV2Result result = null;

        try {
            result = s3client.listObjectsV2(bucketName);
            // TODO: adicionar logger e remover sysout
            System.out.println(result);
        } catch (AmazonServiceException e) {
            // TODO: adicionar logger e remover sysout
            System.err.println(e.getMessage());
            System.exit(1);
        }

        return ofNullable(result).isPresent()
                ? result.getObjectSummaries().stream()
                    .map(S3ObjectData::from)
                    .collect(Collectors.toUnmodifiableList())
                : Collections.emptyList();
    }

    public byte[] download(String key) {
        byte[] fileDownload = new byte[1024];

        try {
            final S3Object s3Object = s3client.getObject(bucketName, key);
            fileDownload = IOUtils.toByteArray(s3Object.getObjectContent());
        } catch (AmazonServiceException | IOException e) {
            // TODO: adicionar logger e remover sysout
            System.err.println(e.getMessage());
            System.exit(1);
        }

        return fileDownload;
    }

    public String delete(String fileUrl) {
        String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
        s3client.deleteObject(new DeleteObjectRequest(bucketName, fileName));
        return "Successfully deleted";
    }
}