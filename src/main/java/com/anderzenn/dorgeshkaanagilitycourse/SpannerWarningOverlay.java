package com.anderzenn.dorgeshkaanagilitycourse;

import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.ComponentOrientation;
import net.runelite.client.ui.overlay.components.ImageComponent;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.util.ImageUtil;

import javax.inject.Inject;
import java.awt.*;
import java.awt.image.BufferedImage;

@Slf4j
public class SpannerWarningOverlay extends OverlayPanel {
    private final DorgeshKaanAgilityCourse plugin;

    private BufferedImage spannerIcon;
    private boolean showWarning;

    @Inject
    private SpannerWarningOverlay(DorgeshKaanAgilityCourse plugin) {
        this.plugin = plugin;
        setPosition(OverlayPosition.TOP_LEFT);
        setLayer(OverlayLayer.ABOVE_WIDGETS);
        panelComponent.setWrap(true);
        panelComponent.setOrientation(ComponentOrientation.HORIZONTAL);
        loadSpannerIcon();
    }

    private void loadSpannerIcon() {
        spannerIcon = loadImage();
    }

    private BufferedImage loadImage() {
        try {
            return ImageUtil.loadImageResource(DorgeshKaanAgilityCourse.class, "/Spanner.png");
        } catch (Exception e) {
            log.error("Error loading image: {}", "Spanner.png", e);
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

        if (spannerIcon != null && plugin.displaySpannerWarning()) {
                panelComponent.getChildren().add(new ImageComponent(spannerIcon));
                panelComponent.getChildren().add(LineComponent.builder()
                        .right("No Spanner!")
                        .rightColor(Color.RED)
                        .build());
                // Spacing to counter text overlapping with icon
                panelComponent.getChildren().add(LineComponent.builder()
                        .left("")
                        .build());
                panelComponent.getChildren().add(LineComponent.builder()
                        .left("")
                        .build());
                // Text that without the two line components above, overlaps with icon
                panelComponent.getChildren().add(LineComponent.builder()
                        .left("Talk to Turgall to receive another.")
                        .build());
            }

        return super.render(graphics);
    }
}
