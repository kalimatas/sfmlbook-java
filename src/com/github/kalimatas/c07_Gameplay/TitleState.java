package com.github.kalimatas.c07_Gameplay;

import org.jsfml.graphics.RenderWindow;
import org.jsfml.graphics.Sprite;
import org.jsfml.graphics.Text;
import org.jsfml.system.Time;
import org.jsfml.system.Vector2f;
import org.jsfml.window.event.Event;

public class TitleState extends State {
    private Sprite backgroundSprite = new Sprite();
    private Text text = new Text();

    private boolean showText = true;
    private Time textEffectTime = Time.ZERO;

    public TitleState(StateStack stack, Context context) {
        super(stack, context);

        backgroundSprite.setTexture(context.textures.getTexture(Textures.TITLE_SCREEN));

        text.setFont(context.fonts.getFont(Fonts.MAIN));
        text.setString("Press any key to start");
        Utility.centerOrigin(text);
        text.setPosition(Vector2f.div(context.window.getView().getSize(), 2.f));
    }

    @Override
    public void draw() {
        RenderWindow window = getContext().window;
        window.draw(backgroundSprite);

        if (showText) {
            window.draw(text);
        }
    }

    @Override
    public boolean update(Time dt) {
        textEffectTime = Time.add(textEffectTime, dt);

        if (textEffectTime.asMicroseconds() >= Time.getSeconds(0.5f).asMicroseconds()) {
            showText = !showText;
            textEffectTime = Time.ZERO;
        }

        return true;
    }

    @Override
    public boolean handleEvent(Event event) {
        // If any key is pressed, trigger the next screen
        if (event.type == Event.Type.KEY_RELEASED) {
            requestStackPop();
            requestStackPush(States.MENU);
        }

        return true;
    }
}
