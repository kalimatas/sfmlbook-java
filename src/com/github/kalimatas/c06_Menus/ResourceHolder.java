package com.github.kalimatas.c06_Menus;

import org.jsfml.graphics.Font;
import org.jsfml.graphics.Texture;

import java.io.IOException;
import java.util.HashMap;

class ResourceHolder {
    private HashMap<Textures, Texture> textureMap = new HashMap<>();
    private HashMap<Fonts, Font> fontMap = new HashMap<>();

    void loadTexture(Textures id, final String filename) {
        Texture texture = new Texture();
        try {
            // Need to load from stream in order to load from JAR
            texture.loadFromStream(getClass().getResourceAsStream(filename));
        } catch (IOException e) {
            throw new RuntimeException("ResourceHolder::load - Failed to load texture " + filename);
        }

        textureMap.put(id, texture);
    }

    Texture getTexture(Textures id) {
        return textureMap.get(id);
    }

    void loadFont(Fonts id, final String filename) {
        Font font = new Font();
        try {
            // Need to load from stream in order to load from JAR
            font.loadFromStream(getClass().getResourceAsStream(filename));
        } catch (IOException e) {
            throw new RuntimeException("ResourceHolder::load - Failed to load font " + filename);
        }

        fontMap.put(id, font);
    }

    Font getFont(Fonts id) {
        return fontMap.get(id);
    }
}

