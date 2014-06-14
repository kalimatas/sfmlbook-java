package com.github.kalimatas.c05_States;

import org.jsfml.graphics.FloatRect;
import org.jsfml.graphics.Sprite;
import org.jsfml.graphics.Text;

public class Utility {
    public static void centerOrigin(Sprite sprite) {
        FloatRect bounds = sprite.getLocalBounds();
        sprite.setOrigin(bounds.width / 2.f, bounds.height / 2.f);
    }

    public static void centerOrigin(Text text) {
        FloatRect bounds = text.getLocalBounds();
        text.setOrigin(bounds.width / 2.f, bounds.height / 2.f);
    }
}
