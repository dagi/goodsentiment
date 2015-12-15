/*
 * Copyright 2015 the original author or authors. Licensed under the Apache License, Version 2.0 (the "License");
 */

package cz.pichlik.goodsentiment.server.resource.event;

import static cz.pichlik.goodsentiment.IOUtils.closeQuietly;
import static java.lang.String.format;
import static java.time.LocalDateTime.ofEpochSecond;
import static java.time.ZoneOffset.UTC;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.UUID;

import cz.pichlik.goodsentiment.common.CSVWriter;
import cz.pichlik.goodsentiment.server.repository.S3RepositoryBase;

public class S3VoteRepository implements VoteRepository{
    private static final boolean NOT_PUBLIC_READ = false;
    private static final String HEADERS[] = new String[]{"sentimentCode", "orgUnit", "latitude", "longitude", "city", "gender", "yearsInCompany","timestamp"};
    public final String bucket;
    private final S3RepositoryBase repoBase;

    public S3VoteRepository(S3RepositoryBase repoBase, String bucket) {
        this.repoBase = repoBase;
        this.bucket = bucket;
    }

    @Override
    public void save(VoteRequest request) {
        LinkedList<Object> row = new LinkedList<>();
        CSVWriter writer = null;
        try {
            writer = new CSVWriter(HEADERS);
            row.add(request.getSentimentCode());
            row.add(request.getOrgUnit());
            row.add(request.getLatitude());
            row.add(request.getLongitude());
            row.add(request.getCity());
            row.add(request.getGender());
            row.add(request.getYearsInCompany());
            row.add(request.getTimestamp());
            writer.writeRow(row);
            row.clear();
        } finally {
            closeQuietly(writer);
        }
        repoBase.save(bucket, getKey(request), writer.getTemporaryResultFile(), NOT_PUBLIC_READ);
    }

    private String getKey(VoteRequest request) {
        UUID fileName = UUID.randomUUID();
        LocalDateTime t = ofEpochSecond(request.getTimestamp(), 0, UTC);
        String key = format("%s/%s/%s/%s.csv", t.getYear(), t.getMonthValue(), t.getDayOfMonth(), fileName);
        return key;
    }

}
