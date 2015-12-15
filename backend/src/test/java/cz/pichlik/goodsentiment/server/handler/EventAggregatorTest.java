/*
 * Copyright 2015 the original author or authors. Licensed under the Apache License, Version 2.0 (the "License");
 */

package cz.pichlik.goodsentiment.server.handler;

import static cz.pichlik.goodsentiment.IOUtils.closeQuietly;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.endsWith;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.hamcrest.core.StringEndsWith;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import cz.pichlik.goodsentiment.server.repository.S3RepositoryBase;

@SuppressWarnings("unchecked")
public class EventAggregatorTest {
    private static final String EVENT_BUCKET = "eventDataBucket";
    private static final String AGGREGATED_BUCKET = "aggregatedDatabucket";
    private EventAggregator eventAggregator;
    private S3RepositoryBase s3Repo = mock(S3RepositoryBase.class);
    private InputStream csvFile1;
    private InputStream csvFile2;

    @Before
    public void setUp() throws Exception {
        this.eventAggregator = new EventAggregator(AGGREGATED_BUCKET, EVENT_BUCKET, s3Repo);
        this.eventAggregator.setSeed(0);
        this.csvFile1 = getClass().getClassLoader().getResourceAsStream("01e97d6d-1925-46c0-8b8c-51e024ddde7b.csv");
        this.csvFile2 = getClass().getClassLoader().getResourceAsStream("07965a5b-f36a-46a9-8580-4827583791b2.csv");
        String firstFile = "csvFile1";
        String secondFile = "csvFile2";
        when(s3Repo.list(eq(EVENT_BUCKET), eq("2015/12/14"))).thenReturn(asList(firstFile, secondFile));
        when(s3Repo.load(eq(EVENT_BUCKET), eq(firstFile))).thenReturn(csvFile1);
        when(s3Repo.load(eq(EVENT_BUCKET), eq(secondFile))).thenReturn(csvFile2);
    }

    @After
    public void tearDown() {
        closeQuietly(csvFile1);
        closeQuietly(csvFile2);
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
            assertThat(result, hasItems(startsWith("1")));
        }
    }

    @Test
    public void timestampIsDate() throws Exception{
        File resultFile = aggregateSingle();
        try(FileReader fr = new FileReader(resultFile)) {
            List<String> result = IOUtils.readLines(fr);
            assertThat(result, hasItems(StringEndsWith.endsWith("2015-12-10T17:55:25")));
            assertThat(result, hasItems(StringEndsWith.endsWith("2015-12-10T16:40:18")));
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
            assertThat(result, hasItems(containsString("-100,Marketing,37.7833,-122.4167,San Francisco,Female,4")));
        }
    }

    @Test
    public void aggregateMoreDays() throws Exception{
        LocalDate from = LocalDate.of(2015, 9, 1);
        LocalDate to = LocalDate.of(2015, 9, 2);
        eventAggregator.aggregateDay(from, to);
        verify(s3Repo).list(eq(EVENT_BUCKET), eq("2015/9/1"));
        verify(s3Repo).list(eq(EVENT_BUCKET), eq("2015/9/2"));
    }

    private File aggregateSingle() {
        ArgumentCaptor<File> resultFile = ArgumentCaptor.forClass(File.class);
        eventAggregator.aggregateDay(2015, 12, 14);
        verify(s3Repo).save(eq(AGGREGATED_BUCKET), anyString(), resultFile.capture(), anyBoolean());
        return resultFile.getValue();
    }

}
