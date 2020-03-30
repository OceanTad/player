package com.hxrainbow.myapplication;

import java.util.Formatter;

public class Util {

    public static String numFormat(long num) {
        if (num > 0) {
            long totalSeconds = num / 1000;
            long seconds = totalSeconds % 60;
            long minutes = (totalSeconds / 60) % 60;
            return new Formatter().format("%02d:%02d", minutes, seconds).toString();
        } else {
            return "00:00";
        }
    }

}
