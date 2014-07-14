package com.github.kalimatas.c02_Resources;

import org.jsfml.graphics.Texture;

import java.io.IOException;
import java.util.HashMap;

class ResourceHolder {
    private HashMap<ID, Texture> textureMap = new HashMap<>();

    public void loadTexture(ID id, final String filename) {
        Texture texture = new Texture();
        try {
            // Need to load from stream in order to load from JAR
            texture.loadFromStream(getClass().getResourceAsStream(filename));
        } catch (IOException e) {
            throw new RuntimeException("ResourceHolder::load - Failed to load texture " + filename);
        }

        textureMap.put(id, texture);
    }

    public Texture getTexture(ID id) {
        return textureMap.get(id);
    }
}

