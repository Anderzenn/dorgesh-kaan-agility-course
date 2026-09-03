package com.anderzenn.dorgeshkaanagilitycourse;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

import java.awt.*;

@ConfigGroup("DorgeshKaanAgilityCourseConfig")
public interface DorgeshKaanAgilityCourseConfig extends Config
{
	/* Config Sections */
	@ConfigSection(
			name = "Course QoL Features",
			description = "Settings for enabling or disabling QoL features in the plugin",
			position = 0
	)
	String courseFeatures = "course qol features";

	@ConfigSection(
			name = "Colour Settings",
			description = "Colour settings for the various highlight options",
			position = 1
	)
	String colSettings = "colour settings";

	/*
	 * Config Items - Highlight Options
	 * NOTE: keyNames below are unchanged from the original plugin so existing users
	 * don't lose their saved settings on update.
	 */
	@ConfigItem(
			keyName = "highlightTurgall",
			name = "Highlight Turgall",
			section = courseFeatures,
			description = "Highlight Turgall (NPC ID 2295)",
			position = 0
	)
	default boolean highlightTurgall()
	{
		return false;
	}

	@ConfigItem(
			keyName = "highlightCourse",
			name = "Highlight Course",
			section = courseFeatures,
			description = "Highlight the correct route on the ground, depending on whether your item is delicate or heavy",
			position = 1
	)
	default boolean highlightCourse()
	{
		return true;
	}

	@ConfigItem(
			keyName = "highlightOption",
			name = "Highlight Dialogue Option",
			section = courseFeatures,
			description = "Highlight the correct item to pick from the boiler room dialogue",
			position = 2
	)
	default boolean highlightOption()
	{
		return true;
	}

	@ConfigItem(
			keyName = "showRoutePanel",
			name = "Show Route Panel",
			section = courseFeatures,
			description = "Show which route (delicate or heavy) to take in the info panel, based on the item you're carrying",
			position = 3
	)
	default boolean showRoutePanel()
	{
		return true;
	}

	/* Colours */
	@ConfigItem(
			keyName = "turgallHighlightColour",
			name = "Turgall highlight colour",
			section = colSettings,
			description = "Colour used to highlight Turgall",
			position = 0
	)
	default Color turgallHighlightColour()
	{
		return Color.CYAN;
	}

	@ConfigItem(
			keyName = "highlightColour",
			name = "Highlight Option Colour",
			section = colSettings,
			description = "Colour of the correct item to pick from the boilers",
			position = 1
	)
	default Color highlightColour()
	{
		return Color.CYAN;
	}

	@ConfigItem(
			keyName = "highlightCourseColour",
			name = "Grapple Route Colour (Delicate Items)",
			section = colSettings,
			description = "Colour for the grapple route's ground highlight and its label in the info panel, used when carrying a delicate item",
			position = 2
	)
	default Color highlightCourseColour()
	{
		return Color.CYAN;
	}

	@ConfigItem(
			keyName = "heavyRouteColour",
			name = "Agility Route Colour (Heavy Items)",
			section = colSettings,
			description = "Colour for the agility route's ground highlight and its label in the info panel, used when carrying a heavy item",
			position = 3
	)
	default Color heavyRouteColour()
	{
		return Color.ORANGE;
	}

	@ConfigItem(
			keyName = "courseStrokeWidth",
			name = "Stroke Width",
			section = colSettings,
			position = 4,
			description = "Width of the stroke on highlights"
	)
	default int courseStrokeWidth()
	{
		return 2;
	}

	/*
	 * Hidden Variables - used for storing the requested items across sessions.
	 */
	@ConfigItem(
			keyName = "requestedItem1",
			name = "Requested item 1",
			description = "Stores the first requested item",
			hidden = true
	)
	default String requestedItem1()
	{
		return "";
	}

	@ConfigItem(
			keyName = "requestedItem2",
			name = "Requested item 2",
			description = "Stores the second requested item",
			hidden = true
	)
	default String requestedItem2()
	{
		return "";
	}
}
