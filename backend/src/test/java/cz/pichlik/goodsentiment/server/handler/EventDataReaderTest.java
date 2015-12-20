/*
 * Copyright (C) 2007-2014, GoodData(R) Corporation. All rights reserved.
 */

package cz.pichlik.goodsentiment.server.handler;

import static cz.pichlik.goodsentiment.IOUtils.closeQuietly;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import cz.pichlik.goodsentiment.server.repository.S3RepositoryBase;

public class EventDataReaderTest {
    private static final String EVENT_BUCKET = "eventDataBucket";

    private S3RepositoryBase s3Repo = mock(S3RepositoryBase.class);
    private InputStream csvFile1;
    private InputStream csvFile2;
    private EventDataReader eventDataReader;

    @Before
    public void setUp() throws Exception {
        this.csvFile1 = getClass().getClassLoader().getResourceAsStream("01e97d6d-1925-46c0-8b8c-51e024ddde7b.csv");
        this.csvFile2 = getClass().getClassLoader().getResourceAsStream("07965a5b-f36a-46a9-8580-4827583791b2.csv");
        String firstFile = "csvFile1";
        String secondFile = "csvFile2";
        when(s3Repo.list(eq(EVENT_BUCKET), eq("2015/12/14"))).thenReturn(asList(firstFile, secondFile));
        when(s3Repo.load(eq(EVENT_BUCKET), eq(firstFile))).thenReturn(csvFile1);
        when(s3Repo.load(eq(EVENT_BUCKET), eq(secondFile))).thenReturn(csvFile2);
        eventDataReader = new EventDataReader(s3Repo, EVENT_BUCKET);
    }

    @After
    public void tearDown() {
        closeQuietly(csvFile1);
        closeQuietly(csvFile2);
    }

    @Test
    public void testRead() {
        List<List<String>> allValues = new ArrayList<>();
        eventDataReader.read(2015, 12, 14, (r) -> { allValues.add(new ArrayList<>(r));});
        assertThat(allValues, hasItem(asList("0","DevOps","49.2","16.6167","Brno","Male","6","1449770125")));
        assertThat(allValues, hasItem(asList("-100","Marketing","37.7833","-122.4167","San Francisco","Female","4","1449765618")));
    }

}
