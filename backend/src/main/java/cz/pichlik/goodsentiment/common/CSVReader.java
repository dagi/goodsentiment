/*
 * Copyright 2015 the original author or authors. Licensed under the Apache License, Version 2.0 (the "License");
 */

package cz.pichlik.goodsentiment.common;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.csv.CSVParser;

public class CSVReader implements Closeable{
    private CSVParser csvParser;

    public CSVReader(InputStream input) {
        super();
        try {
            this.csvParser = new CSVParser(new InputStreamReader(input), CSVFormats.format());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void readLines(RowProcessor processor) {
        ArrayList<String> val = new ArrayList<>();
        AtomicInteger counter = new AtomicInteger(0);
        csvParser.forEach((r) -> {
            try {
            r.forEach((s) -> val.add(s));
            processor.accept(val, counter.getAndIncrement());
            } finally {
                val.clear();
            }
        }
        );
    }

    public interface RowProcessor {
        /**
         * Process a row.
         * @param row the row
         * @param rowNum  the number of the current row (starts from 0 - the first line)
         */
        public void accept(List<String> row, int rowNum);
    }

    @Override
    public void close() throws IOException {
        this.csvParser.close();
    }
}
