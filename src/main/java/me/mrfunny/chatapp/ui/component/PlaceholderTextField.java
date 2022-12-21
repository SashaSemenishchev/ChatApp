package me.mrfunny.chatapp.ui.component;

import javax.swing.*;
import javax.swing.text.Document;
import java.awt.*;

public class PlaceholderTextField extends JTextField {
    private String placeholder;

    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(final String s) {
        placeholder = s;
    }

    @Override
    protected void paintComponent(final Graphics thisGraphics) {
        super.paintComponent(thisGraphics);

        if (placeholder == null || placeholder.length() == 0 || getText().length() > 0) {
            return;
        }

        final Graphics2D graphics = (Graphics2D) thisGraphics;
        graphics.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setColor(getDisabledTextColor());
        graphics.drawString(placeholder, getInsets().left, thisGraphics.getFontMetrics()
                .getMaxAscent() + getInsets().top);
    }

}
