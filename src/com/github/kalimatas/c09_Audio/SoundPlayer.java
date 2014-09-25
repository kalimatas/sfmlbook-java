package com.github.kalimatas.c09_Audio;

import org.jsfml.audio.Listener;
import org.jsfml.audio.Sound;
import org.jsfml.audio.SoundSource;
import org.jsfml.system.Vector2f;
import org.jsfml.system.Vector3f;

import java.util.LinkedList;

public class SoundPlayer {
    private static final float LISTENER_Z = 300.f;
    private static final float ATTENUATION = 8.f;
    private static final float MIN_DISTANCE_2D = 200.f;
    private static final float MIN_DISTANCE_3D = (float) Math.sqrt(MIN_DISTANCE_2D * MIN_DISTANCE_2D + LISTENER_Z * LISTENER_Z);

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

        // Listener points towards the screen (default in SFML)
        Listener.setDirection(0.f, 0.f, -1.f);
    }

    public void play(SoundEffects effect) {
        play(effect, getListenerPosition());
    }

    public void play(SoundEffects effect, Vector2f position) {
        Sound sound = new Sound();
        sound.setBuffer(soundBuffers.getSoundEffect(effect));
        sound.setPosition(position.x, -position.y, 0.f);
        sound.setAttenuation(ATTENUATION);
        sound.setMinDistance(MIN_DISTANCE_3D);

        sounds.addLast(sound);

        sound.play();
    }

    public void removeStoppedSounds() {
        LinkedList<Sound> originalSounds = new LinkedList<>(sounds);
        for (Sound sound : originalSounds) {
            if (sound.getStatus() == SoundSource.Status.STOPPED) {
                sounds.remove(sound);
            }
        }
    }

    public void setListenerPosition(Vector2f position) {
        Listener.setPosition(position.x, -position.y, LISTENER_Z);
    }

    public Vector2f getListenerPosition() {
        Vector3f position = Listener.getPosition();
        return new Vector2f(position.x, -position.y);
    }
}
