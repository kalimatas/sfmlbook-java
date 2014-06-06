package com.github.kalimatas.c04_Input;

import org.jsfml.graphics.*;
import org.jsfml.system.Time;
import org.jsfml.system.Vector2f;

import java.util.ArrayList;
import java.util.List;

public class SceneNode extends BasicTransformable
        implements Drawable
{
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

    public final void update(Time dt) {
        updateCurrent(dt);
        updateChildren(dt);
    }

    protected void updateCurrent(Time dt) {
        // Do nothing by default
    }

    protected void updateChildren(Time dt) {
        for (SceneNode child : children) {
            child.update(dt);
        }
    }

    @Override
    public final void draw(RenderTarget target, RenderStates states) {
        // Apply transform of current node
        RenderStates rs = new RenderStates(states, Transform.combine(states.transform, getTransform()));

        // Draw node and children with changed transform
        drawCurrent(target, rs);
        drawChildren(target, rs);
    }

    protected void drawCurrent(RenderTarget target, RenderStates states) {
        // Do nothing by default
    }

    protected void drawChildren(RenderTarget target, RenderStates states) {
        for (SceneNode child : children) {
            child.draw(target, states);
        }
    }

    public Vector2f getWorldPosition() {
        return getWorldTransform().transformPoint(new Vector2f(0.f, 0.f));
    }

    public Transform getWorldTransform() {
        Transform transform = Transform.IDENTITY;

        for (SceneNode node = this; node != null; node = node.parent) {
            transform = Transform.combine(transform, node.getTransform());
        }

        return transform;
    }

    public void onCommand(final Command command, Time dt) {
        // Command current node, if category matches
        if ((command.category & getCategory()) > 0) {
            command.commandAction.invoke(this, dt);
        }

        // Command children
        for (SceneNode child : children) {
            child.onCommand(command, dt);
        }
    }

    public int getCategory() {
        return Category.SCENE;
    }
}
