/*
 * Copyright (C) 2007-2014, GoodData(R) Corporation. All rights reserved.
 */

package cz.pichlik.goodsentiment.server.handler;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import org.junit.Test;

public class DataJarAggregatorHandlerTest {
    private DataJarAggregator dataJarAggregator = mock(DataJarAggregator.class);
    private DataJarAggregatorHandler handler = new DataJarAggregatorHandler(dataJarAggregator);

    @Test
    public void testToday() {
          handler.today();
          verify(dataJarAggregator).countsForDay(anyInt(), anyInt(), anyInt());
    }

}
