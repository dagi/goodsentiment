/*
 * Copyright 2015 the original author or authors. Licensed under the Apache License, Version 2.0 (the "License");
 */

package cz.pichlik.goodsentiment.server.handler;

import static java.lang.String.format;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import cz.pichlik.goodsentiment.server.repository.S3RepositoryBase;

/**
 * Aggregates data for the <a href="">DataJar</a> - a famous hackathon project by Bob Koutsky
 */
public class DataJarAggregator {

    private static final String FILENAME = "datajar.txt";

    private static final Logger log = Logger.getLogger(DataJarAggregator.class);

    private final String integrationDataBucket;
    private final S3RepositoryBase s3RepositoryBase;
    private final EventDataReader eventDataReader;

    public DataJarAggregator(String integrationDataBucket, S3RepositoryBase s3RepositoryBase, EventDataReader eventDataReader) {
        super();
        this.integrationDataBucket = integrationDataBucket;
        this.s3RepositoryBase = s3RepositoryBase;
        this.eventDataReader = eventDataReader;
    }

    /**
     * Creates counts for the given day and persist them to a text file for the Datajar.
     */
     void countsForDay(int year, int month, int day) {
        final AtomicInteger happyCounter = new AtomicInteger(0);
        final AtomicInteger neutralCounter = new AtomicInteger(0);
        final AtomicInteger unhappyCounter = new AtomicInteger(0);
        computeCounts(happyCounter, neutralCounter, unhappyCounter, year, month, day);
        File tempDataFile;
        try {
            tempDataFile = writeToTempFile(happyCounter, unhappyCounter, neutralCounter);
        } catch (IOException e) {
            log.info("Cannot serialize counts to a temporary file", e);
            throw new RuntimeException(e);
        }
        log.info(format("Writing data to %s/%s", integrationDataBucket, FILENAME));
        s3RepositoryBase.save(integrationDataBucket, FILENAME, tempDataFile, true);
    }

    private File writeToTempFile(final AtomicInteger happyCounter, final AtomicInteger unhappyCounter, AtomicInteger neutralCounter) throws IOException {
        File tempDataFile = File.createTempFile("datajar", "txt");
        try (FileWriter writer = new FileWriter(tempDataFile)) {
            writer.write(format("happy=%s\n", happyCounter.get()));
            writer.write(format("unhappy=%s\n", unhappyCounter.get()));
            writer.write(format("neutral=%s\n", neutralCounter.get()));
        }
        return tempDataFile;
    }

    private void computeCounts(final AtomicInteger happyCounter, final AtomicInteger neutralCounter, final AtomicInteger unhappyCounter, int year, int month, int day) {
        eventDataReader.read(year, month, day, (r) -> {
            String sentimentCode = r.get(0);
            switch(sentimentCode) {
                case "100" : {
                    happyCounter.incrementAndGet();
                    break;
                }
                case "-100" : {
                    unhappyCounter.incrementAndGet();
                    break;
                }
                default: {
                    neutralCounter.incrementAndGet();
                }
            }
        });
    }
}
