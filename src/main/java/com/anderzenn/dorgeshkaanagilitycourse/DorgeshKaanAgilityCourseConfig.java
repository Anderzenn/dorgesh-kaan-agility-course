package com.anderzenn.dorgeshkaanagilitycourse;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

import java.awt.*;

@ConfigGroup("DorgeshKaanAgilityCourseConfig")
public interface DorgeshKaanAgilityCourseConfig extends Config
{
	@ConfigItem(
			keyName = "highlightOption",
			name = "Highlight Option",
			description = "Highlight the correct item to pick from the boilers"
	)
	default boolean highlightOption() {
		return true;
	}

	@ConfigItem(
			keyName = "highlightColour",
			name = "Highlight Colour",
			description = "Colour of the correct item to pick from the boilers"
	)
	default Color highlightColour() {
		return Color.CYAN;
	}

	@ConfigItem(
			keyName = "requestedItem1",
			name = "Requested item 1",
			description = "Stores the first requested item",
			hidden = true
	)
	default String requestedItem1() {
		return "";
	}

	@ConfigItem(
			keyName = "requestedItem2",
			name = "Requested item 2",
			description = "Stores the second requested item",
			hidden = true
	)
	default String requestedItem2() {
		return "";
	}
}
