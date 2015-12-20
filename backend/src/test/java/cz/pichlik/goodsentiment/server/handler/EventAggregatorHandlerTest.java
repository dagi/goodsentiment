/*
 * Copyright (C) 2007-2014, GoodData(R) Corporation. All rights reserved.
 */

package cz.pichlik.goodsentiment.server.handler;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;

import org.junit.Before;
import org.junit.Test;

public class EventAggregatorHandlerTest {
    private final EventAggregator eventAggregator = mock(EventAggregator.class);
    private LocalDate startingDay = LocalDate.now().minusDays(1);
    private EventAggregatorHandler handler;

    @Before
    public void setUp() throws Exception {
        handler = new EventAggregatorHandler(eventAggregator, startingDay);
    }

    @Test
    public void testDaily() {
        handler.daily();
        LocalDate now = LocalDate.now();
        verify(eventAggregator).aggregateDay(eq(now.getYear()), eq(now.getMonthValue()), eq(now.getDayOfMonth()));
    }

    @Test
    public void testAllData() {
        handler.allData();
        verify(eventAggregator).aggregateDay(eq(startingDay), any(LocalDate.class));
    }

}
