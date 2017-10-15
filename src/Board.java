
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
				System.out.print(cells[i][j]+" ");
			}
			System.out.println();
		}
	}
}
