/*
 * Copyright 2015 the original author or authors. Licensed under the Apache License, Version 2.0 (the "License");
 */

package cz.pichlik.goodsentiment.server.handler;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.endsWith;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import java.io.File;
import java.io.FileReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.io.IOUtils;
import org.hamcrest.core.StringEndsWith;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import cz.pichlik.goodsentiment.server.repository.S3RepositoryBase;

@SuppressWarnings("unchecked")
public class EventAggregatorTest {

    private static final String AGGREGATED_BUCKET = "aggregatedDatabucket";
    private EventAggregator eventAggregator;
    private S3RepositoryBase s3Repo = mock(S3RepositoryBase.class);
    private EventDataReader eventDataReader = mock(EventDataReader.class);

    @Before
    public void setUp() throws Exception {
        this.eventAggregator = new EventAggregator(AGGREGATED_BUCKET, eventDataReader, s3Repo);
        this.eventAggregator.setSeed(0);
        doAnswer((i) -> {
            Object[] arguments = i.getArguments();
            Consumer<List<String>> callback = (Consumer<List<String>>) arguments[3];
            callback.accept(new ArrayList<>(asList("0","DevOps","49.2","16.6167","Brno","Male","6","1449770125")));
            return null;
        }).when(eventDataReader).read(anyInt(), anyInt(), anyInt(), any(Consumer.class));
     }

    @Test
    public void resultCSVHasHeader() throws Exception{
        File resultFile = aggregateSingle();
        try(FileReader fr = new FileReader(resultFile)) {
            List<String> result = IOUtils.readLines(fr);
            assertThat("Missing the CSV header", result, hasItem("id,sentimentCode,orgUnit,latitude,longitude,city,gender,yearsInCompany,timestamp"));
        }
    }

    @Test
    public void idIsPresent() throws Exception{
        File resultFile = aggregateSingle();
        try(FileReader fr = new FileReader(resultFile)) {
            List<String> result = IOUtils.readLines(fr);
            assertThat(result, hasItems(startsWith("0")));
        }
    }

    @Test
    public void timestampIsDate() throws Exception{
        File resultFile = aggregateSingle();
        try(FileReader fr = new FileReader(resultFile)) {
            List<String> result = IOUtils.readLines(fr);
            assertThat(result, hasItems(StringEndsWith.endsWith("2015-12-10T17:55:25")));
        }
    }

    @Test
    public void fileHasPublicRead() {
        aggregateSingle();
        verify(s3Repo).save(eq(AGGREGATED_BUCKET), anyString(), any(File.class), eq(true));
    }

    @Test
    public void filenamEndsWithCSV() {
        aggregateSingle();
        verify(s3Repo).save(eq(AGGREGATED_BUCKET), endsWith(".csv"), any(File.class), any(Boolean.class));
    }

    @Test
    public void resultHasData() throws Exception{
        File resultFile = aggregateSingle();
        try(FileReader fr = new FileReader(resultFile)) {
            List<String> result = IOUtils.readLines(fr);
            assertThat(result, hasItems(containsString("0,DevOps,49.2,16.6167,Brno,Male,6")));
        }
    }

    @Test
    public void aggregateMoreDays() throws Exception{
        LocalDate from = LocalDate.of(2015, 9, 1);
        LocalDate to = LocalDate.of(2015, 9, 2);
        eventAggregator.aggregateDay(from, to);
        verify(eventDataReader).read(eq(2015), eq(9), eq(1), any(Consumer.class));
        verify(eventDataReader).read(eq(2015), eq(9), eq(2), any(Consumer.class));
    }

    private File aggregateSingle() {
        ArgumentCaptor<File> resultFile = ArgumentCaptor.forClass(File.class);
        eventAggregator.aggregateDay(2015, 12, 14);
        verify(s3Repo).save(eq(AGGREGATED_BUCKET), anyString(), resultFile.capture(), anyBoolean());
        return resultFile.getValue();
    }

}
