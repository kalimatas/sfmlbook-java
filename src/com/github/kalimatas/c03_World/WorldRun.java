package com.github.kalimatas.c03_World;

public class WorldRun {
    public static void main(String[] args) {
        try {
            Game game = new Game();
            game.run();
        } catch (Exception e) {
            System.out.println("EXCEPTION: " + e.getMessage());
        }
    }
}
