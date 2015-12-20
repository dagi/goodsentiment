/*
 * Copyright 2015 the original author or authors. Licensed under the Apache License, Version 2.0 (the "License");
 */

package cz.pichlik.goodsentiment.server.handler;

import static java.lang.String.format;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;

import cz.pichlik.goodsentiment.common.CSVWriter;
import cz.pichlik.goodsentiment.server.repository.S3RepositoryBase;

/**
 * Aggregates event data on the daily bases.
 */
public class EventAggregator {
    private static final boolean PUBLIC_READ = true;

    private static final Logger log = Logger.getLogger(EventAggregator.class);

    private static final String CSV_HEADERS[] = new String[]{"id", "sentimentCode", "orgUnit", "latitude", "longitude", "city", "gender", "yearsInCompany","timestamp"};

    private final String aggregatedDatabucket;
    private final S3RepositoryBase s3RepositoryBase;
    private final EventDataReader eventDataReader;
    private long seed = System.currentTimeMillis(); //theoretically not safe

    public EventAggregator(String aggregatedDatabucket, EventDataReader eventDataReader, S3RepositoryBase s3RepositoryBase) {
        super();
        this.eventDataReader = eventDataReader;
        this.aggregatedDatabucket = aggregatedDatabucket;
        this.s3RepositoryBase = s3RepositoryBase;
    }

    /**
     * Aggregates the data between two dates (inclusive). Events for single day
     * are aggregated separately.
     */
    void aggregateDay(LocalDate from, LocalDate to) {
        log.info(format("Aggregates data from=%s to=%s", from, to));
        LocalDate date = from;
        while(date.isBefore(to) || date.isEqual(to)) {
            aggregateDay(date.getYear(), date.getMonthValue(), date.getDayOfMonth());
            date = date.plusDays(1);
        }
    }

    /**
     * Aggregates the data for single day.
     */
    void aggregateDay(int year, int month, int day) {
        log.info(format("Aggregates data %s/%s/%s", year, month, day));
        AtomicLong idSeed = new AtomicLong(seed);
        File tempResultFile = null;
        try(CSVWriter writer = new CSVWriter(CSV_HEADERS)) {
            eventDataReader.read(year, month, day, (row) -> {
                addRowId(idSeed, row);
                convertTimestampToDate(row);
                writer.writeRow(row);
            });
            tempResultFile = writer.getTemporaryResultFile();
        } catch(IOException e) {
            log.error(format("Cannot initialize CSV writer for aggregated result"), e);
            throw new RuntimeException(e);
        }
        log.info(format("Total count=%s of events", idSeed.get() - seed));
        String key = aggregatedDataKey(year, month, day);
        log.info(format("Writing result to file=%s/%s", aggregatedDatabucket,key));
        s3RepositoryBase.save(aggregatedDatabucket, key, tempResultFile, PUBLIC_READ);
    }

    private String aggregatedDataKey(int year, int month, int day) {
        String key = format("goodsentinment-data-%s-%s-%s.csv", year, month, day);
        return key;
    }

    private void convertTimestampToDate(List<String> row) {
        int lastColumnIndex = row.size() - 1;
        String timestamp = row.remove(lastColumnIndex);
        row.add(LocalDateTime.ofEpochSecond(Long.valueOf(timestamp), 0, ZoneOffset.UTC).toString());
    }

    private void addRowId(AtomicLong idSeed, List<String> row) {
        row.add(0, Long.toString(idSeed.getAndIncrement()));
    }

    /**
     * For tests only
     */
    void setSeed(long seed) {
        this.seed = seed;
    }
}
