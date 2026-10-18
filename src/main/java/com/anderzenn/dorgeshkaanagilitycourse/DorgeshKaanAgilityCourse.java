package com.anderzenn.dorgeshkaanagilitycourse;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.Text;

import javax.inject.Inject;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@PluginDescriptor(
	name = "Dorgesh-Kaan Agility Course",
	description = "provides quality of life information to-do with the Dorgesh-Kaan agility course",
	tags = {"agility","dorgesh-kaan","dorgesh","kaan","dorgeshkaan","helper","agilityhelper","course","agilitycourse","agility course"}
)
public class DorgeshKaanAgilityCourse extends Plugin
{
	private static final String CONFIG_GROUP = "DorgeshKaanAgilityCourseConfig";
	private static final Pattern REQUEST_PATTERN = Pattern.compile("The engineer asks you to get a (\\w+) or a (\\w+)");
	private static final Pattern LAP_COUNT_PATTERN = Pattern.compile("Your Dorgesh-Kaan Agility lap count is: (\\d+)");

	private static final String GRAPPLE_ITEM_NAME_FRAGMENT = "mith grapple";
	private static final String CROSSBOW_NAME_FRAGMENT = "crossbow";
	private static final String EXCLUDED_CROSSBOW_NAME = "love crossbow";

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private ConfigManager configManager;

	@Inject
	private DorgeshKaanAgilityCourseConfig config;

	@Inject
	private ItemManager itemManager;

	@Inject
	private DorgeshKaanOverlay overlay;

	@Inject
	private SpannerWarningOverlay spannerWarningOverlay;

	@Inject
	private TurgallHighlightOverlay turgallHighlightOverlay;

	@Inject
	private CourseHighlightOverlay courseHighlightOverlay;

	public String requestedItem1 = null;
	public String requestedItem2 = null;
	private boolean wasInRegion = false;

