package com.github.kalimatas.c10_Network;

import org.jsfml.system.Vector2f;

public class SoundNode extends SceneNode {
    private SoundPlayer sounds;

    public SoundNode(SoundPlayer player) {
        this.sounds = player;
    }

    public void playSound(SoundEffects sound, Vector2f position) {
        sounds.play(sound, position);
    }

    @Override
    public int getCategory() {
        return Category.SOUND_EFFECT;
    }
}
