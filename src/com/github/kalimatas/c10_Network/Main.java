package com.github.kalimatas.c10_Network;

public class Main {
    public static void main(String[] args) {
        try {
            Application app = new Application();
            app.run();
        } catch (Exception e) {
            System.out.println("EXCEPTION: " + e.getMessage());
        }
    }
}
