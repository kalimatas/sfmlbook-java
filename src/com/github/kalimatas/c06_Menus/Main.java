package com.github.kalimatas.c06_Menus;

public class Main {
    public static void main(String[] args) {
        try {
            Application app = new Application();
            app.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
