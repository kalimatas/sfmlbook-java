package com.github.kalimatas.c08_Graphics;

import org.jsfml.graphics.*;
import org.jsfml.system.Time;
import org.jsfml.system.Vector2f;
import org.jsfml.system.Vector2i;

public class Animation extends BasicTransformable
        implements Drawable
{
    private Sprite sprite = new Sprite();
    private Vector2i frameSize = new Vector2i(0, 0);
    private int numFrames = 0;
    private int currentFrame = 0;
    private Time duration = Time.ZERO;
    private Time elapsedTime = Time.ZERO;
    private boolean repeat = false;

    public Animation(final Texture texture) {
        sprite.setTexture(texture);
    }

    public void setTexture(final Texture texture) {
        sprite.setTexture(texture);
    }

    public Texture getTexture() {
        return new Texture(sprite.getTexture());
    }

    public void setFrameSize(Vector2i frameSize) {
        this.frameSize = frameSize;
    }

    public Vector2i getFrameSize() {
        return frameSize;
    }

    public void setNumFrames(int numFrames) {
        this.numFrames = numFrames;
    }

    public int getNumFrames() {
        return numFrames;
    }

    public void setDuration(Time duration) {
        this.duration = duration;
    }

    public Time getDuration() {
        return duration;
    }

    public void setRepeating(boolean flat) {
        repeat = flat;
    }

    public boolean isRepeating() {
        return repeat;
    }

    public void restart() {
        currentFrame = 0;
    }

    public boolean isFinished() {
        return currentFrame >= numFrames;
    }

    public FloatRect getLocalBounds() {
        return new FloatRect(getOrigin(), new Vector2f(getFrameSize()));
    }

    public FloatRect getGlobalBounds() {
        return getTransform().transformRect(getLocalBounds());
    }

    public void update(Time dt) {
        Time timePerFrame = Time.div(duration, numFrames);
        elapsedTime = Time.add(elapsedTime, dt);

        Vector2i textureBounds = sprite.getTexture().getSize();
        IntRect textureRect = sprite.getTextureRect();

        if (currentFrame == 0) {
            textureRect = new IntRect(0, 0, frameSize.x, frameSize.y);
        }

        int left = textureRect.left;
        int top = textureRect.top;

        // While we have a frame to process
        while (elapsedTime.compareTo(timePerFrame) >= 0 && (currentFrame <= numFrames || repeat)) {
            // Move the texture rect left
            left += textureRect.width;

            // If we reach the end of the texture
            if (left + textureRect.width > textureBounds.x) {
                // Move it down one line
                left = 0;
                top += textureRect.height;
            }

            textureRect = new IntRect(left, top, textureRect.width, textureRect.height);

            // And progress to next frame
            elapsedTime = Time.sub(elapsedTime, timePerFrame);
            if (repeat) {
                currentFrame = (currentFrame + 1) % numFrames;

                if (currentFrame == 0) {
                    textureRect = new IntRect(0, 0, frameSize.x, frameSize.y);
                }
            } else {
                currentFrame++;
            }
        }

        sprite.setTextureRect(textureRect);
    }

    @Override
    public void draw(RenderTarget target, RenderStates states) {
        RenderStates rs = new RenderStates(states, Transform.combine(states.transform, getTransform()));
        target.draw(sprite, rs);
    }
}
