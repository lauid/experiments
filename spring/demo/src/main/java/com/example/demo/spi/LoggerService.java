package com.example.demo.spi;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public class LoggerService {
    private static final LoggerService SERVICE = new LoggerService();
    private final Logger logger;

    private final List<Logger> loggerList;

    private LoggerService() {
        ServiceLoader<Logger> loader = ServiceLoader.load(Logger.class);
        List<Logger> list = new ArrayList<>();
        for (Logger log : loader) {
            list.add(log);
        }
        loggerList = list;
        if (!list.isEmpty()) {
            logger = list.get(0);
        } else {
            logger = null;
        }
    }

    public static LoggerService getService() {
        return SERVICE;
    }

    public void info(String msg) {
        if (logger == null) {
            System.out.println(msg);
        } else {
            logger.info(msg);
        }
    }

    public void debug(String msg) {
        if (logger == null) {
            System.out.println(msg);
        } else {
            logger.debug(msg);
        }
    }
}
