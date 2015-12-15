/*
 * Copyright 2015 the original author or authors. Licensed under the Apache License, Version 2.0 (the "License");
 */

package cz.pichlik.goodsentiment.common;

import static cz.pichlik.goodsentiment.IOUtils.closeQuietly;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CSVReaderTest {

    private InputStream csvFile1;
    private InputStream csvFile2;

    @After
    public void tearDown() throws Exception {
        closeQuietly(csvFile1);
        closeQuietly(csvFile2);
    }

    @Before
    public void setUp() throws Exception {
        this.csvFile1 = getClass().getClassLoader().getResourceAsStream("goodsentinment-data-2015-09-01.csv");
    }

    @Test
    public void csvHeader() throws IOException{
        final List<String> result = readData();
        assertThat("Missing CSV header",result, hasItems("id","sentimentCode","orgUnit","latitude","longitude","city","gender","yearsInCompany","timestamp"));
    }

    @Test
    public void dataRow() throws IOException{
        final List<String> result = readData();
        assertThat("Missing row data",result, hasItems("0","0","DevOps","49.2","16.6167","Brno","Male","6","2015-09-01T15:53:39"));
    }

    @Test
    public void allDataRead() throws IOException{
        final List<String> result = readData();
        assertThat("Not all lines were read",result.size(), is(18));
    }

    private List<String> readData() throws IOException {
        final List<String> result = new ArrayList<>();
        try(CSVReader csvReader = new CSVReader(csvFile1)) {
            csvReader.readLines((r,c) -> {
                result.addAll(r);
            });
        }
        return result;
    }
}
