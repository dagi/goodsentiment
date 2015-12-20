/*
 * Copyright 2015 the original author or authors. Licensed under the Apache License, Version 2.0 (the "License");
 */

package cz.pichlik.goodsentiment.server.handler;

import java.time.LocalDate;

import cz.pichlik.goodsentiment.server.ioc.ObjectFactory;

/**
 * The AWS lambda function interface for the daily aggregator. It supports
 * aggregation for the current day or all dates.
 */
public class EventAggregatorHandler {

    private final EventAggregator eventAggregator;
    private final LocalDate startingDay;

    public EventAggregatorHandler() {
        ObjectFactory objectFactory = new ObjectFactory();
        this.eventAggregator = objectFactory.eventAggregator();
        this.startingDay = objectFactory.startingDay();
    }

    public EventAggregatorHandler(EventAggregator eventAggregator, LocalDate startingDay) {
        super();
        this.eventAggregator = eventAggregator;
        this.startingDay = startingDay;
    }

    public void daily() {
        LocalDate now = LocalDate.now();
        eventAggregator.aggregateDay(now.getYear(), now.getMonthValue(), now.getDayOfMonth());
    }

    public void allData() {
        //this is a date when we got first data
        LocalDate to = LocalDate.now();
        eventAggregator.aggregateDay(startingDay, to);
    }
}
