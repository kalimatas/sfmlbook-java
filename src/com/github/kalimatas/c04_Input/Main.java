package com.github.kalimatas.c04_Input;

public class Main {
    public static void main(String[] args) {
        try {
            Game game = new Game();
            game.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
