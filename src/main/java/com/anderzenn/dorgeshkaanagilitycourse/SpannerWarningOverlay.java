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

/**
 * Warns the player if they're in the course region without the Dorgesh-Kaan spanner.
 */
@Slf4j
public class SpannerWarningOverlay extends OverlayPanel
{
	private final DorgeshKaanAgilityCourse plugin;
	private final BufferedImage spannerIcon;

	@Inject
	private SpannerWarningOverlay(DorgeshKaanAgilityCourse plugin)
	{
		this.plugin = plugin;
		setPosition(OverlayPosition.TOP_LEFT);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
		panelComponent.setWrap(true);
		panelComponent.setOrientation(ComponentOrientation.HORIZONTAL);
		spannerIcon = loadImage();
	}

	private BufferedImage loadImage()
	{
		try
		{
			return ImageUtil.loadImageResource(DorgeshKaanAgilityCourse.class, "/Spanner.png");
		}
		catch (Exception e)
		{
			log.error("Error loading image: {}", "Spanner.png", e);
			return null;
		}
	}

	public void clearWarning()
	{
		panelComponent.getChildren().clear();
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		panelComponent.getChildren().clear();

		if (spannerIcon != null && plugin.displaySpannerWarning())
		{
			panelComponent.getChildren().add(new ImageComponent(spannerIcon));

			panelComponent.getChildren().add(LineComponent.builder()
					.right("No Spanner!")
					.rightColor(Color.RED)
					.build());

			// Spacing to counter text overlapping with icon
			panelComponent.getChildren().add(LineComponent.builder().left("").build());
			panelComponent.getChildren().add(LineComponent.builder().left("").build());

			panelComponent.getChildren().add(LineComponent.builder()
					.left("Talk to Turgall to receive one.")
					.build());
		}

		return super.render(graphics);
	}
}
