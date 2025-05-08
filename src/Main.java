import java.util.Scanner;

// NOTE: the board may display weirdly on fonts which:
// - are not monospace
// - don't support unicode

public class Main {
	static Board Board = new Board(9);
	static int PlayerTurn = Consts.STATE_BLACK;
	static Scanner StdIn = new Scanner(System.in);

	static int BlackCaptures = 0;
	static int WhiteCaptures = 0;

	public static void main(String[] args) {
		// no "win/lose" state was mentioned
		// so, I'll assume it can't happen
		// I don't know how to play Go after all
		while (true) {
			playTurn();
			printScores();

			PlayerTurn = Consts.stateAdversary(PlayerTurn);
		}
	}

	public static void printScores() {
		int[] areaScore = Board.territoryScore();
		int[] pieceCount = Board.countPieces();

		int blackPieces = pieceCount[Consts.STATE_BLACK - 1];
		int blackTerritory = areaScore[Consts.STATE_BLACK - 1];
		int blackScore = blackPieces + blackTerritory + BlackCaptures;

		System.out.println(
			Consts.STATE_PIECES[Consts.STATE_BLACK]
				+ " Black score: "
				+ blackScore
				+ " (territory: "
				+ blackTerritory
				+ ", pieces: "
				+ blackPieces
				+ ", captures: "
				+ BlackCaptures
				+ ")"
		);

		int whitePieces = pieceCount[Consts.STATE_WHITE - 1];
		int whiteTerritory = areaScore[Consts.STATE_WHITE - 1];
		int whiteScore = whitePieces + whiteTerritory + WhiteCaptures;

		System.out.println(
			Consts.STATE_PIECES[Consts.STATE_WHITE]
				+ " White score: "
				+ whiteScore
				+ " (territory: "
				+ whiteTerritory
				+ ", pieces: "
				+ whitePieces
				+ ", captures: "
				+ WhiteCaptures
				+ ")"
		);
	}

	public static void playTurn() {
		String piece = Consts.STATE_PIECES[PlayerTurn];

		System.out.println("    Playing: " + piece + " " + Consts.STATE_NAMES[PlayerTurn]);
		System.out.println(Board);
		System.out.println("Choose a coord to put the " + piece + " piece:");
		System.out.print("X: ");

		while (true) {
			int x = StdIn.nextInt();

			System.out.print("Y: ");

			int y = StdIn.nextInt();

			System.out.println();

			int location = Board.calcIndex(x, y);
			int validation = Board.validPosition(location);

			if (x >= Board.Side || y >= Board.Side) {
				validation = Consts.ERROR_OUT_OF_BOUNDS;
			}

			if (validation == Consts.SUCCESS) {
				Board.setCell(location, PlayerTurn);

				int[] captures = Board.captureGroups();
				BlackCaptures += captures[Consts.STATE_BLACK - 1];
				WhiteCaptures += captures[Consts.STATE_WHITE - 1];

				return;
			}

			System.out.print("Failed to place piece: " + Consts.ERROR_MESSAGES[validation] + "\nX: ");
		}

	}
}