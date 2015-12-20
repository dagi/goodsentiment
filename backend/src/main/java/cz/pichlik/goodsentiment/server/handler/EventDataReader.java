/*
 * Copyright (C) 2007-2014, GoodData(R) Corporation. All rights reserved.
 */

package cz.pichlik.goodsentiment.server.handler;

import static java.lang.String.format;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

import org.apache.log4j.Logger;

import cz.pichlik.goodsentiment.common.CSVReader;
import cz.pichlik.goodsentiment.server.repository.S3RepositoryBase;

public class EventDataReader {
    private static final Logger log = Logger.getLogger(EventDataReader.class);
    private final S3RepositoryBase s3RepositoryBase;
    private final String eventDataBucket;

    public EventDataReader(S3RepositoryBase s3RepositoryBase, String eventDataBucket) {
        super();
        this.s3RepositoryBase = s3RepositoryBase;
        this.eventDataBucket = eventDataBucket;
    }

    /**
     * Reads all available vote events for the given day. An event is represented as
     * the list of strings. Every event is passed to the given consumer.
     */
    public void read(final int year, final int month,final int day, Consumer<List<String>> eventProcesseor) {
        String listKey = format("%s/%s/%s", year, month, day);
        log.info(format("Aggregate data for ", listKey));
        List<String> list = s3RepositoryBase.list(eventDataBucket, listKey);

        list.stream().forEach((key) -> {
            log.info(format("Loading file=%s", key));
            try (CSVReader reader = new CSVReader(s3RepositoryBase.load(eventDataBucket, key))) {
                reader.readLines((r, c) -> {
                    if(c == 0) { //skip the CSV header
                        return;
                    }
                    eventProcesseor.accept(r);
                });
            } catch(IOException e) {
                log.error(format("Cannot load file=%s", key));
            }
        });
    }
}
