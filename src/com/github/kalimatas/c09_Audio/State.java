package com.github.kalimatas.c09_Audio;

import org.jsfml.graphics.RenderWindow;
import org.jsfml.graphics.TextureCreationException;
import org.jsfml.system.Time;
import org.jsfml.window.event.Event;

abstract public class State {
    private StateStack stack;
    private Context context;

    public static class Context {
        public RenderWindow window;
        public ResourceHolder textures;
        public ResourceHolder fonts;
        public Player player;
        public MusicPlayer music;
        public SoundPlayer sounds;

        public Context(RenderWindow window, ResourceHolder textures, ResourceHolder fonts, Player player, MusicPlayer music, SoundPlayer sounds) {
            this.window = window;
            this.textures = textures;
            this.fonts = fonts;
            this.player = player;
            this.music = music;
            this.sounds = sounds;
        }
    }

    public State(StateStack stack, Context context) {
        this.stack = stack;
        this.context = context;
    }

    public abstract void draw() throws TextureCreationException;

    public abstract boolean update(Time dt);

    public abstract boolean handleEvent(final Event event);

    protected void requestStackPush(States stateId) {
        stack.pushState(stateId);
    }

    protected void requestStackPop() {
        stack.popState();
    }

    protected void requestStateClear() {
        stack.clearStates();
    }

    protected Context getContext() {
        return context;
    }
}
