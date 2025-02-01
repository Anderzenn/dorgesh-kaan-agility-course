package com.anderzenn.dorgeshkaanagilitycourse;

import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.ImageComponent;
import net.runelite.client.ui.overlay.components.LineComponent;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;


public class SpannerWarningOverlay extends OverlayPanel {
    private BufferedImage spannerIcon;
    private boolean showWarning;

    @Inject
    public SpannerWarningOverlay() {
        setPosition(OverlayPosition.TOP_LEFT);
        setLayer(OverlayLayer.ABOVE_WIDGETS);
        loadSpannerIcon();
    }

    private void loadSpannerIcon() {
        spannerIcon = loadImage("Spanner.png");
    }

    private BufferedImage loadImage(String fileName) {
        try (InputStream inputStream = getClass().getResourceAsStream("/" + fileName)) {
            return inputStream != null ? ImageIO.read(inputStream) : null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void updateWarning(boolean showWarning) {
        this.showWarning = showWarning;
    }

    public void clearWarning() {
        showWarning = false;
        panelComponent.getChildren().clear();
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        panelComponent.getChildren().clear();

        if (showWarning) {
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("No spanner!")
                    .right("Talk to Turgall.").build());
            if (spannerIcon != null) {
                panelComponent.getChildren().add(new ImageComponent(spannerIcon));
            }
        }

        return super.render(graphics);
    }
}
