package com.anderzenn.dorgeshkaanagilitycourse;

/**
 * The two routes through the Dorgesh-Kaan agility course, and which route is correct
 * is determined entirely by which item the player is currently carrying:
 * <ul>
 *     <li>{@link #DELICATE} - capacitor / fuse / meter -&gt; the grapple (pylon) route,
 *         which requires a crossbow + mith grapple equipped</li>
 *     <li>{@link #HEAVY} - cog / lever / powerbox -&gt; the agility (cable/wall/tunnel/ladder) route</li>
 * </ul>
 * Per the OSRS Wiki: "The heavy items can only be taken back via the Agility route,
 * while the delicate items can only be taken back via the grapple route." The original
 * plugin had this backwards (its variable names and comments called capacitor/fuse/meter
 * the "agility route" items) - the object IDs below have been swapped to match reality,
 * confirmed against the Wiki's obstacle lists: the grapple route is 3 repeated pylons
 * (a single object ID), while the agility route is a chain of distinct obstacles
 * (cable x2 variants, ladder, jutting wall, tunnel - five distinct object IDs).
 * <p>
 * This single enum replaces what used to be three separately-maintained copies of the
 * same item/object-ID lists spread across the overlay and panel classes.
 */
public enum RouteType
{
	DELICATE(
			"Grapple Route",
			new String[]{"capacitor", "fuse", "meter"},
			new int[]{22664}
	),
	HEAVY(
			"Agility Route",
			new String[]{"cog", "lever", "powerbox"},
			new int[]{22569, 22572, 22564, 22552, 22557}
	),
	NONE("No Item", new String[0], new int[0]);

	private final String displayName;
	private final String[] itemNames;
	private final int[] objectIds;

	RouteType(String displayName, String[] itemNames, int[] objectIds)
	{
		this.displayName = displayName;
		this.itemNames = itemNames;
		this.objectIds = objectIds;
	}

	public String getDisplayName()
	{
		return displayName;
	}

	/** Item name fragments (lowercase) that identify this route's item in the inventory. */
	public String[] getItemNames()
	{
		return itemNames;
	}

	/** Game/ground object IDs that make up this route's highlighted path. */
	public boolean containsObjectId(int id)
	{
		for (int val : objectIds)
		{
			if (val == id)
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Exact (case-insensitive) match against this route's item names - used for
	 * checking dialogue option text and requested-item strings, which are already
	 * single, normalised words (unlike inventory item names, which need substring matching).
	 */
	public boolean containsItemName(String name)
	{
		if (name == null)
		{
			return false;
		}
		for (String candidate : itemNames)
		{
			if (candidate.equalsIgnoreCase(name))
			{
				return true;
			}
		}
		return false;
	}
}
