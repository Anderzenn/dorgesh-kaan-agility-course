package com.anderzenn.dorgeshkaanagilitycourse;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
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
	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	ConfigManager configManager;

	@Inject
	DorgeshKaanAgilityCourseConfig config;

	@Inject
	private DorgeshKaanOverlay overlay;

	@Inject
	private SpannerWarningOverlay spannerWarningOverlay;

	private static final int REGION_ID = 10833;
	private static final int SPANNER_ITEM_ID = ItemID.SPANNER;

	private static final Pattern REQUEST_PATTERN = Pattern.compile("The engineer asks you to get a (\\w+) or a (\\w+)");

	//public final Map<String, String> itemPaths = new HashMap<>();
	public String requestedItem1 = null;
	public String requestedItem2 = null;
	private boolean wasInRegion = false;

	@Override
	protected void startUp() throws Exception
	{
		overlayManager.add(overlay);
		overlayManager.add(spannerWarningOverlay);
		//initializeItemPaths();

		// Load saved items in config
		requestedItem1 = config.requestedItem1();
		requestedItem2 = config.requestedItem2();

		updateOverlay();
	}

	@Override
	protected void shutDown() throws Exception {
		overlayManager.remove(overlay);
		overlayManager.remove(spannerWarningOverlay);
		overlay.clearOverlay();
		spannerWarningOverlay.clearWarning();
	}

	/*
	Not currently being used, saved for a future feature where it highlights
	the path you need to take based on if the item you have is heavy or delicate.
	I've kept it in the code for the sake of me actually remembering lol

	private void initializeItemPaths() {
		// Maps items to paths, based on if they're delicate or heavy.
		itemPaths.put("cog", "heavy");
		itemPaths.put("lever", "heavy");
		itemPaths.put("power box", "heavy"); // Power Box
		itemPaths.put("capacitor", "delicate");
		itemPaths.put("fuse", "delicate");
		itemPaths.put("meter", "delicate");
	}
	 */

	private void clearRequestedItems() {
		requestedItem1 = null;
		requestedItem2 = null;

		// Clear items stored in config
		configManager.unsetConfiguration("DorgeshKaanAgilityCourseConfig", "requestedItem1");
		configManager.unsetConfiguration("DorgeshKaanAgilityCourseConfig", "requestedItem2");
	}

	@Subscribe
	public void onChatMessage(ChatMessage event) {
		if (event.getType() != ChatMessageType.GAMEMESSAGE)	return;

		Matcher requestMatcher = REQUEST_PATTERN.matcher(event.getMessage());
		if (requestMatcher.find()) {
			requestedItem1 = requestMatcher.group(1);
			requestedItem2 = requestMatcher.group(2);

			if (requestedItem2.equalsIgnoreCase("power")) requestedItem2 = "power box";

			// Save requested items to config
			configManager.setConfiguration("DorgeshKaanAgilityCourseConfig", "requestedItem1", requestedItem1);
			configManager.setConfiguration("DorgeshKaanAgilityCourseConfig", "requestedItem2", requestedItem2);

			updateOverlay();
			return;
		}

		if (event.getMessage().contains("Your Dorgesh-Kaan Agility lap count is: ")) {
			clearRequestedItems();
			overlay.clearOverlay();
			updateOverlay();
		}
	}

	@Subscribe
	public void onGameTick(GameTick event) {
		boolean inRegion = isInRegion();
		if (inRegion != wasInRegion) {
			wasInRegion = inRegion;
			updateOverlay();
		}

		if (isInRegion()) {
			updateOverlay();
		}
	}

	private void updateOverlay() {
		if (!isInRegion()) {
			overlay.clearOverlay();
			spannerWarningOverlay.clearWarning();
			return;
		}

		if (config.highlightOption()) highlightOption();

		overlay.updateOverlay(requestedItem1, requestedItem2);
		spannerWarningOverlay.updateWarning(displaySpannerWarning());
	}

	private boolean isInRegion() {
		return client.getLocalPlayer() != null && WorldPoint.fromLocal(client, client.getLocalPlayer().getLocalLocation()).getRegionID() == REGION_ID;
	}

	private boolean hasSpanner() {
		ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
		if (inventory != null) {
			for (Item item : inventory.getItems()) {
				if (item.getId() == SPANNER_ITEM_ID) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean displaySpannerWarning() {
		return isInRegion() && !hasSpanner();
	}

	private void highlightOption() {
		clientThread.invokeLater(() -> {
			Widget dialogueOptions = client.getWidget(InterfaceID.DIALOG_OPTION, 1);

			if (dialogueOptions == null) {
				return;
			}

			if (dialogueOptions.getChildren() == null) {
				return;
			}

			for (Widget option : dialogueOptions.getChildren()) {
				if (option != null && option.getText() != null) {
					String optionText = option.getText().trim().toLowerCase();

					if (isRequestedItem(optionText)) {
						option.setTextColor(config.highlightColour().getRGB()); // Colour the correct dialogue option
					}
				}
			}
		});
	}

	private boolean isRequestedItem(String optionText) {
		return optionText != null && (optionText.equalsIgnoreCase(requestedItem1) || optionText.equalsIgnoreCase(requestedItem2));
	}

	@Provides
	DorgeshKaanAgilityCourseConfig dorgeshKaanAgilityCourseConfig(ConfigManager configManager) {
		return configManager.getConfig(DorgeshKaanAgilityCourseConfig.class);
	}
}
