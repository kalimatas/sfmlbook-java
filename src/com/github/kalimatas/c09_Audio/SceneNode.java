package com.github.kalimatas.c09_Audio;

import org.jsfml.graphics.*;
import org.jsfml.system.Time;
import org.jsfml.system.Vector2f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class SceneNode extends BasicTransformable
        implements Drawable
{
    public class Pair {
        SceneNode first;
        SceneNode second;

        public Pair(SceneNode first, SceneNode second) {
            this.first = first;
            this.second = second;
        }
    }

    private List<SceneNode> children = new ArrayList<>();
    private SceneNode parent;
    private int category = Category.NONE;

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

    public final void update(Time dt, CommandQueue commands) {
        updateCurrent(dt, commands);
        updateChildren(dt, commands);
    }

    protected void updateCurrent(Time dt, CommandQueue commands) {
        // Do nothing by default
    }

    protected FloatRect getBoundingRect() {
        return new FloatRect(0.f, 0.f, 0.f, 0.f);
    }

    protected void updateChildren(Time dt, CommandQueue commands) {
        for (SceneNode child : children) {
            child.update(dt, commands);
        }
    }

    @Override
    public final void draw(RenderTarget target, RenderStates states) {
        // Apply transform of current node
        RenderStates rs = new RenderStates(states, Transform.combine(states.transform, getTransform()));

        // Draw node and children with changed transform
        drawCurrent(target, rs);
        drawChildren(target, rs);

        // Draw bounding rectangle - disabled by default
        //drawBoundingRect(target, states);
    }

    protected void drawCurrent(RenderTarget target, RenderStates states) {
        // Do nothing by default
    }

    protected void drawChildren(RenderTarget target, RenderStates states) {
        for (SceneNode child : children) {
            child.draw(target, states);
        }
    }

    protected void drawBoundingRect(RenderTarget target, RenderStates states) {
        FloatRect rect = getBoundingRect();

        RectangleShape shape = new RectangleShape();
        shape.setPosition(new Vector2f(rect.left, rect.top));
        shape.setSize(new Vector2f(rect.width, rect.height));
        shape.setFillColor(Color.TRANSPARENT);
        shape.setOutlineColor(Color.GREEN);
        shape.setOutlineThickness(1.f);

        target.draw(shape);
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

    @SuppressWarnings("unchecked")
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

    public void setCategory(int category) {
        this.category = category;
    }

    public int getCategory() {
        return category;
    }

    public void checkSceneCollision(SceneNode sceneGraph, Set<Pair> collisionPairs) {
        checkNodeCollision(sceneGraph, collisionPairs);

        for (SceneNode child : sceneGraph.children) {
            checkSceneCollision(child, collisionPairs);
        }
    }

    public void checkNodeCollision(SceneNode node, Set<Pair> collisionPairs) {
        if (!this.equals(node) && collision(this, node) && !isDestroyed() && !node.isDestroyed()) {
            boolean hasPair = false;

            for (Pair pair : collisionPairs) {
                if ((pair.first.equals(this) && pair.second.equals(node))
                    || (pair.first.equals(node) && pair.second.equals(this)))
                {
                    hasPair = true;
                    break;
                }
            }

            if (!hasPair) {
                collisionPairs.add(new Pair(this, node));
            }
        }

        for (SceneNode child : children) {
            child.checkNodeCollision(node, collisionPairs);
        }
    }

    public void removeWrecks() {
        // Remove all children which request so
        for (Iterator<SceneNode> itr = children.iterator(); itr.hasNext(); ) {
            SceneNode child = itr.next();
            if (child.isMarkedForRemoval()) {
                itr.remove();
            }
        }

        // Call function recursively for all remaining children
        for (SceneNode child : children) {
            child.removeWrecks();
        }
    }

    public boolean isMarkedForRemoval() {
        // By default, remove node if entity is destroyed
        return isDestroyed();
    }

    public boolean isDestroyed() {
        // By default, scene node needn't be removed
        return false;
    }

    public boolean collision(final SceneNode lhs, final SceneNode rhs) {
        return lhs.getBoundingRect().intersection(rhs.getBoundingRect()) != null;
    }

    public float distance(final SceneNode lhs, final SceneNode rhs) {
        return Utility.length(Vector2f.sub(lhs.getWorldPosition(), rhs.getWorldPosition()));
    }
}
