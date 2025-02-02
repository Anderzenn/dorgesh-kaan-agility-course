package com.anderzenn.dorgeshkaanagilitycourse;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.ComponentOrientation;
import net.runelite.client.ui.overlay.components.ImageComponent;
import net.runelite.client.util.ImageUtil;

import javax.inject.Inject;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class DorgeshKaanOverlay extends OverlayPanel {
    private final Client client;
    private final DorgeshKaanAgilityCourse plugin;
    private final Map<String, BufferedImage> itemIcons = new HashMap<>();

    private String requestedItem1;
    private String requestedItem2;

    @Inject
    private DorgeshKaanOverlay(Client client, DorgeshKaanAgilityCourse plugin) {
        super(plugin);
        this.client = client;
        this.plugin = plugin;

        setPosition(OverlayPosition.ABOVE_CHATBOX_RIGHT);
        setLayer(OverlayLayer.ABOVE_WIDGETS);
        panelComponent.setOrientation(ComponentOrientation.HORIZONTAL);
        panelComponent.setGap(new Point(5, 0));

        loadIcons();
    }

    private void loadIcons() {
        itemIcons.put("capacitor", loadImage("Capacitor.png"));
        itemIcons.put("cog", loadImage("Cog.png"));
        itemIcons.put("fuse", loadImage("Fuse.png"));
        itemIcons.put("lever", loadImage("Lever.png"));
        itemIcons.put("meter", loadImage("Meter.png"));
        itemIcons.put("power box", loadImage("Powerbox.png"));
    }

    private BufferedImage loadImage(String fileName) {
        try {
            return ImageUtil.loadImageResource(DorgeshKaanAgilityCourse.class, "/" + fileName);
        } catch (Exception e) {
            log.error("Error loading image: {}", fileName, e);
            return null;
        }
    }

    public void updateOverlay(String item1, String item2) {
        if (!plugin.hasSpanner()) {
            clearOverlay();
            return;
        }

        this.requestedItem1 = item1;
        this.requestedItem2 = item2;
    }

    public void clearOverlay() {
        requestedItem1 = null;
        requestedItem2 = null;
        panelComponent.getChildren().clear();
    }

    private void addIconToPanel(String itemName) {
        BufferedImage icon = itemIcons.get(itemName);
        if (icon != null) panelComponent.getChildren().add(new ImageComponent(icon));
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        panelComponent.getChildren().clear();

        if (requestedItem1 != null) {
            addIconToPanel(requestedItem1);
        }

        if (requestedItem2 != null) {
            addIconToPanel(requestedItem2);
        }

        return super.render(graphics);
    }
}
