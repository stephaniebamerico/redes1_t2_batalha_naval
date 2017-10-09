import java.awt.Point;

public class Player {
	private int id;
	private Board[] boards = null;
	private Point[][] ships = null;
	
	public Player(int id, Point[][] ships) {
		this.id = id;
		this.ships = ships;
		
		// Initializes the player's board
		boards = new Board[5];
		for (int i = 0; i < 5; i++) {
			boards[i] = new Board();
		}
		for (int i = 0; i < 5; i++) {
			for (int j = 0; j < 5; j++) {
				boards[0].cells[i][j] = Cell.WATER;
			}
		}
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 3; j++) {
				boards[0].cells[ships[i][j].x][ships[i][j].y] = Cell.SHIP;
			}
		}
	}
	
	public int getId() {
		return this.id;
	}
}
