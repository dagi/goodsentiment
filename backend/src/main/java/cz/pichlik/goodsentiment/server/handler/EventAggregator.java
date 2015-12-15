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

import cz.pichlik.goodsentiment.common.CSVReader;
import cz.pichlik.goodsentiment.common.CSVWriter;
import cz.pichlik.goodsentiment.server.repository.S3RepositoryBase;

public class EventAggregator {
    private static final boolean PUBLIC_READ = true;

    private static final Logger log = Logger.getLogger(EventAggregator.class);

    private static final String CSV_HEADERS[] = new String[]{"id", "sentimentCode", "orgUnit", "latitude", "longitude", "city", "gender", "yearsInCompany","timestamp"};

    private final String eventDataBucket;
    private final String aggregatedDatabucket;
    private final S3RepositoryBase s3RepositoryBase;
    private long seed = System.currentTimeMillis(); //theoretically not safe

    public EventAggregator(String aggregatedDatabucket, String eventDataBucket, S3RepositoryBase s3RepositoryBase) {
        super();
        this.eventDataBucket = eventDataBucket;
        this.aggregatedDatabucket = aggregatedDatabucket;
        this.s3RepositoryBase = s3RepositoryBase;
    }

    /**
     * Aggregates the data between two dates (inclusive). Events for single day
     * are aggregated separately.
     */
    public void aggregateDay(LocalDate from, LocalDate to) {
        LocalDate date = from;
        while(date.isBefore(to) || date.isEqual(to)) {
            aggregateDay(date.getYear(), date.getMonthValue(), date.getDayOfMonth());
            date = date.plusDays(1);
        }
    }

    /**
     * Aggregates the data for single day.
     */
    public void aggregateDay(int year, int month, int day) {
        String listKey = format("%s/%s/%s", year, month, day);
        log.info(format("Aggregate data for ", listKey));
        List<String> list = s3RepositoryBase.list(eventDataBucket, listKey);
        AtomicLong idSeed = new AtomicLong(seed);
        File tempResultFile = null;
        try(CSVWriter writer = new CSVWriter(CSV_HEADERS)) {
            list.stream().forEach((key) -> {
                log.info(format("Loading file=%s", key));
                try (CSVReader reader = new CSVReader(s3RepositoryBase.load(eventDataBucket, key))) {
                    reader.readLines((row, rowNum) -> {
                        if(rowNum == 0) { //skip the CSV header row
                            return;
                        }
                        addRowId(idSeed, row);
                        convertTimestampToDate(row);
                        writer.writeRow(row);
                    });
                } catch(IOException e) {
                    log.error(format("Cannot load file=%s", key));
                }
            });
            tempResultFile = writer.getTemporaryResultFile();
        } catch(IOException e) {
            log.error(format("Cannot initialize CSV writer for aggregated result"), e);
            throw new RuntimeException(e);
        }
        log.info(format("Total count=%s of events", idSeed.get() - seed));
        String key = format("goodsentinment-data-%s-%s-%s.csv", year, month, day);
        log.info(format("Writing result to file=%s/%s", aggregatedDatabucket,key));
        s3RepositoryBase.save(aggregatedDatabucket, key, tempResultFile, PUBLIC_READ);
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
