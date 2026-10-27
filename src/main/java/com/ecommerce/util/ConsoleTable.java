package com.ecommerce.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Utility for rendering aesthetic ASCII tables with customizable borders and column widths.
 */
public class ConsoleTable {
    private final List<String> headers;
    private final List<List<String>> rows;
    private final int maxColWidth;

    public ConsoleTable(String... headers) {
        this(45, headers);
    }

    public ConsoleTable(int maxColWidth, String... headers) {
        this.headers = Arrays.asList(headers);
        this.rows = new ArrayList<>();
        this.maxColWidth = maxColWidth;
    }

    public void addRow(String... cells) {
        List<String> row = new ArrayList<>();
        for (String cell : cells) {
            String val = cell == null ? "" : cell;
            if (val.length() > maxColWidth) {
                val = val.substring(0, maxColWidth - 3) + "...";
            }
            row.add(val);
        }
        while (row.size() < headers.size()) {
            row.add("");
        }
        rows.add(row);
    }

    public void print() {
        if (headers.isEmpty()) return;

        int[] colWidths = new int[headers.size()];
        for (int i = 0; i < headers.size(); i++) {
            colWidths[i] = headers.get(i).length();
        }

        for (List<String> row : rows) {
            for (int i = 0; i < row.size(); i++) {
                if (i < colWidths.length) {
                    colWidths[i] = Math.max(colWidths[i], row.get(i).length());
                }
            }
        }

        // Add padding
        for (int i = 0; i < colWidths.length; i++) {
            colWidths[i] += 2;
        }

        String horizontalBorder = buildBorder(colWidths);
        System.out.println(horizontalBorder);

        // Print header
        StringBuilder headerLine = new StringBuilder("|");
        for (int i = 0; i < headers.size(); i++) {
            headerLine.append(" ")
                    .append(padRight(headers.get(i), colWidths[i] - 1))
                    .append("|");
        }
        System.out.println(AnsiColor.bold(headerLine.toString()));
        System.out.println(horizontalBorder);

        // Print rows
        if (rows.isEmpty()) {
            String emptyMsg = "No records found.";
            int totalInnerWidth = 0;
            for (int w : colWidths) totalInnerWidth += w + 1;
            totalInnerWidth -= 1;
            System.out.println("| " + padRight(emptyMsg, totalInnerWidth - 1) + "|");
        } else {
            for (List<String> row : rows) {
                StringBuilder rowLine = new StringBuilder("|");
                for (int i = 0; i < headers.size(); i++) {
                    String cellVal = i < row.size() ? row.get(i) : "";
                    rowLine.append(" ")
                            .append(padRight(cellVal, colWidths[i] - 1))
                            .append("|");
                }
                System.out.println(rowLine.toString());
            }
        }

        System.out.println(horizontalBorder);
    }

    private String buildBorder(int[] colWidths) {
        StringBuilder sb = new StringBuilder("+");
        for (int w : colWidths) {
            for (int i = 0; i < w; i++) sb.append("-");
            sb.append("+");
        }
        return sb.toString();
    }

    private String padRight(String s, int n) {
        return String.format("%-" + n + "s", s);
    }
}
