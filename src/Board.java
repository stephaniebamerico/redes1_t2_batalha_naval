
public class Board {
	public Cell[][] cells;
	
	public Board() {
		cells = new Cell[5][5];
		for (int i = 0; i < 5; i++) {
			for (int j = 0; j < 5; j++) {
				cells[i][j] = Cell.UNKNOWN;
			}
		}
	}
	
	public void printBoard() {
		for (int i = 0; i < 5; i++) {
			for (int j = 0; j < 5; j++) {
				if(cells[i][j] == Cell.UNKNOWN)
					System.out.print("?"+" ");
				else if(cells[i][j] == Cell.WATER)
					System.out.print("~"+" ");
				else if(cells[i][j] == Cell.SHIP)
					System.out.print("N"+" ");
				else if(cells[i][j] == Cell.SUNK0 || cells[i][j] == Cell.SUNK1 || cells[i][j] == Cell.SUNK2)
					System.out.print("X"+" ");
			}
			System.out.println();
		}
	}
}
