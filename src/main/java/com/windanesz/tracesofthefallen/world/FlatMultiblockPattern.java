package com.windanesz.tracesofthefallen.world;

import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class FlatMultiblockPattern {

	private final List<BlockPos> positions;

	private FlatMultiblockPattern(List<BlockPos> positions) {
		this.positions = Collections.unmodifiableList(new ArrayList<>(positions));
	}

	public boolean matches(BlockPos origin, Predicate<BlockPos> positionValidator) {
		for (BlockPos offset : positions) {
			if (!positionValidator.test(origin.add(offset))) {
				return false;
			}
		}
		return true;
	}

	public void forEachPosition(BlockPos origin, Consumer<BlockPos> consumer) {
		for (BlockPos offset : positions) {
			consumer.accept(origin.add(offset));
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {

		private final List<BlockPos> positions = new ArrayList<>();

		public Builder addSquare(int centerX, int centerZ, int radius) {
			for (int x = centerX - radius; x <= centerX + radius; x++) {
				for (int z = centerZ - radius; z <= centerZ + radius; z++) {
					BlockPos offset = new BlockPos(x, 0, z);
					if (!positions.contains(offset)) {
						positions.add(offset);
					}
				}
			}
			return this;
		}

		public Builder addRing(int centerX, int centerZ, int radius) {
			for (int x = centerX - radius; x <= centerX + radius; x++) {
				for (int z = centerZ - radius; z <= centerZ + radius; z++) {
					if (Math.abs(x - centerX) == radius || Math.abs(z - centerZ) == radius) {
						BlockPos offset = new BlockPos(x, 0, z);
						if (!positions.contains(offset)) {
							positions.add(offset);
						}
					}
				}
			}
			return this;
		}

		public FlatMultiblockPattern build() {
			return new FlatMultiblockPattern(positions);
		}
	}
}
