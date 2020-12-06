package br.com.pricardo.aws.s3;

import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class S3ObjectData implements Serializable {

    private static final long serialVersionUID = -1252860467483935248L;

    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd HH:mm:ss"
    )
    private final LocalDateTime lastModification;

    private String bucketName;
    private String name;

    public S3ObjectData(
            String name,
            String bucketName,
            LocalDateTime lastModification
    ) {
        this.lastModification = lastModification;
        this.bucketName = bucketName;
        this.name = name;
    }

    public LocalDateTime getLastModification() {
        return lastModification;
    }

    public String getBucketName() {
        return bucketName;
    }

    public String getName() {
        return name;
    }

    public static S3ObjectData from(S3ObjectSummary s3ObjectSummary) {
        return new S3ObjectData(
                s3ObjectSummary.getKey(),
                s3ObjectSummary.getBucketName(),
                Instant.ofEpochMilli(s3ObjectSummary.getLastModified().getTime())
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime());
    }
}
