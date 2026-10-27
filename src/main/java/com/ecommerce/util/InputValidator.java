package com.ecommerce.util;

import java.math.BigDecimal;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * Robust console input reader with type checks, validation, and error recovery.
 */
public class InputValidator {
    private static final Scanner SCANNER = new Scanner(System.in);
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    public static String readString(String prompt, boolean allowEmpty) {
        while (true) {
            System.out.print(AnsiColor.color(prompt + ": ", AnsiColor.BOLD_CYAN));
            String line = SCANNER.nextLine();
            if (line != null) line = line.trim();
            if (!allowEmpty && (line == null || line.isEmpty())) {
                System.out.println(AnsiColor.error("Input cannot be empty. Please try again."));
                continue;
            }
            return line;
        }
    }

    public static String readEmail(String prompt) {
        while (true) {
            String email = readString(prompt, false);
            if (EMAIL_PATTERN.matcher(email).matches()) {
                return email;
            }
            System.out.println(AnsiColor.error("Invalid email address format (e.g. user@example.com)."));
        }
    }

    public static int readInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(AnsiColor.color(prompt + " (" + min + "-" + max + "): ", AnsiColor.BOLD_CYAN));
            String line = SCANNER.nextLine().trim();
            try {
                int val = Integer.parseInt(line);
                if (val < min || val > max) {
                    System.out.println(AnsiColor.error("Value must be between " + min + " and " + max + "."));
                    continue;
                }
                return val;
            } catch (NumberFormatException e) {
                System.out.println(AnsiColor.error("Please enter a valid integer number."));
            }
        }
    }

    public static BigDecimal readBigDecimal(String prompt, BigDecimal min, BigDecimal max) {
        while (true) {
            System.out.print(AnsiColor.color(prompt + " (min " + min + "): ", AnsiColor.BOLD_CYAN));
            String line = SCANNER.nextLine().trim();
            try {
                BigDecimal val = new BigDecimal(line);
                if (min != null && val.compareTo(min) < 0) {
                    System.out.println(AnsiColor.error("Value must be at least " + min + "."));
                    continue;
                }
                if (max != null && val.compareTo(max) > 0) {
                    System.out.println(AnsiColor.error("Value cannot exceed " + max + "."));
                    continue;
                }
                return val;
            } catch (Exception e) {
                System.out.println(AnsiColor.error("Please enter a valid decimal number (e.g. 19.99)."));
            }
        }
    }

    public static boolean readConfirmation(String prompt) {
        while (true) {
            System.out.print(AnsiColor.color(prompt + " (y/n): ", AnsiColor.BOLD_YELLOW));
            String line = SCANNER.nextLine().trim().toLowerCase();
            if (line.equals("y") || line.equals("yes")) return true;
            if (line.equals("n") || line.equals("no")) return false;
            System.out.println(AnsiColor.error("Please enter 'y' for yes or 'n' for no."));
        }
    }

    public static String readPassword(String prompt) {
        // Try System.console() first for masking; fallback to scanner for IDEs
        if (System.console() != null) {
            System.out.print(AnsiColor.color(prompt + ": ", AnsiColor.BOLD_CYAN));
            char[] chars = System.console().readPassword();
            if (chars != null && chars.length > 0) {
                return new String(chars);
            }
        }
        return readString(prompt, false);
    }
}
