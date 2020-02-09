package com.kasian.trackme;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import de.mindpipe.android.logging.log4j.LogConfigurator;

import static com.kasian.trackme.Utils.LOG_FILE_NAME;

public class ALogger {
    private static final int MAX_FILE_SIZE = 1024 * 1024 * 5;    // 5Mb
    private static final String LOG_PATTERN = "%d %-5p %c{1} - %m%n";

    public static org.apache.log4j.Logger getLogger(Class clazz) {
        final LogConfigurator logConfigurator = new LogConfigurator();
        logConfigurator.setFileName(LOG_FILE_NAME);
        logConfigurator.setRootLevel(Level.ALL);
        logConfigurator.setLevel("org.apache", Level.ALL);
        logConfigurator.setUseFileAppender(true);
        logConfigurator.setMaxFileSize(MAX_FILE_SIZE);
        logConfigurator.setFilePattern(LOG_PATTERN);
        logConfigurator.setImmediateFlush(true);
        logConfigurator.configure();
        return Logger.getLogger(clazz);
    }
}
