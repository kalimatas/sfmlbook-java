package com.github.kalimatas.c10_Network.GUI;

import com.github.kalimatas.c10_Network.Fonts;
import com.github.kalimatas.c10_Network.ResourceHolder;
import org.jsfml.graphics.RenderStates;
import org.jsfml.graphics.RenderTarget;
import org.jsfml.graphics.Text;
import org.jsfml.graphics.Transform;
import org.jsfml.window.event.Event;

public class Label extends Component {
    private Text text;

    public Label(final String text, final ResourceHolder fonts) {
        this.text = new Text(text, fonts.getFont(Fonts.MAIN), 16);
    }

    @Override
    public boolean isSelectable() {
        return false;
    }

    @Override
    public void handleEvent(Event event) {

    }

    @Override
    public void draw(RenderTarget target, RenderStates states) {
        RenderStates rs = new RenderStates(states, Transform.combine(states.transform, getTransform()));
        target.draw(text, rs);
    }

    public void setText(final String text) {
        this.text.setString(text);
    }
}
