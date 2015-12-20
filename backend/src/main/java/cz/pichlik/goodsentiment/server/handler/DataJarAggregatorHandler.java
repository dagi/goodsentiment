/*
 * Copyright (C) 2007-2014, GoodData(R) Corporation. All rights reserved.
 */

package cz.pichlik.goodsentiment.server.handler;

import java.time.LocalDate;

import cz.pichlik.goodsentiment.server.ioc.ObjectFactory;

/**
 * The AWS lambda interface for DataJar aggregator
 */
public class DataJarAggregatorHandler {
    private final DataJarAggregator dataJarAggregator;

    public DataJarAggregatorHandler(){
        this(new ObjectFactory().dataJarAggregator());
    }

    public DataJarAggregatorHandler(DataJarAggregator dataJarAggregator) {
        super();
        this.dataJarAggregator = dataJarAggregator;
    }

    public void today() {
        LocalDate now = LocalDate.now();
        dataJarAggregator.countsForDay(now.getYear(), now.getMonthValue(), now.getDayOfMonth());
    }

}
