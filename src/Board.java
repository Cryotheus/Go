import java.util.ArrayList;

public class Board {
	// Array of packed integers
	int[] Packed;

	// Side-length of the board.
	// to get the cell count: `Side * Side`
	int Side;

	//get the appropriate string: `GRID_STRINGS[state in 0..=3][y in 0..=2][x in 0..=2]`
	public static final String[][][] GRID_STRINGS = {
		{
			{" ┌─", "─┬─", "─┐"},
			{" ├─", "─┼─", "─┤"},
			{" └─", "─┴─", "─┘"},
		},
		{
			{" ○─", "─○─", "─○"},
			{" ○─", "─○─", "─○"},
			{" ○─", "─○─", "─○"},
		},
		{
			{" ◉─", "─◉─", "─◉"},
			{" ◉─", "─◉─", "─◉"},
			{" ◉─", "─◉─", "─◉"},
		},
	};

	public Board(int side) {
		Side = side;
		int size = side * side;

		//how many ints are needed to store `size` x STATE enums
		int intReq = Math.ceilDiv(size, Consts.INT_CAP);
		Packed = new int[intReq];
	}

	//-1 for min/left/top
	//0 for max/bottom/right
	//1 for in-between
	//
	//coord should not be >= Side
	private int align(int coord) {
		if (coord == 0) {
			return 0;
		} else if (coord == (Side - 1)) {
			return 2;
		}

		return 1;
	}

	// Creates a pretty-print of the Board.
	public static int arrayIndex(int digit) {
		return Math.floorDiv(digit, Consts.INT_CAP);
	}

	public int getCell(int index) {
		return Packing.getDigit(Packed[arrayIndex(index)], Consts.RADIX, index % Consts.INT_CAP);
	}

	public void setCell(int index, int state) {
		int ri = arrayIndex(index);
		Packed[ri] = Packing.setDigit(Packed[ri], Consts.RADIX, index % Consts.INT_CAP, state);
	}

	// Returns `true` if the group at `rootIndex` of state `friendlyState` has an empty cell adjacent allowing breathing room.
	private boolean captureGroupsRecurse(ArrayList<Integer> indicesTodo, ArrayList<Integer> inGroup, int rootIndex, int friendlyState) {
		ArrayList<Integer> neighbors = orthogonalIndices(rootIndex);
		boolean foundEmpty = false;

		for (int neighborIndex : neighbors) {
			int neighborState = getCell(neighborIndex);

			if (neighborState == Consts.STATE_EMPTY) {
				foundEmpty = true;
			} else if (neighborState == friendlyState) {
				boolean removed = false;

				for (int todoIndex = 0; todoIndex < indicesTodo.size(); todoIndex++) {
					if (indicesTodo.get(todoIndex) == neighborIndex) {
						removed = true;

						//we should only ever check a cell once
						indicesTodo.remove(todoIndex);

						break;
					}
				}

				//if the cell was already checked
				//don't do anything to it
				if (!removed)
					continue;

				inGroup.add(neighborIndex);

				if (!indicesTodo.isEmpty()) {
					//recursion here
					foundEmpty |= captureGroupsRecurse(indicesTodo, inGroup, neighborIndex, friendlyState);
				}
			}
		}

		return foundEmpty;
	}

	public int captureGroupsFor(ArrayList<Integer> indicesTodo, int friendlyState) {
		int captures = 0;

		while (!indicesTodo.isEmpty()) {
			ArrayList<Integer> inGroup = new ArrayList<>();
			int rootIndex = indicesTodo.removeLast();

			inGroup.add(rootIndex);

			if (!captureGroupsRecurse(indicesTodo, inGroup, rootIndex, friendlyState)) {
				for (int index : inGroup) {
					captures += 1;
					setCell(index, Consts.STATE_EMPTY);
				}
			}
		}

		return captures;
	}

	// Returns the amount of captures `[STATE_BLACK, STATE_WHITE]` respectively.
	// This mutates the board, removing suffocated pieces.
	public int[] captureGroups() {
		ArrayList<Integer> indicesBlack = new ArrayList<>();
		ArrayList<Integer> indicesWhite = new ArrayList<>();

		for (int index = 0; index < Side * Side; index++) {
			switch (getCell(index)) {
				case Consts.STATE_EMPTY:
					break;
				case Consts.STATE_BLACK:
					indicesBlack.add(index);
					break;
				case Consts.STATE_WHITE:
					indicesWhite.add(index);
					break;
			}
		}

		//yes, intentionally reversed
		//as the value returned is how many pieces
		//of the `friendlyState` were removed
		return new int[]{
			captureGroupsFor(indicesWhite, Consts.STATE_WHITE), //white's loss, black's captures
			captureGroupsFor(indicesBlack, Consts.STATE_BLACK), //black's loss, white's captures
		};
	}

	public int calcIndex(int x, int y) {
		return y * Side + x;
	}

	public int calcX(int index) {
		return index % Side;
	}

	public int calcY(int index) {
		return Math.floorDiv(index, Side);
	}

