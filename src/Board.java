public class Board {
	int[] Packed;
	int Side;

	public Board(int side) {
		Side = side;
		int size = side * side;
		int intReq = Math.ceilDiv(size, State.INT_CAP);
	}

	public int getCell(int index) {
		return Packing.getDigit(Packed[arrayIndex(index)], State.RADIX, index % State.INT_CAP);
	}

	public void setCell(int index, int state) {
		int ri = arrayIndex(index);
		Packed[ri] = Packing.setDigit(Packed[ri], State.RADIX, index % State.INT_CAP, state);
	}

	public static int arrayIndex(int digit) {
		return Math.floorDiv(digit, State.INT_CAP);
	}

	public static int calcIndex(int x, int y) {
		return y * State.INT_CAP + x;
	}

	public static int calcX(int index) {
		return index % State.INT_CAP;
	}

	public static int calcY(int index) {
		return Math.floorDiv(index, State.INT_CAP);
	}
}
