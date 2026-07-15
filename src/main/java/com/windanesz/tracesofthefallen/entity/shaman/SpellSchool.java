package com.windanesz.tracesofthefallen.entity.shaman;

/**
 * Represents the three distinct schools of magic practiced by Goblin Shamans.
 * The school mastery of a shaman is determined by the color and count of their crest feathers:
 * <ul>
 *   <li>{@link #ANIMISM}: Blue Feathers (Variant 0: AAA)</li>
 *   <li>{@link #SOUL_FLAME}: Green Feathers (Variant 6: BBB)</li>
 *   <li>{@link #PYROMANCY}: Red Feathers (Variant 9: CCC)</li>
 * </ul>
 */
public enum SpellSchool {
	ANIMISM(0, "Animism"),
	SOUL_FLAME(1, "Soul Flame"),
	PYROMANCY(2, "Pyromancy"),
	SUPPORT(3, "Support");

	private final int schoolId;
	private final String displayName;

	SpellSchool(int schoolId, String displayName) {
		this.schoolId = schoolId;
		this.displayName = displayName;
	}

	public int getSchoolId() {
		return this.schoolId;
	}

	public String getDisplayName() {
		return this.displayName;
	}
}
