public final class Consts {
	// Cell states.
	public static final int STATE_EMPTY = 0;
	public static final int STATE_BLACK = 1;
	public static final int STATE_WHITE = 2;

	public static final String[] STATE_PIECES = {null, "○", "◉"};
	public static final String[] STATE_NAMES = {"No one", "Black", "White"};

	// For radix packed int arrays.
	public static final int RADIX = 3;

	// The amount of STATE enums a single integer can hold.
	public static final int INT_CAP = Packing.intCapacity(Consts.RADIX);

	// Error states for fallible operations.
	// I don't like the exception system,
	// as exceptions require stack unwinding which is super slow
	// objects are on the heap, unless java's escape analysis says otherwise
	public static final int SUCCESS = 0; //success state
	public static final int ERROR_OCCUPIED = 1;
	public static final int ERROR_OUT_OF_BOUNDS = 2;
	//public static final int ERROR_SURROUNDED = 3; //unused

	public static final String[] ERROR_MESSAGES = {
		"operation completed successfully",
		"location is already occupied",
		"location is out of bounds",
		null, //"location is surrounded by adversary pieces", //unused
	};

	public static int stateAdversary(int state) {
		return (state % 2) + 1;
	}
}
