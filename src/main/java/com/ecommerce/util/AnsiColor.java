package com.ecommerce.util;

/**
 * ANSI Escape Codes for colored, formatted console output.
 */
public class AnsiColor {
    public static final String RESET = "\u001B[0m";
    public static final String BLACK = "\u001B[30m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String PURPLE = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";
    public static final String WHITE = "\u001B[37m";

    // Bold colors
    public static final String BOLD = "\u001B[1m";
    public static final String BOLD_RED = "\u001B[1;31m";
    public static final String BOLD_GREEN = "\u001B[1;32m";
    public static final String BOLD_YELLOW = "\u001B[1;33m";
    public static final String BOLD_BLUE = "\u001B[1;34m";
    public static final String BOLD_PURPLE = "\u001B[1;35m";
    public static final String BOLD_CYAN = "\u001B[1;36m";
    public static final String BOLD_WHITE = "\u001B[1;37m";

    // Backgrounds
    public static final String BG_BLUE = "\u001B[44m";
    public static final String BG_GREEN = "\u001B[42m";
    public static final String BG_RED = "\u001B[41m";

    public static String color(String text, String ansiCode) {
        return ansiCode + text + RESET;
    }

    public static String bold(String text) {
        return BOLD + text + RESET;
    }

    public static String success(String text) {
        return BOLD_GREEN + text + RESET;
    }

    public static String error(String text) {
        return BOLD_RED + text + RESET;
    }

    public static String warning(String text) {
        return BOLD_YELLOW + text + RESET;
    }

    public static String info(String text) {
        return BOLD_CYAN + text + RESET;
    }
}
