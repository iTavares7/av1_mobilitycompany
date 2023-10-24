package br.com.av1.sumo.util;

public class TempUtil {
    private static int number = 0;
    public static Integer getNumber() {
        return ++number;
    }
}