package com.github.kalimatas.c09_Audio;

import org.jsfml.graphics.RenderTarget;
import org.jsfml.graphics.RenderTexture;
import org.jsfml.graphics.Shader;
import org.jsfml.graphics.TextureCreationException;
import org.jsfml.system.Vector2f;
import org.jsfml.system.Vector2i;

public class BloomEffect extends PostEffect {
    private ResourceHolder shaders = new ResourceHolder();
    private RenderTexture brightnessTexture = new RenderTexture();
    private RenderTexture[] firstPassTextures = {new RenderTexture(), new RenderTexture()};
    private RenderTexture[] secondPassTextures = {new RenderTexture(), new RenderTexture()};

    public BloomEffect() {
        shaders.loadShader(Shaders.BRIGHTNESS_PASS, "Media/Shaders/Fullpass.vert", "Media/Shaders/Brightness.frag");
        shaders.loadShader(Shaders.DOWN_SAMPLE_PASS, "Media/Shaders/Fullpass.vert", "Media/Shaders/DownSample.frag");
        shaders.loadShader(Shaders.GAUSSIAN_BLUR_PASS, "Media/Shaders/Fullpass.vert", "Media/Shaders/GuassianBlur.frag");
        shaders.loadShader(Shaders.ADD_PASS, "Media/Shaders/Fullpass.vert", "Media/Shaders/Add.frag");
    }

    @Override
    public void apply(final RenderTexture input, RenderTarget output) throws TextureCreationException {
        prepareTextures(input.getSize());

        filterBright(input, brightnessTexture);

        downsample(brightnessTexture, firstPassTextures[0]);
        blurMultipass(firstPassTextures);

        downsample(firstPassTextures[0], secondPassTextures[0]);
        blurMultipass(secondPassTextures);

        add(firstPassTextures[0], secondPassTextures[0], firstPassTextures[1]);
        firstPassTextures[1].display();
        add(input, firstPassTextures[1], output);
    }

    private void prepareTextures(Vector2i size) throws TextureCreationException {
        if (brightnessTexture.getSize() != size) {
            brightnessTexture.create(size.x, size.y);
            brightnessTexture.setSmooth(true);

            firstPassTextures[0].create(size.x / 2, size.y / 2);
            firstPassTextures[0].setSmooth(true);
            firstPassTextures[1].create(size.x / 2, size.y / 2);
            firstPassTextures[1].setSmooth(true);

            secondPassTextures[0].create(size.x / 4, size.y / 4);
            secondPassTextures[0].setSmooth(true);
            secondPassTextures[1].create(size.x / 4, size.y / 4);
            secondPassTextures[1].setSmooth(true);
        }
    }

    private void filterBright(final RenderTexture input, RenderTexture output) {
        Shader brightness = shaders.getShader(Shaders.BRIGHTNESS_PASS);

        brightness.setParameter("source", input.getTexture());
        applyShader(brightness, output);
        output.display();
    }

    private void blurMultipass(RenderTexture[] renderTextures) {
        Vector2i textureSize = renderTextures[0].getSize();

        for (int count = 0; count < 2; ++count) {
            blur(renderTextures[0], renderTextures[1], new Vector2f(0.f, 1.f / textureSize.y));
            blur(renderTextures[1], renderTextures[0], new Vector2f(1.f / textureSize.x, 0.f));
        }
    }

    private void blur(final RenderTexture input, RenderTexture output, Vector2f offsetFactor) {
        Shader gaussianBlur = shaders.getShader(Shaders.GAUSSIAN_BLUR_PASS);

        gaussianBlur.setParameter("source", input.getTexture());
        gaussianBlur.setParameter("offsetFactor", offsetFactor);
        applyShader(gaussianBlur, output);
        output.display();
    }

    private void downsample(final RenderTexture input, RenderTexture output) {
        Shader downSampler = shaders.getShader(Shaders.DOWN_SAMPLE_PASS);

        downSampler.setParameter("source", input.getTexture());
        downSampler.setParameter("sourceSize", new Vector2f(input.getSize()));
        applyShader(downSampler, output);
        output.display();
    }

    private void add(final RenderTexture source, final RenderTexture bloom, RenderTarget output) {
        Shader adder = shaders.getShader(Shaders.ADD_PASS);

        adder.setParameter("source", source.getTexture());
        adder.setParameter("bloom", bloom.getTexture());
        applyShader(adder, output);
    }
}
