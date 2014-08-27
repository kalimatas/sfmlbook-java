package com.github.kalimatas.c08_Graphics;

import org.jsfml.graphics.RenderStates;
import org.jsfml.graphics.RenderTarget;
import org.jsfml.graphics.Text;

public class TextNode extends SceneNode {
    private Text text = new Text();

    public TextNode(final ResourceHolder fonts, final String text) {
        this.text.setFont(fonts.getFont(Fonts.MAIN));
        this.text.setCharacterSize(20);
        setString(text);
    }

    @Override
    protected void drawCurrent(RenderTarget target, RenderStates states) {
        target.draw(text, states);
    }

    public void setString(final String text) {
        this.text.setString(text);
        Utility.centerOrigin(this.text);
    }
}
