package com.cdnbye.sdk;

public enum LogLevel {

    // ASSERT = 7; DEBUG = 3; ERROR = 6;INFO = 4;VERBOSE = 2;WARN = 5;

    VERBOSE(2),
    DEBUG(3),
    INFO(4),
    WARN(5),
    ERROR(6);

    private int value = 0;

    LogLevel(int value) {
        this.value = value;
    }

    public int value() {
        return this.value;
    }
}
