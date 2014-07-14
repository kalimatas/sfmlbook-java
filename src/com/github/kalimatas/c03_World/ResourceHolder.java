package com.github.kalimatas.c03_World;

import org.jsfml.graphics.Texture;

import java.io.IOException;
import java.util.HashMap;

public class ResourceHolder {
    private HashMap<Textures, Texture> textureMap = new HashMap<>();

    public void loadTexture(Textures id, final String filename) {
        Texture texture = new Texture();
        try {
            // Need to load from stream in order to load from JAR
            texture.loadFromStream(getClass().getResourceAsStream(filename));
        } catch (IOException e) {
            throw new RuntimeException("ResourceHolder::load - Failed to load texture " + filename);
        }

        textureMap.put(id, texture);
    }

    public Texture getTexture(Textures id) {
        return textureMap.get(id);
    }
}

