package com.github.kalimatas.c10_Network;

import org.jsfml.audio.Music;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MusicPlayer {
    private Music music = new Music();
    private Map<Musics, String> filenames = new HashMap<>();
    private float volume = 100.f;

    public MusicPlayer() {
        filenames.put(Musics.MENU_THEME, "Media/Music/MenuTheme.ogg");
        filenames.put(Musics.MISSION_THEME, "Media/Music/MissionTheme.ogg");
    }

    public void play(Musics theme) {
        String filename = filenames.get(theme);

        try {
            music.openFromStream(getClass().getResourceAsStream(filename));
        } catch (IOException e) {
            throw new RuntimeException("Music " + filename + " could not be loaded.");
        }

        music.setVolume(volume);
        music.setLoop(true);
        music.play();
    }

    public void stop() {
        music.stop();
    }

    public void setVolume(float volume) {
        this.volume = volume;
    }

    public void setPaused(boolean paused) {
        if (paused) {
            music.pause();
        } else {
            music.play();
        }
    }
}
