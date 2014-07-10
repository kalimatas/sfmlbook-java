package com.github.kalimatas.c06_Menus.GUI;

import org.jsfml.graphics.RenderStates;
import org.jsfml.graphics.RenderTarget;
import org.jsfml.graphics.Transform;
import org.jsfml.window.Keyboard;
import org.jsfml.window.event.Event;

import java.util.LinkedList;

public class Container extends Component {
    private LinkedList<Component> children = new LinkedList<>();
    private int selectedChild = -1;

    public void pack(Component component) {
        children.addLast(component);

        if (!hasSelection() && component.isSelectable()) {
            select(children.size() - 1);
        }
    }

    @Override
    public boolean isSelectable() {
        return false;
    }

    @Override
    public void handleEvent(Event event) {
        // If we have selected a child then give it events
        if (hasSelection() && children.get(selectedChild).isActive()) {
            children.get(selectedChild).handleEvent(event);
        } else if (event.type == Event.Type.KEY_RELEASED) {
            Keyboard.Key code = event.asKeyEvent().key;
            if (code == Keyboard.Key.W || code == Keyboard.Key.UP) {
                selectPrevious();
            } else if (code == Keyboard.Key.S || code == Keyboard.Key.DOWN) {
                selectNext();
            } else if (code == Keyboard.Key.RETURN || code == Keyboard.Key.SPACE) {
                if (hasSelection()) {
                    children.get(selectedChild).activate();
                }
            }
        }
    }

    @Override
    public void draw(RenderTarget target, RenderStates states) {
        RenderStates rs = new RenderStates(states, Transform.combine(states.transform, getTransform()));

        for (final Component child : children) {
            target.draw(child, rs);
        }
    }

    private boolean hasSelection() {
        return selectedChild >= 0;
    }

    public void select(int index) {
        if (children.get(index).isSelectable()) {
            if (hasSelection()) {
                children.get(selectedChild).deselect();
            }

            children.get(index).select();
            selectedChild = index;
        }
    }

    public void selectNext() {
        if (!hasSelection()) {
            return;
        }

        // Search next component that is selectable, wrap around if necessary
        int next = selectedChild;
        do {
            next = (next + 1) % children.size();
        } while (!children.get(next).isSelectable());

        // Select that component
        select(next);
    }

    public void selectPrevious() {
        if (!hasSelection()) {
            return;
        }

        // Search previous component that is selectable, wrap around if necessary
        int prev = selectedChild;
        do {
            prev = (prev + children.size() - 1) % children.size();
        } while (!children.get(prev).isSelectable());

        // Select that component
        select(prev);
    }
}
