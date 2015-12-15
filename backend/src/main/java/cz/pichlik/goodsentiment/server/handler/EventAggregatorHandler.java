/*
 * Copyright 2015 the original author or authors. Licensed under the Apache License, Version 2.0 (the "License");
 */

package cz.pichlik.goodsentiment.server.handler;

import java.time.LocalDate;

import cz.pichlik.goodsentiment.server.ioc.ObjectFactory;

public class EventAggregatorHandler {
    private ObjectFactory objectFactory = new ObjectFactory();

    public void daily() {
        EventAggregator eventAggregator = objectFactory.eventAggregator();
        LocalDate now = LocalDate.now();
        eventAggregator.aggregateDay(now.getYear(), now.getMonthValue(), now.getDayOfMonth());
    }

    public void allData() {
        EventAggregator eventAggregator = objectFactory.eventAggregator();
        LocalDate from = LocalDate.of(2015, 12, 10); //this is a date when we got first data
        LocalDate to = LocalDate.now();
        eventAggregator.aggregateDay(from, to);
    }
}
