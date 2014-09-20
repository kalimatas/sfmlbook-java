package com.github.kalimatas.c08_Graphics;

import org.jsfml.graphics.FloatRect;
import org.jsfml.graphics.Sprite;
import org.jsfml.graphics.Text;
import org.jsfml.system.Vector2f;

import java.util.Random;

public class Utility {
    public static void centerOrigin(Sprite sprite) {
        FloatRect bounds = sprite.getLocalBounds();
        sprite.setOrigin((float)Math.floor(bounds.width / 2.f), (float)Math.floor(bounds.height / 2.f));
    }

    public static void centerOrigin(Text text) {
        FloatRect bounds = text.getLocalBounds();
        text.setOrigin((float)Math.floor(bounds.width / 2.f), (float)Math.floor(bounds.height / 2.f));
    }

    public static void centerOrigin(Animation animation) {
        FloatRect bounds = animation.getLocalBounds();
        animation.setOrigin((float)Math.floor(bounds.width / 2.f), (float)Math.floor(bounds.height / 2.f));
    }

    public static float toDegree(float radian) {
        return 180.f / (float)Math.PI * radian;
    }

    public static float toRadian(float degree) {
        return (float)Math.PI / 180.f * degree;
    }

    public static int randomInt(int exclusiveMax) {
        Random random = new Random();
        return random.nextInt(exclusiveMax);
    }

    public static float length(Vector2f vector) {
        return (float) Math.sqrt(vector.x * vector.x + vector.y * vector.y);
    }

    public static Vector2f unitVector(Vector2f vector) {
        assert vector != new Vector2f(0.f, 0.f);
        return Vector2f.div(vector, length(vector));
    }
}
