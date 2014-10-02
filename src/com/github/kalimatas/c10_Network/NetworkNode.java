package com.github.kalimatas.c10_Network;

import com.github.kalimatas.c10_Network.Network.GameActions;
import org.jsfml.system.Vector2f;

import java.util.LinkedList;

public class NetworkNode extends SceneNode {
    private LinkedList<GameActions.Action> pendingActions = new LinkedList<>();

    public int getCategory() {
        return Category.NETWORK;
    }

    public void notifyGameAction(GameActions.Type type, Vector2f position) {
        pendingActions.addLast(new GameActions.Action(type, position));
    }

    public GameActions.Action pollGameAction() {
        // There are no references in Java, so the method is modified,
        // but the idea is the same.
        return pendingActions.isEmpty()
                ? null
                : pendingActions.removeFirst();
    }
}
