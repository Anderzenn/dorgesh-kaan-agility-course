package com.anderzenn.dorgeshkaanagilitycourse;

import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.*;
import net.runelite.client.util.ImageUtil;

import javax.inject.Inject;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * Info panel shown near the chatbox. Displays:
 * <ul>
 *     <li>the two items the engineer might request, highlighted green once picked up</li>
 *     <li>a "Route:" row showing whether the item currently held is a delicate or heavy item</li>
 * </ul>
 */
@Slf4j
public class DorgeshKaanOverlay extends OverlayPanel
{
	private static final int ICON_SIZE = 10;
	private static final int ICON_TEXT_GAP = 4;
	private static final int PANEL_WIDTH = 230;

	private final DorgeshKaanAgilityCourse plugin;
	private final DorgeshKaanAgilityCourseConfig config;
	private final Map<String, BufferedImage> itemIcons = new HashMap<>();

	private String requestedItem1;
	private String requestedItem2;

	@Inject
	private DorgeshKaanOverlay(DorgeshKaanAgilityCourse plugin, DorgeshKaanAgilityCourseConfig config)
	{
		super(plugin);
		this.plugin = plugin;
		this.config = config;
		setPosition(OverlayPosition.ABOVE_CHATBOX_RIGHT);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
		panelComponent.setPreferredSize(new Dimension(PANEL_WIDTH, 0));
		loadIcons();
	}

	private void loadIcons()
	{
		for (RouteType route : RouteType.values())
		{
			for (String itemName : route.getItemNames())
			{
				itemIcons.put(itemName, loadImage(capitalise(itemName) + ".png"));
			}
		}
	}

	private BufferedImage loadImage(String fileName)
	{
		try
		{
			return ImageUtil.loadImageResource(DorgeshKaanAgilityCourse.class, "/" + fileName);
		}
		catch (Exception e)
		{
			log.error("Error loading image: {}", fileName, e);
			return null;
		}
	}

	public void updateOverlay(String item1, String item2)
	{
		if (plugin.isMissingSpanner())
		{
			clearOverlay();
			return;
		}

		this.requestedItem1 = item1;
		this.requestedItem2 = item2;
	}

	public void clearOverlay()
	{
		requestedItem1 = null;
		requestedItem2 = null;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		panelComponent.getChildren().clear();

		boolean hasRequestedItems = requestedItem1 != null || requestedItem2 != null;
		RouteType route = config.showRoutePanel() ? plugin.getCurrentRouteType() : RouteType.NONE;

		if (!hasRequestedItems && route == RouteType.NONE)
		{
			return null;
		}

		if (requestedItem1 != null)
		{
			addItemRow(requestedItem1);
		}
		if (requestedItem2 != null)
		{
			addItemRow(requestedItem2);
		}

		if (route != RouteType.NONE)
		{
			addRouteRow(route);
		}

		return super.render(graphics);
	}

	private void addItemRow(String itemName)
	{
		String displayName = capitalise(itemName);
		boolean needsGrapple = plugin.isDelicateItem(itemName) && !plugin.hasGrapple();

		Color textColor;
		if (needsGrapple)
		{
			displayName += " (needs grapple)";
			textColor = Color.RED;
		}
		else
		{
			boolean hasItem = plugin.checkInventoryForItem(itemName);
			textColor = hasItem ? Color.GREEN : Color.WHITE;
		}

		BufferedImage icon = itemIcons.get(itemName);
		BufferedImage scaledIcon = icon != null ? ImageUtil.resizeImage(icon, ICON_SIZE, ICON_SIZE) : null;

		panelComponent.getChildren().add(
				new ItemRowComponent(scaledIcon, displayName, textColor)
		);
	}

	private void addRouteRow(RouteType route)
	{
		Color colour = RouteColours.forRoute(config, route);
		panelComponent.getChildren().add(
				LineComponent.builder()
						.left("Route:")
						.right(route.getDisplayName())
						.rightColor(colour)
						.build()
		);
	}

	private static String capitalise(String s)
	{
		if (s == null || s.isEmpty())
		{
			return s;
		}
		return Character.toUpperCase(s.charAt(0)) + s.substring(1);
	}

	private static class ItemRowComponent implements LayoutableRenderableEntity
	{
		private final BufferedImage icon;
		private final String text;
		private final Color textColor;

		private Point preferredLocation = new Point();
		private Dimension preferredSize = new Dimension(PANEL_WIDTH, 0);

		ItemRowComponent(BufferedImage icon, String text, Color textColor)
		{
			this.icon = icon;
			this.text = text;
			this.textColor = textColor;
		}

		@Override
		public Rectangle getBounds()
		{
			return null;
		}

		@Override
		public void setPreferredLocation(Point point)
		{
			this.preferredLocation = point;
		}

		@Override
		public void setPreferredSize(Dimension dimension)
		{
			this.preferredSize = dimension;
		}

		@Override
		public Dimension render(Graphics2D graphics)
		{
			final int x = preferredLocation.x;
			final int y = preferredLocation.y;

			if (icon != null)
			{
				final ImageComponent imageComponent = new ImageComponent(icon);
				imageComponent.setPreferredLocation(new Point(x, y));
				imageComponent.render(graphics);
			}

			final int textX = x + ICON_SIZE + ICON_TEXT_GAP;
			final int textWidth = Math.max(0, preferredSize.width - ICON_SIZE - ICON_TEXT_GAP);

			final LineComponent lineComponent = LineComponent.builder().left(text).leftColor(textColor).build();
			lineComponent.setPreferredLocation(new Point(textX, y));
			lineComponent.setPreferredSize(new Dimension(textWidth, 0));

			final Dimension textDim = lineComponent.render(graphics);

			final int rowHeight = Math.max(ICON_SIZE, textDim != null ? textDim.height : 0);
			return new Dimension(preferredSize.width, rowHeight);
		}
	}
}
