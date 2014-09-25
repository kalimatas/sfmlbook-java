package com.github.kalimatas.c10_Network;

import org.jsfml.system.Time;

public class EmitterNode extends SceneNode {
    private Time accumulatedTime = Time.ZERO;
    private Particle.Type type;
    private ParticleNode particleSystem;

    public EmitterNode(Particle.Type type) {
        this.type = type;
    }

    @Override
    protected void updateCurrent(Time dt, CommandQueue commands) {
        if (particleSystem != null) {
            emitParticles(dt);
        } else {
            // Find particle node with the same type as emitter node
            Command command = new Command();
            command.category = Category.PARTICLE_SYSTEM;
            command.commandAction = new CommandAction<ParticleNode>() {
                @Override
                public void invoke(ParticleNode container, Time dt) {
                    if (container.getParticleType() == type) {
                        particleSystem = container;
                    }
                }
            };

            commands.push(command);
        }
    }

    private void emitParticles(Time dt) {
        final float emissionRate = 30.f;
        final Time interval = Time.div(Time.getSeconds(1.f), emissionRate);

        accumulatedTime = Time.add(accumulatedTime, dt);

        if (accumulatedTime.compareTo(interval) > 0) {
            accumulatedTime = Time.sub(accumulatedTime, interval);
            particleSystem.addParticle(getWorldPosition());
        }
    }
}
