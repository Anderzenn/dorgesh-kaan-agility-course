package com.anderzenn.dorgeshkaanagilitycourse;

import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

import javax.inject.Inject;
import java.awt.*;

/**
 * Highlights the ground route(s) of the Dorgesh-Kaan agility course:
 * <ul>
 *     <li>Before the player has picked up an item, both possible outbound routes to the
 *         generator are highlighted (the grapple route only if a grapple is equipped,
 *         since it's unusable without one either direction).</li>
 *     <li>Once the player is carrying an item, only the single correct return route is
 *         highlighted, since the other route can't be used to bring that item back.</li>
 * </ul>
 */
public class CourseHighlightOverlay extends Overlay
{
	private final Client client;
	private final DorgeshKaanAgilityCourseConfig config;
	private final DorgeshKaanAgilityCourse plugin;

	@Inject
	private CourseHighlightOverlay(Client client, DorgeshKaanAgilityCourseConfig config, DorgeshKaanAgilityCourse plugin)
	{
		this.client = client;
		this.config = config;
		this.plugin = plugin;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.highlightCourse() || client.getLocalPlayer() == null)
		{
			return null;
		}

		// Region ID needs the instance-resolved point (stays 10833 on every floor);
		// the plane needs the player's resolved world location (0/2/3 per floor).
		WorldPoint instancePoint = WorldPoint.fromLocalInstance(client, client.getLocalPlayer().getLocalLocation());
		WorldPoint playerLocation = client.getLocalPlayer().getWorldLocation();

		if (instancePoint.getRegionID() != DorgeshKaanConstants.REGION_ID || playerLocation.getPlane() != DorgeshKaanConstants.COURSE_PLANE)
		{
			return null;
		}

		WorldView worldView = client.getTopLevelWorldView();
		Tile[][][] tiles = worldView.getScene().getTiles();
		int plane = worldView.getPlane();

		RouteType route = plugin.getCurrentRouteType();
		if (route != RouteType.NONE)
		{
			// Carrying an item - only the matching return route is usable.
			highlightRoute(graphics, tiles, plane, route);
		}
		else
		{
			// No item yet - show the outbound route(s) toward the generator.
			// The agility route needs no equipment; the grapple route is only shown if usable.
			highlightRoute(graphics, tiles, plane, RouteType.HEAVY);
			if (plugin.hasGrapple())
			{
				highlightRoute(graphics, tiles, plane, RouteType.DELICATE);
			}
		}

		return null;
	}

	private void highlightRoute(Graphics2D graphics, Tile[][][] tiles, int plane, RouteType route)
	{
		Color colour = RouteColours.forRoute(config, route);

		for (Tile[] column : tiles[plane])
		{
			for (Tile tile : column)
			{
				if (tile == null)
				{
					continue;
				}

				for (GameObject go : tile.getGameObjects())
				{
					if (go != null && route.containsObjectId(go.getId()))
					{
						drawOutline(graphics, go.getConvexHull(), colour);
					}
				}

				GroundObject groundObj = tile.getGroundObject();
				if (groundObj != null && route.containsObjectId(groundObj.getId()))
				{
					drawOutline(graphics, groundObj.getConvexHull(), colour);
				}
			}
		}
	}

	private void drawOutline(Graphics2D graphics, Shape shape, Color colour)
	{
		if (shape == null)
		{
			return;
		}

		graphics.setColor(colour);
		graphics.setStroke(new BasicStroke(config.courseStrokeWidth()));
		graphics.draw(shape);

		graphics.setColor(new Color(colour.getRed(), colour.getGreen(), colour.getBlue(), 20));
		graphics.fill(shape);
	}
}
