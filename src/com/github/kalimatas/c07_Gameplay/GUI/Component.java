package com.github.kalimatas.c07_Gameplay.GUI;

import org.jsfml.graphics.BasicTransformable;
import org.jsfml.graphics.Drawable;
import org.jsfml.window.event.Event;

public abstract class Component extends BasicTransformable
    implements Drawable
{
    private boolean isSelected = false;
    private boolean isActive = false;

    public abstract boolean isSelectable();

    public boolean isSelected() {
        return isSelected;
    }

    public void select() {
        isSelected = true;
    }

    public void deselect() {
        isSelected = false;
    }

    public boolean isActive() {
        return isActive;
    }

    public void activate() {
        isActive = true;
    }

    public void deactivate() {
        isActive = false;
    }

    public abstract void handleEvent(final Event event);
}
