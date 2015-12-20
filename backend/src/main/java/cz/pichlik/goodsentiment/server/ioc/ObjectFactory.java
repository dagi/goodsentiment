/*
 * Copyright 2015 the original author or authors. Licensed under the Apache License, Version 2.0 (the "License");
 */

package cz.pichlik.goodsentiment.server.ioc;

import java.time.LocalDate;

import com.amazonaws.services.s3.AmazonS3Client;

import cz.pichlik.goodsentiment.server.handler.DataJarAggregator;
import cz.pichlik.goodsentiment.server.handler.EventAggregator;
import cz.pichlik.goodsentiment.server.handler.EventDataReader;
import cz.pichlik.goodsentiment.server.repository.S3RepositoryBase;
import cz.pichlik.goodsentiment.server.resource.event.S3VoteRepository;

/**
 * A poor man's bean factory. We don't have Spring at the context of AWS Lambda
 * but still we should keep configuration and wiring separated.
 */
public class ObjectFactory {
    private final AmazonS3Client s3Client;
    private final S3RepositoryBase s3RepositoryBase;
    private final S3VoteRepository s3VoteRepository;
    private final EventAggregator eventAggregator;
    private final String eventDataBucket = "goodsentiment.event-data";
    private final String aggregatedDataBucket = "goodsentiment.aggregated-data";
    private final String integrationDataBucket = "goodsentiment.integration-data";
    private final EventDataReader eventDataReader;
    private final DataJarAggregator dataJarAggregator;
    private final LocalDate startingDay = LocalDate.of(2015, 12, 10); //the oldest day with events data

    public ObjectFactory() {
        this.s3Client = new AmazonS3Client();
        this.s3RepositoryBase = new S3RepositoryBase(this.s3Client);
        this.s3VoteRepository = new S3VoteRepository(this.s3RepositoryBase, eventDataBucket);
        this.eventDataReader = new EventDataReader(s3RepositoryBase, eventDataBucket);
        this.eventAggregator = new EventAggregator(aggregatedDataBucket, eventDataReader, s3RepositoryBase);
        this.dataJarAggregator = new DataJarAggregator(integrationDataBucket, s3RepositoryBase, eventDataReader);
    }

    public AmazonS3Client s3client() {
        return this.s3Client;
    }

    public S3RepositoryBase s3RepositoryBase() {
        return this.s3RepositoryBase;
    }

    public S3VoteRepository voteRepository() {
        return this.s3VoteRepository;
    }

    public EventAggregator eventAggregator() {
        return this.eventAggregator;
    }

    public LocalDate startingDay() {
        return this.startingDay;
    }

    public EventDataReader eventDataReader() {
        return this.eventDataReader;
    }

    public DataJarAggregator dataJarAggregator() {
        return this.dataJarAggregator;
    }
}
