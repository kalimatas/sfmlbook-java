package com.github.kalimatas.c10_Network;

import org.jsfml.graphics.*;
import org.jsfml.system.Vector2f;

public abstract class PostEffect {
    public abstract void apply(final RenderTexture input, RenderTarget output) throws TextureCreationException;

    public void applyShader(final Shader shader, RenderTarget output) {
        Vector2f outputSize = new Vector2f(output.getSize());

        VertexArray vertices = new VertexArray(PrimitiveType.TRIANGLE_STRIP);
        vertices.add(0, new Vertex(new Vector2f(0, 0), new Vector2f(0, 1)));
        vertices.add(1, new Vertex(new Vector2f(outputSize.x, 0), new Vector2f(1, 1)));
        vertices.add(2, new Vertex(new Vector2f(0, outputSize.y), new Vector2f(0, 0)));
        vertices.add(3, new Vertex(new Vector2f(outputSize.x, outputSize.y), new Vector2f(1, 0)));

        RenderStates statesWithBlendMode = new RenderStates(BlendMode.NONE);
        RenderStates states = new RenderStates(statesWithBlendMode, shader);

        output.draw(vertices, states);
    }

    public static boolean isSupported() {
        // On Mac OS X 10.10 the game just freezes after some time
        // with bloom effect enabled, so I disabled it (though on 10.9 it worked).
        // To enable the effect uncomment the following line.
        //return Shader.isAvailable();

        return false;
    }
}
