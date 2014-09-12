package com.github.kalimatas.c08_Graphics;

import com.github.kalimatas.c08_Graphics.DataTables.DataTables;
import com.github.kalimatas.c08_Graphics.DataTables.ParticleData;
import org.jsfml.graphics.*;
import org.jsfml.system.Time;
import org.jsfml.system.Vector2f;

import java.util.ArrayList;
import java.util.LinkedList;

public class ParticleNode extends SceneNode {
    private LinkedList<Particle> particles = new LinkedList<>();
    private final Texture texture;
    private Particle.Type type;

    private VertexArray vertexArray = new VertexArray(PrimitiveType.QUADS);
    private boolean needsVertexUpdate = true;

    private static ArrayList<ParticleData> Table = DataTables.initializeParticleData();

    public ParticleNode(Particle.Type type, final ResourceHolder textures) {
        this.type = type;
        this.texture = new Texture(textures.getTexture(Textures.PARTICLE));
    }

    public void addParticle(Vector2f position) {
        Particle particle = new Particle();
        particle.position = position;
        particle.color = Table.get(type.ordinal()).color;
        particle.lifetime = Table.get(type.ordinal()).lifetime;

        particles.addLast(particle);
    }

    public Particle.Type getParticleType() {
        return type;
    }

    public int getCategory() {
        return Category.PARTICLE_SYSTEM;
    }

    @Override
    protected void updateCurrent(Time dt, CommandQueue commands) {
        // Remove expired particles at beginning
        while (!particles.isEmpty() && particles.peekFirst().lifetime.asMicroseconds() <= Time.ZERO.asMicroseconds()) {
            particles.removeFirst();
        }

        // Decrease lifetime of existing particles
        for (Particle particle : particles) {
            particle.lifetime = Time.sub(particle.lifetime, dt);
        }

        needsVertexUpdate = true;
    }

    @Override
    protected void drawCurrent(RenderTarget target, RenderStates states) {
        if (needsVertexUpdate) {
            computeVectices();
            needsVertexUpdate = false;
        }

        // Apply particle texture
        RenderStates rs = new RenderStates(states, texture);

        // Draw vertices
        target.draw(vertexArray, rs);
    }

    private void addVertex(float worldX, float worldY, float texCoordX, float texCoordY, final Color color) {
        Vector2f position = new Vector2f(worldX, worldY);
        Vector2f texCoords = new Vector2f(texCoordX, texCoordY);
        vertexArray.add(new Vertex(position, color, texCoords));
    }

    private void computeVectices() {
        Vector2f size = new Vector2f(texture.getSize());
        Vector2f half = Vector2f.div(size, 2.f);

        // Refill vertex array
        vertexArray.clear();
        for (Particle particle : particles) {
            Vector2f pos = particle.position;

            float ration = particle.lifetime.asSeconds() / Table.get(type.ordinal()).lifetime.asSeconds();
            Color color = new Color(particle.color.r, particle.color.g, particle.color.b, (int)(255 * Math.max(ration, 0.f)));

            addVertex(pos.x - half.x, pos.y - half.y, 0.f, 0.f, color);
            addVertex(pos.x + half.x, pos.y - half.y, size.x, 0.f, color);
            addVertex(pos.x + half.x, pos.y + half.y, size.x, size.y, color);
            addVertex(pos.x - half.x, pos.y + half.y, 0.f, size.y, color);
        }
    }
}
