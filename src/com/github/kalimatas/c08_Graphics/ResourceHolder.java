package com.github.kalimatas.c08_Graphics;

import org.jsfml.graphics.Font;
import org.jsfml.graphics.Shader;
import org.jsfml.graphics.ShaderSourceException;
import org.jsfml.graphics.Texture;

import java.io.IOException;
import java.util.HashMap;

public class ResourceHolder {
    private HashMap<Textures, Texture> textureMap = new HashMap<>();
    private HashMap<Fonts, Font> fontMap = new HashMap<>();
    private HashMap<Shaders, Shader> shaderMap = new HashMap<>();

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

    public void loadFont(Fonts id, final String filename) {
        Font font = new Font();
        try {
            // Need to load from stream in order to load from JAR
            font.loadFromStream(getClass().getResourceAsStream(filename));
        } catch (IOException e) {
            throw new RuntimeException("ResourceHolder::load - Failed to load font " + filename);
        }

        fontMap.put(id, font);
    }

    public Font getFont(Fonts id) {
        return fontMap.get(id);
    }

    public void loadShader(Shaders id, final String filename, final String secondParam) {
        Shader shader = new Shader();
        try {
            // Need to load from stream in order to load from JAR
            shader.loadFromStream(
                    getClass().getResourceAsStream(filename),
                    getClass().getResourceAsStream(secondParam)
            );
        } catch (IOException | ShaderSourceException e) {
            throw new RuntimeException("ResourceHolder::load - Failed to load shader " + filename);
        }

        shaderMap.put(id, shader);
    }

    public Shader getShader(Shaders id) {
        return shaderMap.get(id);
    }
}
