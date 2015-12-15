/*
 * Copyright 2015 the original author or authors. Licensed under the Apache License, Version 2.0 (the "License");
 */

package cz.pichlik.goodsentiment.server.repository;

import java.io.File;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;

public class S3RepositoryBase {
    private final AmazonS3Client s3Client;

    public S3RepositoryBase(AmazonS3Client s3Client) {
        super();
        this.s3Client = s3Client;
    }

    public void save(final String bucket, String key, File file, boolean publicRead) {
        PutObjectRequest req = new PutObjectRequest(bucket, key, file);
        if(publicRead) {
            req.withCannedAcl(CannedAccessControlList.PublicRead);
        }
        s3Client.putObject(req);
    }

    public InputStream load(String bucket, String key) {
        GetObjectRequest getReq = new GetObjectRequest(bucket, key);
        S3Object object = s3Client.getObject(getReq);
        return object.getObjectContent();
    }

    public List<String> list(final String bucket, final String key) {
        ListObjectsRequest listObjectsRequest =
                new ListObjectsRequest()
                    .withBucketName(bucket)
                    .withPrefix(key);

        ObjectListing objectListing = s3Client.listObjects(listObjectsRequest);
        LinkedList<String> result = new LinkedList<>();
        for(;;) {
            for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
                result.add(objectSummary.getKey().toString());
            }
            if(objectListing.isTruncated()) {
                objectListing = s3Client.listNextBatchOfObjects(objectListing);
            } else {
                return result;
            }
        }
    }
}
