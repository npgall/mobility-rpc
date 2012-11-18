/**
 * Copyright 2011, 2012 Niall Gallagher
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.googlecode.mobilityrpc.quickstart.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.logging.*;

/**
 * Utility methods to configure JDK logging.
 * <p/>
 * These methods will only be used when the framework is run as a standalone server.
 * Otherwise the framework will use but not configure JDK logging, and as such will use whatever configuration
 * the host application sets up.
 *
 * @author Niall Gallagher
 */
public class LoggingUtil {


    /**
     * Sets the logging level for all loggers in or a descendant of the package specified, to the level specified.
     * 
     * @param packageRoot The root package to which the logging level should be applied e.g. "com.foo"
     * @param level The logging level to apply.
     */
    public static void setPackageLoggingLevel(String packageRoot, Level level) {
        Logger appLogger = Logger.getLogger(packageRoot);
        appLogger.setLevel(level);
        Logger rootLogger = Logger.getLogger("");
        for (Handler handler : rootLogger.getHandlers()) {
            handler.setLevel(level);
        }

    }

    public static void setLibraryLoggingLevel(Level level) {
        setPackageLoggingLevel("com.googlecode.mobilityrpc", level);
    }

    /**
     * Sets the logging format for all handlers of the root logger to a single line format.
     */
    public static void setSingleLineLoggingFormat() {
        Logger rootLogger = Logger.getLogger("");
        for (Handler handler : rootLogger.getHandlers()) {
            handler.setFormatter(new SingleLineFormater());
        }
    }


    /**
     * A modified version of {@link java.util.logging.SimpleFormatter} which updates the standard 2-line JDK logging format
     * to instead print on one line.
     * <p/>
     * Note methods in this class are synchronized (as copied from JDK implementation) and so should not be used in
     * production for performance reasons unless via an asynchronous single-threaded log handler.
     *
     * @author Niall Gallagher
     */
    static class SingleLineFormater extends Formatter {

        // Note: ordinarily these objects are not thread-safe,
        // but for the format() method is synchronized (and is for testing only)...
        private final SimpleDateFormat ISO_8601_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        private final DecimalFormat THREAD_ID_FORMAT = new DecimalFormat("000");


        // Line separator string.  This is the value of the line.separator
        // property at the moment that the SimpleFormatter was created.
        private final String lineSeparator = (String) java.security.AccessController.doPrivileged(
                new sun.security.action.GetPropertyAction("line.separator"));

        /**
         * Format the given LogRecord.
         * @param record the log record to be formatted.
         * @return a formatted log record
         */
        public synchronized String format(LogRecord record) {
            StringBuilder sb = new StringBuilder();

            sb.append(ISO_8601_DATE_FORMAT.format(record.getMillis()));
            sb.append("\tThread-");
            sb.append(THREAD_ID_FORMAT.format(record.getThreadID()));
            sb.append("\t");
            sb.append(record.getLevel().getLocalizedName());
            sb.append("\t");
            if (record.getSourceClassName() != null) {
                sb.append(getSimpleClassName(record.getSourceClassName()));
            } else {
                sb.append(record.getLoggerName());
            }
            if (record.getSourceMethodName() != null) {
                sb.append(".");
                sb.append(record.getSourceMethodName());
            }
            sb.append("\t");
            String message = formatMessage(record);
            sb.append(message);
            sb.append(lineSeparator);
            //noinspection ThrowableResultOfMethodCallIgnored
            if (record.getThrown() != null) {
                try {
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    //noinspection ThrowableResultOfMethodCallIgnored
                    record.getThrown().printStackTrace(pw);
                    pw.close();
                    sb.append(sw.toString());
                } catch (Exception ignore) {
                    // Ignore
                }
            }
            return sb.toString();
        }

        static String getSimpleClassName(String fullyQualifiedClassName) {
            int index = fullyQualifiedClassName.lastIndexOf('.');
            if (index == -1) {
                return fullyQualifiedClassName;
            }
            if (index + 1 >= fullyQualifiedClassName.length()) {
                return fullyQualifiedClassName;
            }
            return fullyQualifiedClassName.substring(index + 1);
        }
    }
}
