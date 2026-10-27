package com.ecommerce;

import java.io.IOException;

import com.ecommerce.ui.MainMenu;
import com.ecommerce.util.AnsiColor;
import com.ecommerce.web.ApiServer;

/**
 * Main application entry point for the E-Commerce Product Management System.
 * Starts the Full-Stack Web Application Server by default, or the CLI if requested.
 */
public class App {
    public static void main(String[] args) {
        boolean runCli = false;
        for (String arg : args) {
            if (arg.equalsIgnoreCase("--cli") || arg.equalsIgnoreCase("-c")) {
                runCli = true;
                break;
            }
        }

        if (runCli) {
            MainMenu menu = new MainMenu();
            menu.start();
        } else {
            try {
                int port = 8080;
                for (String arg : args) {
                    if (arg.startsWith("--port=")) {
                        port = Integer.parseInt(arg.substring(7));
                    }
                }
                ApiServer server = new ApiServer(port);
                server.start();
                System.out.println(AnsiColor.info("Press Ctrl+C to terminate the server, or run with --cli for console mode."));
            } catch (IOException | NumberFormatException e) {
                System.err.println(AnsiColor.error("Failed to start Web Server: " + e.getMessage()));
                System.out.println(AnsiColor.warning("Falling back to Interactive Console interface..."));
                MainMenu menu = new MainMenu();
                menu.start();
            }
        }
    }
}
