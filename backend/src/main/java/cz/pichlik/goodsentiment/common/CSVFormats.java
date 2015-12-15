/*
 * Copyright 2015 the original author or authors. Licensed under the Apache License, Version 2.0 (the "License");
 */

package cz.pichlik.goodsentiment.common;

import org.apache.commons.csv.CSVFormat;

public class CSVFormats {

    public static CSVFormat format(String... header) {
        CSVFormat csvFormat =
                CSVFormat
                .newFormat(',')
                .withRecordSeparator("\r\n")
                .withQuote('"')
                .withNullString("");
        return (header.length > 0) ? csvFormat.withHeader(header) : csvFormat;
    }
}