	public int[] countPieces() {
		int[] counts = new int[2];

		for (int index = 0; index < Side * Side; index++) {
			int state = getCell(index);

			if (state != Consts.STATE_EMPTY) {
				counts[state - 1] += 1;
			}
		}

		return counts;
	}

	public String displayString() {
		int index = 0;
		StringBuilder builder = new StringBuilder("◤");

		for (int x = 0; x < Side; x++) {
			String xStr = Integer.toString(x);
			int leadingSpaces = Integer.max(3 - xStr.length(), 0);

			if (leadingSpaces != 0)
				builder.append(" ".repeat(leadingSpaces));

			builder.append(xStr);
		}

		builder.append('\n');

		for (int y = 0; y < Side; y++) {
			for (int x = 0; x < Side; x++) {
				int horizontal = align(x);
				int vertical = align(y);

				int state = getCell(index);
				String tile = GRID_STRINGS[state][vertical][horizontal];

				if (x == 0) {
					builder.append(y);
					builder.append(" ");
				}

				builder.append(tile);

				index++;
			}

			builder.append('\n');
		}

		return builder.toString();
	}

	public int[] orthogonalCells(int index) {
		ArrayList<Integer> indices = orthogonalIndices(index);
		int[] cells = new int[indices.size()];

		for (int i = 0; i < indices.size(); i++) {
			cells[i] = getCell(indices.get(i));
		}

		return cells;
	}

	public ArrayList<Integer> orthogonalIndices(int index) {
		ArrayList<Integer> states = new ArrayList<>(4);

		int x = calcX(index);
		int y = calcY(index);

		for (int xOff = -1; xOff <= 1; xOff += 2) {
			int xTrn = x + xOff;

			if (xTrn >= 0 && xTrn < Side) {
				states.add(calcIndex(xTrn, y));
			}
		}

		for (int yOff = -1; yOff <= 1; yOff += 2) {
			int yTrn = y + yOff;

			if (yTrn >= 0 && yTrn < Side) {
				states.add(calcIndex(x, yTrn));
			}
		}

		return states;
	}

	// returns -1 if the area is surrounded by both groups
	private int territoryScoreRecurse(ArrayList<Integer> indicesTodo, int rootIndex, int friendlyState) {
		int adversary = Consts.stateAdversary(friendlyState);
		int territory = 0;
		ArrayList<Integer> neighbors = orthogonalIndices(rootIndex);

		for (int neighborIndex : neighbors) {
			int neighborState = getCell(neighborIndex);

			if (neighborState == adversary) {
				return -1;
			} else if (neighborState == Consts.STATE_EMPTY) {
				boolean removed = false;

				for (int todoIndex = 0; todoIndex < indicesTodo.size(); todoIndex++) {
					if (indicesTodo.get(todoIndex) == neighborIndex) {
						removed = true;

						//we should only ever check a cell once
						indicesTodo.remove(todoIndex);

						break;
					}
				}

				//if the cell was already checked
				//don't do anything to it
				if (!removed)
					continue;

				if (territory != -1) {
					territory++;
				}

				if (!indicesTodo.isEmpty()) {
					//recursion here
					int result = territoryScoreRecurse(indicesTodo, neighborIndex, friendlyState);

					if (result == -1 || territory == -1) {
						territory = -1;
					} else {
						territory += result;
					}
				}
			}
		}

		return territory;
	}

	private int territoryScoreSum(ArrayList<Integer> indicesTodo, int friendlyState) {
		int territory = 0;

		while (!indicesTodo.isEmpty()) {
			int rootIndex = indicesTodo.removeLast();
			int result = territoryScoreRecurse(indicesTodo, rootIndex, friendlyState);

			if (result != -1) {
				territory += result + 1;
			}
		}

		return territory;
	}

	// Returns the count of empty cells encompassed by `[STATE_BLACK, STATE_WHITE]` respectively.
	public int[] territoryScore() {
		boolean blackExists = false;
		boolean whiteExists = false;

		ArrayList<Integer> indicesTodoBlack = new ArrayList<>();
		ArrayList<Integer> indicesTodoWhite = new ArrayList<>();

		for (int index = 0; index < Side * Side; index++) {
			switch (getCell(index)) {
				case Consts.STATE_EMPTY:
					indicesTodoBlack.add(index);
					indicesTodoWhite.add(index);
					break;
				case Consts.STATE_BLACK:
					blackExists = true;
					break;
				case Consts.STATE_WHITE:
					whiteExists = true;
					break;
			}
		}

		if (blackExists && whiteExists) {
			return new int[]{
				territoryScoreSum(indicesTodoBlack, Consts.STATE_BLACK),
				territoryScoreSum(indicesTodoWhite, Consts.STATE_WHITE),
			};
		} else {
			return new int[]{0, 0};
		}
	}

	// Returns an ERROR enum, or the SUCCESS enum
	public int validPosition(int index) {
		if (getCell(index) != Consts.STATE_EMPTY)
			return Consts.ERROR_OCCUPIED;

		return Consts.SUCCESS;
	}

	@Override
	public String toString() {
		return displayString();
	}
}
