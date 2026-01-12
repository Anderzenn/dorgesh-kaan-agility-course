package com.anderzenn.dorgeshkaanagilitycourse;

import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

import javax.inject.Inject;
import java.awt.Graphics2D;
import java.awt.Shape;

public class TurgallHighlightOverlay extends Overlay
{
	private static final int REGION_ID = 10833;
	private static final int TURGALL_NPC_ID = 2295;

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
	public java.awt.Dimension render(Graphics2D graphics)
	{
		if (!config.highlightTurgall())
		{
			return null;
		}

		if (client.getLocalPlayer() == null)
		{
			return null;
		}

		int regionId = WorldPoint.fromLocal(client, client.getLocalPlayer().getLocalLocation()).getRegionID();
		if (regionId != REGION_ID)
		{
			return null;
		}

		for (NPC npc : client.getNpcs())
		{
			if (npc == null || npc.getId() != TURGALL_NPC_ID)
			{
				continue;
			}

			Shape hull = npc.getConvexHull();
			if (hull != null)
			{
				OverlayUtil.renderPolygon(graphics, hull, config.turgallHighlightColour());
			}
		}

		return null;
	}
}

