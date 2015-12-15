/*
 * Copyright 2015 the original author or authors. Licensed under the Apache License, Version 2.0 (the "License");
 */

package cz.pichlik.goodsentiment.common;

import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

public class CSVWriter implements Closeable {
    private final CSVPrinter csvPrinter;
    private final File temporaryResultFile;

    public CSVWriter(String... header) {
        CSVFormat format = CSVFormats.format(header);
        try {
            this.temporaryResultFile = File.createTempFile("temp", "csv");
        } catch(IOException e) {
            throw new RuntimeException("Cannot create a temp file for merging CSV files", e);
        }
        try {
            csvPrinter = new CSVPrinter(new FileWriter(this.temporaryResultFile), format);
        } catch(IOException e) {
            throw new RuntimeException("Cannot initialize the CSV printer", e);
        }
    }

    public void writeRow(List<?> row) {
        try{
            csvPrinter.printRecord(row);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws IOException {
        this.csvPrinter.close();
    }

    public File getTemporaryResultFile() {
        return temporaryResultFile;
    }
}
