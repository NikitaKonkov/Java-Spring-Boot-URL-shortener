package com.example.demo.url.Solutions;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrlCheck {
    private static final String REGEX = "[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*.[a-zA-Z0-9]*\\.[a-zA-Z]{2,}$"; // Jesus
    private static final Pattern PATTERN = Pattern.compile(REGEX);
    public static boolean check(String url) // checks if the url is valid
    {
        Matcher matcher = PATTERN.matcher(url);
        return matcher.matches(); // Output: true
    }
}
