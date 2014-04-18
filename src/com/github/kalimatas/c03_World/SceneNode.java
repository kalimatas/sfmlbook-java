package com.github.kalimatas.c03_World;

import java.util.ArrayList;
import java.util.List;

public class SceneNode {
    private List<SceneNode> children = new ArrayList<>();
    private SceneNode parent;

    public void attachChild(SceneNode child) {
        child.parent = this;
        children.add(child);
    }

    public SceneNode detachChild(final SceneNode child) {
        if (!children.contains(child)) {
            return null;
        }

        SceneNode result = children.get(children.indexOf(child));
        children.remove(child);
        result.parent = null;
        return result;
    }
}