	@Override
	protected void startUp() throws Exception
	{
		overlayManager.add(overlay);
		overlayManager.add(spannerWarningOverlay);
		overlayManager.add(turgallHighlightOverlay);
		overlayManager.add(courseHighlightOverlay);

		// Load saved items from config. The config default is "" rather than null,
		// so normalise that here - otherwise the panel tries to render a row for an
		// empty item name and throws on capitalising it.
		requestedItem1 = emptyToNull(config.requestedItem1());
		requestedItem2 = emptyToNull(config.requestedItem2());

		// Fixes https://github.com/Anderzenn/dorgesh-kaan-agility-course/issues/1
		clientThread.invokeLater(this::updateOverlay);
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(overlay);
		overlayManager.remove(spannerWarningOverlay);
		overlayManager.remove(turgallHighlightOverlay);
		overlayManager.remove(courseHighlightOverlay);
		overlay.clearOverlay();
		spannerWarningOverlay.clearWarning();
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (event.getType() != ChatMessageType.GAMEMESSAGE)
		{
			return;
		}

		String message = Text.removeTags(event.getMessage());

		Matcher requestMatcher = REQUEST_PATTERN.matcher(message);
		if (requestMatcher.find())
		{
			setRequestedItems(requestMatcher.group(1), requestMatcher.group(2));
			updateOverlay();
			return;
		}

		Matcher lapCountMatcher = LAP_COUNT_PATTERN.matcher(message);
		if (lapCountMatcher.find())
		{
			clearRequestedItems();
			overlay.clearOverlay();
			updateOverlay();
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		boolean inRegion = isInRegion();
		if (inRegion != wasInRegion)
		{
			wasInRegion = inRegion;
			updateOverlay();
		}

		if (inRegion)
		{
			updateOverlay();
		}
	}

	private void setRequestedItems(String item1, String item2)
	{
		requestedItem1 = item1;
		requestedItem2 = item2.equalsIgnoreCase("power") ? "powerbox" : item2;

		configManager.setConfiguration(CONFIG_GROUP, "requestedItem1", requestedItem1);
		configManager.setConfiguration(CONFIG_GROUP, "requestedItem2", requestedItem2);
	}

	private void clearRequestedItems()
	{
		requestedItem1 = null;
		requestedItem2 = null;

		configManager.unsetConfiguration(CONFIG_GROUP, "requestedItem1");
		configManager.unsetConfiguration(CONFIG_GROUP, "requestedItem2");
	}

	private void updateOverlay()
	{
		if (!isInRegion())
		{
			overlay.clearOverlay();
			spannerWarningOverlay.clearWarning();
			return;
		}

		if (config.highlightOption())
		{
			highlightDialogueOption();
		}

		overlay.updateOverlay(requestedItem1, requestedItem2);
	}

	private boolean isInRegion()
	{
		return client.getLocalPlayer() != null
				&& WorldPoint.fromLocal(client, client.getLocalPlayer().getLocalLocation()).getRegionID() == DorgeshKaanConstants.REGION_ID;
	}

	/**
	 * Determines which route is currently correct, based on the item(s) in the player's
	 * inventory. The delicate/grapple route additionally requires a crossbow + mith
	 * grapple equipped - without one, it's not reported as a usable route (see
	 * {@link #isItemSelectable(String)}). Delicate takes priority in the unusual case
	 * both item types are held at once (e.g. a leftover item from a previous lap).
	 */
	public RouteType getCurrentRouteType()
	{
		if (hasAnyItem(RouteType.DELICATE.getItemNames()) && hasGrapple())
		{
			return RouteType.DELICATE;
		}
		if (hasAnyItem(RouteType.HEAVY.getItemNames()))
		{
			return RouteType.HEAVY;
		}
		return RouteType.NONE;
	}

	public boolean checkInventoryForItem(String targetName)
	{
		return hasAnyItem(new String[]{targetName});
	}

	/**
	 * @return true if this item name belongs to the delicate/grapple route (capacitor, fuse, meter).
	 */
	public boolean isDelicateItem(String itemName)
	{
		return RouteType.DELICATE.containsItemName(itemName);
	}

	/**
	 * An item is "selectable" unless it's a delicate item and the player doesn't have a
	 * grapple equipped - since the grapple route can't actually be completed without one.
	 */
	public boolean isItemSelectable(String itemName)
	{
		return !isDelicateItem(itemName) || hasGrapple();
	}

	/**
	 * @return true if the player has a crossbow (any, excluding the love crossbow) equipped
	 * in the weapon slot, and a mith grapple equipped in the ammunition slot.
	 */
	public boolean hasGrapple()
	{
		ItemContainer equipment = client.getItemContainer(InventoryID.WORN);
		if (equipment == null)
		{
			return false;
		}

		Item weapon = equipment.getItem(EquipmentInventorySlot.WEAPON.getSlotIdx());
		Item ammo = equipment.getItem(EquipmentInventorySlot.AMMO.getSlotIdx());

		if (weapon == null || ammo == null || weapon.getId() <= 0 || ammo.getId() <= 0)
		{
			return false;
		}

		String weaponName = itemManager.getItemComposition(weapon.getId()).getName().toLowerCase();
		String ammoName = itemManager.getItemComposition(ammo.getId()).getName().toLowerCase();

		boolean hasCrossbow = weaponName.contains(CROSSBOW_NAME_FRAGMENT) && !weaponName.contains(EXCLUDED_CROSSBOW_NAME);
		boolean hasMithGrapple = ammoName.contains(GRAPPLE_ITEM_NAME_FRAGMENT);

		return hasCrossbow && hasMithGrapple;
	}

	private boolean hasAnyItem(String[] itemNames)
	{
		ItemContainer inventory = client.getItemContainer(InventoryID.INV);
		if (inventory == null)
		{
			return false;
		}

		for (Item item : inventory.getItems())
		{
			String name = itemManager.getItemComposition(item.getId()).getName().toLowerCase();
			for (String target : itemNames)
			{
				if (name.contains(target.toLowerCase()))
				{
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * @return true if the player does NOT currently have the Dorgesh-Kaan spanner.
	 * (Previously named {@code hasSpanner()} despite returning the opposite - renamed
	 * for clarity, behaviour unchanged.)
	 */
	public boolean isMissingSpanner()
	{
		ItemContainer inventory = client.getItemContainer(InventoryID.INV);
		if (inventory == null)
		{
			return true;
		}

		for (Item item : inventory.getItems())
		{
			if (item.getId() == ItemID.DORGESH_SPANNER)
			{
				return false;
			}
		}
		return true;
	}

	public boolean displaySpannerWarning()
	{
		return isInRegion() && isMissingSpanner();
	}

	private void highlightDialogueOption()
	{
		clientThread.invokeLater(() -> {
			Widget dialogueOptions = client.getWidget(InterfaceID.CHATMENU, 1);
			if (dialogueOptions == null || dialogueOptions.getChildren() == null)
			{
				return;
			}

			for (Widget option : dialogueOptions.getChildren())
			{
				if (option == null || option.getText() == null)
				{
					continue;
				}

				String optionText = option.getText().trim().replaceAll("\\s","");
				if (isRequestedItem(optionText))
				{
					option.setTextColor(config.highlightColour().getRGB());
				}
			}
		});
	}

	private boolean isRequestedItem(String optionText)
	{
		if (!isItemSelectable(optionText))
		{
			return false;
		}
		return optionText.equalsIgnoreCase(requestedItem1) || optionText.equalsIgnoreCase(requestedItem2);
	}

	private static String emptyToNull(String s)
	{
		return (s == null || s.isEmpty()) ? null : s;
	}

	@Provides
	DorgeshKaanAgilityCourseConfig dorgeshKaanAgilityCourseConfig(ConfigManager configManager)
	{
		return configManager.getConfig(DorgeshKaanAgilityCourseConfig.class);
	}
}
