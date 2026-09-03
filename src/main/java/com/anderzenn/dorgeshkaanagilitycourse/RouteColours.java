package com.anderzenn.dorgeshkaanagilitycourse;

import java.awt.Color;

/**
 * Picks the configured colour for a given route. This used to be a default method on
 * {@link DorgeshKaanAgilityCourseConfig}, but RuneLite's config proxy intercepts every
 * interface method call - including default ones - and since a plain default method has
 * no {@code @ConfigItem}, the proxy logged a warning and returned null instead of running
 * the method body, which caused a NullPointerException wherever the colour was used.
 * Keeping this as an ordinary static helper avoids the proxy entirely.
 */
final class RouteColours
{
	private RouteColours()
	{
		// no instances
	}

	static Color forRoute(DorgeshKaanAgilityCourseConfig config, RouteType route)
	{
		return route == RouteType.HEAVY ? config.heavyRouteColour() : config.highlightCourseColour();
	}
}
