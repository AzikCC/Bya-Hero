package com.byahero;

import com.byahero.config.AppConfig;
import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.IOException;

/**
 * START HERE. Run this class to start the website at http://localhost:8080
 *
 * How one request travels through the program:
 *   Browser -> Controller -> Service -> Repository -> CSV file
 *   - Controller : reads the request and shows the page   (package controller)
 *   - View       : builds the HTML                        (package view)
 *   - Service    : the rules, like validation and fares   (package service)
 *   - Repository : reads and writes the CSV files         (package repository)
 *   - Model      : the objects, like Bus and Fare         (package model)
 *   - Config     : connects all of the above              (package config)
 */
public class Main {

    private static final int PORT = 8080;

    /** Finds the "resources" folder, even if the IDE runs from a different folder. */
    private static String findResourceFolder() {
        String[] choices = { "resources", "../resources", "ByaHero/resources" };
        for (int i = 0; i < choices.length; i++) {
            File board = new File(choices[i] + "/templates/board.html");
            if (board.exists()) {
                return choices[i];
            }
        }
        return "resources";
    }

    public static void main(String[] args) throws IOException {
        HttpServer server = new AppConfig(findResourceFolder(), PORT).createServer();
        server.start();
        System.out.println("ByaHero is running at http://localhost:" + PORT);
    }
}
