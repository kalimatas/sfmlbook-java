package com.github.kalimatas.c09_Audio;

import org.jsfml.audio.Sound;
import org.jsfml.audio.SoundSource;
import org.jsfml.system.Vector2f;

import java.util.LinkedList;

public class SoundPlayer {
    private ResourceHolder soundBuffers = new ResourceHolder();
    private LinkedList<Sound> sounds = new LinkedList<>();

    public SoundPlayer() {
        soundBuffers.loadSoundEffect(SoundEffects.ALLIED_GUNFIRE, "Media/Sound/AlliedGunfire.wav");
        soundBuffers.loadSoundEffect(SoundEffects.ENEMY_GUNFIRE, "Media/Sound/EnemyGunfire.wav");
        soundBuffers.loadSoundEffect(SoundEffects.EXPLOSION1, "Media/Sound/Explosion1.wav");
        soundBuffers.loadSoundEffect(SoundEffects.EXPLOSION2, "Media/Sound/Explosion2.wav");
        soundBuffers.loadSoundEffect(SoundEffects.LAUNCH_MISSILE, "Media/Sound/LaunchMissile.wav");
        soundBuffers.loadSoundEffect(SoundEffects.COLLECT_PICKUP, "Media/Sound/CollectPickup.wav");
        soundBuffers.loadSoundEffect(SoundEffects.BUTTON, "Media/Sound/Button.wav");
    }

    public void play(SoundEffects effect) {
        sounds.addLast(new Sound(soundBuffers.getSoundEffect(effect)));
        sounds.peekLast().play();
    }

    public void play(SoundEffects effect, Vector2f position) {

    }

    public void removeStoppedSounds() {
        for (Sound sound : sounds) {
            if (sound.getStatus() == SoundSource.Status.STOPPED) {
                sounds.remove(sound);
            }
        }
    }

    public void setListenerPosition(Vector2f position) {

    }

    public Vector2f getListenerPosition() {
        return new Vector2f(0.f, 0.f);
    }
}
