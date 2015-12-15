/*
 * Copyright 2015 the original author or authors. Licensed under the Apache License, Version 2.0 (the "License");
 */

package cz.pichlik.goodsentiment;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;

/**
 * Input/Output utilities.
 */
public class IOUtils {

    /**
     * Flushes and closes the given Closeable quietly.
     * @param is the given closeable
     * @param log logger used to log any failure should the close fail
     */
    public static void closeQuietly(Closeable is) {
        if (is != null) {
            try{
                if(is instanceof Flushable) {
                    ((Flushable) is).flush();
                }
            } catch (IOException e) {
                //ignore flush errors
            } finally {
                try {
                    is.close();
                } catch (IOException ex) {
                }
            }
        }
    }
}
