package com.github.kalimatas.c08_Graphics;

import com.github.kalimatas.c08_Graphics.GUI.Button;
import com.github.kalimatas.c08_Graphics.GUI.Callback;
import com.github.kalimatas.c08_Graphics.GUI.Container;
import com.github.kalimatas.c08_Graphics.GUI.Label;
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
        addButtonLabel(Player.Action.MOVE_LEFT, 300.f, "Move Left", context);
        addButtonLabel(Player.Action.MOVE_RIGHT, 350.f, "Move Right", context);
        addButtonLabel(Player.Action.MOVE_UP, 400.f, "Move Up", context);
        addButtonLabel(Player.Action.MOVE_DOWN, 450.f, "Move Down", context);
        addButtonLabel(Player.Action.FIRE, 500.f, "Fire", context);
        addButtonLabel(Player.Action.LAUNCH_MISSILE, 550.f, "Missile", context);

        updateLabels();

        Button backButton = new Button(context.fonts, context.textures);
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
        for (int action = 0; action < Player.Action.ACTION_COUNT.ordinal(); action++) {
            if (bindingButtons.get(action).isActive()) {
                isKeyBinding = true;
                if (event.type == Event.Type.KEY_RELEASED) {
                    getContext().player.assignKey(Player.Action.getAction(action), event.asKeyEvent().key);
                    bindingButtons.get(action).deactivate();
                }
                break;
            }
        }

        if (isKeyBinding) {
            updateLabels();
        } else {
            GUIContainer.handleEvent(event);
        }

        return false;
    }

    private void updateLabels() {
        Player player = getContext().player;

        for (int i = 0; i < Player.Action.ACTION_COUNT.ordinal(); i++) {
            Keyboard.Key key = player.getAssignedKey(Player.Action.getAction(i));
            bindingLabels.get(i).setText(key.toString());
        }
    }

    private void addButtonLabel(Player.Action action, float y, final String text, Context context) {
        Button button = new Button(context.fonts, context.textures);
        button.setPosition(80.f, y);
        button.setText(text);
        button.setToggle(true);
        bindingButtons.add(action.ordinal(), button);

        Label label = new Label("", context.fonts);
        label.setPosition(300.f, y + 15.f);
        bindingLabels.add(action.ordinal(), label);

        GUIContainer.pack(button);
        GUIContainer.pack(label);
    }
}
