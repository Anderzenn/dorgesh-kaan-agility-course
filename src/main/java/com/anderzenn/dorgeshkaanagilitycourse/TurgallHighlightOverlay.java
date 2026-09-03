package com.anderzenn.dorgeshkaanagilitycourse;

import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

import javax.inject.Inject;
import java.awt.*;

/**
 * Highlights Turgall, the Dorgesh-Kaan engineer, so he's easy to spot to collect
 * the spanner from or hand requested items to.
 */
public class TurgallHighlightOverlay extends Overlay
{
	private final Client client;
	private final DorgeshKaanAgilityCourseConfig config;

	@Inject
	private TurgallHighlightOverlay(Client client, DorgeshKaanAgilityCourseConfig config)
	{
		this.client = client;
		this.config = config;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.highlightTurgall() || client.getLocalPlayer() == null)
		{
			return null;
		}

		int regionId = WorldPoint.fromLocal(client, client.getLocalPlayer().getLocalLocation()).getRegionID();
		if (regionId != DorgeshKaanConstants.REGION_ID)
		{
			return null;
		}

		for (NPC npc : client.getTopLevelWorldView().npcs())
		{
			if (npc == null || npc.getId() != DorgeshKaanConstants.TURGALL_NPC_ID)
			{
				continue;
			}

			Shape hull = npc.getConvexHull();
			if (hull == null)
			{
				continue;
			}

			Color colour = config.turgallHighlightColour();
			graphics.setColor(colour);
			graphics.setStroke(new BasicStroke(config.courseStrokeWidth()));
			graphics.draw(hull);

			graphics.setColor(new Color(colour.getRed(), colour.getGreen(), colour.getBlue(), 20));
			graphics.fill(hull);
		}

		return null;
	}
}
