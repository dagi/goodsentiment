/*
 * Copyright 2015 the original author or authors. Licensed under the Apache License, Version 2.0 (the "License");
 */

package cz.pichlik.goodsentiment.common;

import static java.util.Arrays.asList;
import static org.apache.commons.io.FileUtils.readLines;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

public class CSVWriterTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void writeHeader() throws IOException{
        CSVWriter writer = new CSVWriter("aaa", "bbb");
        writer.close();
        File resultFile = writer.getTemporaryResultFile();
        List<String> result = readLines(resultFile);
        assertThat("Missin header in the CSV file", result, hasItems("aaa,bbb"));
    }

    @Test
    public void writeRow() throws IOException{
        CSVWriter writer = new CSVWriter();
        writer.writeRow(asList(1,2));
        writer.writeRow(asList(3,4));
        writer.close();
        File resultFile = writer.getTemporaryResultFile();
        List<String> result = FileUtils.readLines(resultFile);
        assertThat("Missing row", result, hasItems("1,2"));
    }

}
