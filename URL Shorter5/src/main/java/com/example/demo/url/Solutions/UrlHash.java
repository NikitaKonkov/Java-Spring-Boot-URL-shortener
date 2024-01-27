package com.example.demo.url.Solutions;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class UrlHash {
    private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"; // Mein Marken Zeichen
    private static int c = 0; //fixing every problem with a counter XD
    public static String hashTime() {
        c++;
        LocalDateTime now = LocalDateTime.now();
        String time = now.format(DateTimeFormatter.ofPattern("SSmmHHddMMyyyy"));
        int micros = now.getNano() / 1000;
        String hashedTime = c + String.format("%06d", micros % 616161) + time;
        String hash = IntStream.range(0, hashedTime.length())
                .filter(i -> i % 2 == 0)
                .mapToObj(i -> hashedTime.substring(i, Math.min(i + 2, hashedTime.length())))
                .map(Integer::parseInt)
                .map(i -> CHARACTERS.charAt(i % CHARACTERS.length()))
                .map(String::valueOf)
                .collect(Collectors.joining())
                .substring(0,5);
        return hash.substring(0,4); // Smaller hash only 4 chars
    }
}

