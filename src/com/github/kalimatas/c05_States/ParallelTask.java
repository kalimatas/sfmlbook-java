package com.github.kalimatas.c05_States;

import org.jsfml.system.Clock;

public class ParallelTask extends Thread {
    private boolean finished = false;
    private Clock elapsedTime = new Clock();

    public void execute() {
        finished = false;
        elapsedTime.restart();
        start();
    }

    synchronized public boolean isFinished() {
        return finished;
    }

    synchronized public float getCompletion() {
        // 100% at 10 seconds of elapsed time
        return elapsedTime.getElapsedTime().asSeconds() / 10.f;
    }

    public void run() {
        // Dummy task - stall 10 seconds
        boolean ended = false;
        while (!ended) {
            synchronized (this) { // Protect the clock
                if (elapsedTime.getElapsedTime().asSeconds() >= 10.f) {
                    ended = true;
                }
            }
        }

        // finished may be accessed from multiple threads, protect it
        synchronized (this) {
            finished = true;
        }
    }
}
