package com.anderzenn.dorgeshkaanagilitycourse;

/**
 * Shared, plugin-wide constants for the Dorgesh-Kaan agility course.
 * Centralised here so the overlays and the main plugin class don't each
 * keep their own copies of the same magic numbers.
 */
final class DorgeshKaanConstants
{
	private DorgeshKaanConstants()
	{
		// no instances
	}

	/**
	 * Region ID shared by all three floors of the course. Note that inside the
	 * instanced template, the raw region ID reported by {@code WorldPoint.fromLocalInstance}
	 * stays 10833 regardless of floor - the actual floor has to be read from the
	 * player's resolved {@code getWorldLocation()} plane instead (0 = lower, 2 = middle, 3 = upper).
	 */
	static final int REGION_ID = 10833;

	/** Plane the boiler-room floor (where the course objects/ground items live) resolves to. */
	static final int COURSE_PLANE = 3;

	/** NPC ID for Turgall, the engineer who hands out the spanner and item requests. */
	static final int TURGALL_NPC_ID = 2295;

	/** Ladder object ID on the ground floor - kept for future use (e.g. fall recovery guidance). */
	static final int GROUND_FLOOR_LADDER_ID = 22600;
}
