/*
 * Copyright 2015 the original author or authors. Licensed under the Apache License, Version 2.0 (the "License");
 */

package cz.pichlik.goodsentiment;

import static java.time.LocalDateTime.of;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Random;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

/**
 * Generates CSV files with mock data for the ETL and the dashboard development.
 */
public class MockDataGenerator {
    private static int sequence = 0;

    public static void main(String args[]) throws IOException{
        String outputDirectory = args[0];
        LocalDate seedDate = LocalDate.of(2015, 9, 1);
        for(int i = 0; i < 100; i++) {
            LocalDate date = seedDate.plusDays(i);
            File outputDirectoryFile = new File(outputDirectory);
            outputDirectoryFile.mkdirs();
            File outputFile = new File(outputDirectoryFile, String.format("goodsentinment-data-%s.csv", date));
            generateFile(outputFile, date);
        }
    }

    private static void generateFile(File file, LocalDate date)  throws IOException{
        CSVFormat format = CSVFormat
                .newFormat(',')
                .withRecordSeparator("\r\n")
                .withQuote('"')
                .withHeader("id", "sentimentCode", "orgUnit", "latitude", "longitude", "city", "gender", "yearsInCompany","timestamp")
                .withNullString("");

        CSVPrinter printer = null;
        try(OutputStreamWriter output = new OutputStreamWriter(new FileOutputStream(file))) {
            printer = new CSVPrinter(output, format);
            for(int i = 0; i < 150 + rg().nextInt(100); i++) {
                long id = sequence++;
                int sentimentCode = generateSentiment();
                String orgUnit = generateOrgUnit();
                Object geo[] = generateGeo();
                Object latitude = geo[0];
                Object longitude = geo[1];
                Object city = geo[2];
                String gender = generateGender();
                int daysInCompany = generateYearsInCompany();
                LocalDateTime timestamp = generateTimestamp(date);
                printer.printRecord(id, sentimentCode, orgUnit, latitude, longitude, city, gender, daysInCompany, timestamp);
            }
        } finally {
            printer.close();
        }
    }

    private static int generateYearsInCompany() {
        return rg().nextInt(10);
    }

    public static int generateSentiment() {
        switch (rg().nextInt(3)) {
            case 0 :
                return 0;
            case 1 :
                return 100;
            case 2 :
                return -100;
        }
        throw new IllegalStateException();
    }

    public enum Gender {Male, Female}

    public static String generateGender() {
        switch (rg().nextInt(3)) {
            case 0:
                return Gender.Female.toString();
            default:
                return Gender.Male.toString();
        }
    }



    public enum OrgUnit {DevOps, Support, Marketing, Finance, Legal}

    public static String generateOrgUnit() {
        switch(rg().nextInt(4)) {
        case 0:
            return OrgUnit.DevOps.toString();
        case 1:
            return OrgUnit.Support.toString();
        case 2:
            return OrgUnit.Marketing.toString();
        case 3:
            return OrgUnit.Finance.toString();
        case 4:
            return OrgUnit.Legal.toString();
        }
        throw new IllegalStateException();
    }

    private static final Object[] PRAGUE_GEO = new Object[]{50.0833d, 14.4167d, "Prague"};
    private static final Object[] BRNO_GEO = new Object[]{49.2000d, 16.6167d, "Brno"};
    private static final Object[] SF_GEO = new Object[]{37.7833d, -122.4167d, "San Francisco"};

    public static Object[] generateGeo() {
        switch(rg().nextInt(3)) {
        case 0:
            return PRAGUE_GEO;
        case 1:
            return BRNO_GEO;
        case 2:
            return SF_GEO;
        }
        throw new IllegalStateException();
    }

    public static LocalDateTime generateTimestamp(LocalDate seed) {
        return of(seed.getYear(), seed.getMonth(), seed.getDayOfMonth(), 0, 0).plusHours(rg().nextInt(23)).plusMinutes(rg().nextInt(59)).plusSeconds(rg().nextInt(59));
    }

    private static final Random RANDOM_GENERATOR = new SecureRandom();
    public static Random rg() {
        return RANDOM_GENERATOR;
    }
}
