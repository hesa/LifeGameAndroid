/*
 *    Copyright (C) 2019 Henrik Sandklef
 *
 *    This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package se.juneday.lifegame.util;

import java.io.PrintStream;

public class Log {

    public enum LogLevel {
        QUIET,
        ERROR,
        WARN,
        INFO,
        DEBUG,
        VERBOSE;

    }

    private static LogLevel logLevel = LogLevel.ERROR;
    private static String includeFilter;
    private static String includeTag;
    private static PrintStream out = System.out;
    private static PrintStream err = System.err;

    public static void logLevel(LogLevel logLevel) {
        Log.logLevel = logLevel;
    }

    public static void includeFilter(String includeFilter) {
        Log.includeFilter = includeFilter;
    }

    public static void includeTag(String includeTag) {
        Log.includeTag = includeTag;
    }

    private static void printTagMessage(String tag, String message, PrintStream stream) {
        if (includeTag != null) {
            if (!tag.contains(includeTag)) {
                return;
            }
        }
        if (includeFilter != null) {
            if (!message.contains(includeFilter)) {
                return;
            }
        }
        stream.println(tag + ":" + message);
    }

    private static void printTagMessage(String tag, String message) {
        printTagMessage(tag, message, out);
    }

    private static void printErrTagMessage(String tag, String message) {
        printTagMessage(tag, message, err);
    }

    public static void d(String tag, String message) {
        if (logLevel.compareTo(LogLevel.DEBUG) >= 0) {
            printErrTagMessage(tag, message);
        }
    }

    public static void e(String tag, String message) {
        if (logLevel.compareTo(LogLevel.ERROR) >= 0) {
            printErrTagMessage(tag, message);
        }
    }

    public static void i(String tag, String message) {
        if (logLevel.compareTo(LogLevel.INFO) >= 0) {
            printTagMessage(tag, message);
        }
    }

    public static void v(String tag, String message) {
        if (logLevel.compareTo(LogLevel.VERBOSE) >= 0) {
            printErrTagMessage(tag, message);
        }
    }

}
