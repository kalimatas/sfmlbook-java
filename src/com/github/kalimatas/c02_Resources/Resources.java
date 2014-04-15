package com.github.kalimatas.c02_Resources;

import org.jsfml.graphics.RenderWindow;
import org.jsfml.graphics.Sprite;
import org.jsfml.window.VideoMode;
import org.jsfml.window.event.Event;

enum ID {
    LANDSCAPE,
    AIRPLANE,
}

public class Resources {
    public static void main(String[] args) {
        RenderWindow window = new RenderWindow(new VideoMode(640, 480), "Resources");
        window.setFramerateLimit(20);

        ResourceHolder textures = new ResourceHolder();

        try {
            textures.loadTexture(ID.LANDSCAPE, "Media/Textures/Desert.png");
            textures.loadTexture(ID.AIRPLANE, "Media/Textures/Eagle.png");
        } catch (RuntimeException e) {
            System.out.printf("Exception: %s\n", e.getMessage());
            System.exit(1);
        }

        Sprite landscape = new Sprite();
        landscape.setTexture(textures.getTexture(ID.LANDSCAPE));
        Sprite airplane = new Sprite();
        airplane.setTexture(textures.getTexture(ID.AIRPLANE));
        airplane.setPosition(200.f, 200.f);

        while (window.isOpen()) {
            for (Event event : window.pollEvents()) {
                switch (event.type) {
                    case KEY_PRESSED:
                    case CLOSED:
                        window.close();
                        break;
                }
            }

            window.clear();
            window.draw(landscape);
            window.draw(airplane);
            window.display();
        }
    }
}
