/*
 * Copyright 2015 the original author or authors. Licensed under the Apache License, Version 2.0 (the "License");
 */

package cz.pichlik.goodsentiment.common;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.hasItemInArray;
import static org.junit.Assert.*;

import org.apache.commons.csv.CSVFormat;
import org.junit.Test;

public class CSVFormatsTest {

    @Test
    public void formatWithHeader() {
        CSVFormat format = CSVFormats.format("test");
        assertThat(format.getHeader(), hasItemInArray("test"));
    }

    @Test
    public void emptyHeader() {
        CSVFormat format = CSVFormats.format();
        assertThat(format.getHeader(), nullValue());
    }



}
