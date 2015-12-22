/*
 * Copyright (C) 2007-2014, GoodData(R) Corporation. All rights reserved.
 */

package cz.pichlik.goodsentiment.server.handler;

import static java.util.Collections.singletonList;
import static org.apache.commons.io.FileUtils.readLines;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.function.Consumer;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import cz.pichlik.goodsentiment.server.repository.S3RepositoryBase;

public class DataJarAggregatorTest {
    private static final int DAY = 1;
    private static final int MONTH = 12;
    private static final int YEAR = 2015;
    private String BUCKET = "bucket";
    private S3RepositoryBase s3RepositoryBase = mock(S3RepositoryBase.class);
    private EventDataReader eventDataReader = mock(EventDataReader.class);

    private DataJarAggregator dataJarAggregator =
            new DataJarAggregator(BUCKET, s3RepositoryBase, eventDataReader);

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() throws Exception {
        doAnswer((i) -> {
            Object[] arguments = i.getArguments();
            Consumer<List<String>> eventProcesseor = (Consumer<List<String>>) arguments[3];
            eventProcesseor.accept(singletonList("100"));
            eventProcesseor.accept(singletonList("100"));
            eventProcesseor.accept(singletonList("100"));
            eventProcesseor.accept(singletonList("-100"));
            eventProcesseor.accept(singletonList("-100"));
            eventProcesseor.accept(singletonList("0"));
            return null;
        }).when(eventDataReader).read(eq(YEAR), eq(MONTH), eq(DAY), any(Consumer.class));
    }

    @Test
    public void countsForDay() throws IOException {
        dataJarAggregator.countsForDay(YEAR, MONTH, DAY);
        ArgumentCaptor<File> fileCaptor = forClass(File.class);
        verify(s3RepositoryBase).save(eq(BUCKET), anyString(), fileCaptor.capture(), eq(true));
        List<String> resultString =
                readLines(fileCaptor.getValue(), Charset.defaultCharset());
        assertThat(resultString, hasItems("3", "2", "1"));
        assertThat(resultString.get(0), is("3"));
        assertThat(resultString.get(1), is("2"));
        assertThat(resultString.get(2), is("1"));
    }

}
