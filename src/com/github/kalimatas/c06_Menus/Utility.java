package com.github.kalimatas.c06_Menus;

import org.jsfml.graphics.FloatRect;
import org.jsfml.graphics.Sprite;
import org.jsfml.graphics.Text;

public class Utility {
    public static void centerOrigin(Sprite sprite) {
        FloatRect bounds = sprite.getLocalBounds();
        sprite.setOrigin((float)Math.floor(bounds.width / 2.f), (float)Math.floor(bounds.height / 2.f));
    }

    public static void centerOrigin(Text text) {
        FloatRect bounds = text.getLocalBounds();
        text.setOrigin((float)Math.floor(bounds.width / 2.f), (float)Math.floor(bounds.height / 2.f));
    }
}
