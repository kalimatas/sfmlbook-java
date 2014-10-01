package com.github.kalimatas.c10_Network;

import com.github.kalimatas.c10_Network.GUI.Button;
import com.github.kalimatas.c10_Network.GUI.Callback;
import com.github.kalimatas.c10_Network.GUI.Container;
import com.github.kalimatas.c10_Network.GUI.Label;
import org.jsfml.graphics.*;
import org.jsfml.system.Time;
import org.jsfml.window.Keyboard;
import org.jsfml.window.event.Event;

import java.util.ArrayList;

public class SettingsState extends State {
    private Sprite backgroundSprite = new Sprite();
    private Container GUIContainer = new Container();
    private ArrayList<Button> bindingButtons = new ArrayList<>(Player.Action.ACTION_COUNT.ordinal());
    private ArrayList<Label> bindingLabels = new ArrayList<>(Player.Action.ACTION_COUNT.ordinal());

    public SettingsState(StateStack stack, Context context) {
        super(stack, context);

        backgroundSprite.setTexture(context.textures.getTexture(Textures.TITLE_SCREEN));

        // Build key binding buttons and labels
        for (int x = 0; x < 2; x++) {
            addButtonLabel(Player.Action.MOVE_LEFT, x, 0, "Move Left", context);
            addButtonLabel(Player.Action.MOVE_RIGHT, x, 1, "Move Right", context);
            addButtonLabel(Player.Action.MOVE_UP, x, 2, "Move Up", context);
            addButtonLabel(Player.Action.MOVE_DOWN, x, 3, "Move Down", context);
            addButtonLabel(Player.Action.FIRE, x, 4, "Fire", context);
            addButtonLabel(Player.Action.LAUNCH_MISSILE, x, 5, "Missile", context);
        }

        updateLabels();

        Button backButton = new Button(context);
        backButton.setPosition(80.f, 620.f);
        backButton.setText("Back");
        backButton.setCallback(new Callback() {
            @Override
            public void invoke() {
                requestStackPop();
            }
        });

        GUIContainer.pack(backButton);
    }

    @Override
    public void draw() {
        RenderWindow window = getContext().window;

        window.draw(backgroundSprite);
        window.draw(GUIContainer);
    }

    @Override
    public boolean update(Time dt) {
        return true;
    }

    @Override
    public boolean handleEvent(Event event) {
        boolean isKeyBinding = false;

        // Iterate through all key binding buttons to see if they are being pressed, waiting for the user to enter a key
        for (int i = 0; i < 2 * Player.Action.ACTION_COUNT.ordinal(); i++) {
            if (bindingButtons.get(i).isActive()) {
                isKeyBinding = true;
                if (event.type == Event.Type.KEY_RELEASED) {
                    // Player 1
                    if (i < Player.Action.ACTION_COUNT.ordinal()) {
                        getContext().keys1.assignKey(Player.Action.getAction(i), event.asKeyEvent().key);
                    }
                    // Player 2
                    else {
                        getContext().keys2.assignKey(Player.Action.getAction(i - Player.Action.ACTION_COUNT.ordinal()), event.asKeyEvent().key);
                    }

                    bindingButtons.get(i).deactivate();
                }
                break;
            }
        }

        // If pressed button changed key bindings, update labels; otherwise consider other buttons in container
        if (isKeyBinding) {
            updateLabels();
        } else {
            GUIContainer.handleEvent(event);
        }

        return false;
    }

    private void updateLabels() {
        for (int i = 0; i < Player.Action.ACTION_COUNT.ordinal(); i++) {
            Player.Action action = Player.Action.getAction(i);

            // Get keys of both players
            Keyboard.Key key1 = getContext().keys1.getAssignedKey(action);
            Keyboard.Key key2 = getContext().keys2.getAssignedKey(action);

            // Assign both key strings to labels
            bindingLabels.get(i).setText(key1.toString());
            bindingLabels.get(i + Player.Action.ACTION_COUNT.ordinal()).setText(key2.toString());
        }
    }

    private void addButtonLabel(Player.Action action, int x, int y, final String text, Context context) {
        // For x==0, start at index 0, otherwise start at half of array
        int index = action.ordinal() + Player.Action.ACTION_COUNT.ordinal() * x;

        Button button = new Button(context);
        button.setPosition(400.f * x + 80.f, 50.f * y + 300.f);
        button.setText(text);
        button.setToggle(true);
        bindingButtons.add(index, button);

        Label label = new Label("", context.fonts);
        label.setPosition(400.f * x + 300.f, 50.f * y + 315.f);
        bindingLabels.add(index, label);

        GUIContainer.pack(button);
        GUIContainer.pack(label);
    }
}
