import java.awt.Point;

public class Player {
	private static int id;
	private static int[] sunkenShips;
	private static Board[] boards = null;
	private static boolean[] activePlayers;
	private static Point[][] shipsCoordinates = null;
	
	public Player(int id, Point[][] ships) {
		Player.id = id;
		Player.shipsCoordinates = ships;
		
		sunkenShips = new int[4];
		activePlayers = new boolean[4];
		for (int player = 0; player < sunkenShips.length; player++) {
			sunkenShips[player] = 0;
			activePlayers[player] = true;
		}
		
		boards = new Board[4];
		for (int player = 0; player < boards.length; player++)  {
			boards[player] = new Board();
		}
		
		// Initializes the player's board
		for (int i = 0; i < 5; i++) {
			for (int j = 0; j < 5; j++) {
				boards[id].cells[i][j] = Cell.WATER;
			}
		}
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 3; j++) {
				boards[id].cells[shipsCoordinates[i][j].x][shipsCoordinates[i][j].y] = Cell.SHIP;
			}
		}
		boards[id].printBoard();
	}
	
	public static Cell getCell(int player, Point coordinates) {
		return boards[player].cells[coordinates.x][coordinates.y];		
	}
	
	public static int nextAvaible() {
		int next = id;
		for (int player = 1; player < activePlayers.length; player++) {
			next = (id+player)%activePlayers.length;
			if(activePlayers[next]) {
				System.err.println("meu id: "+(id+1)+" prox: "+(next+1));
				return (next+1);
			}
		}
		return id;
	}
	
	public void setBoardCell(int player, Point coordinates, Cell status) {
		boards[player].cells[coordinates.x][coordinates.y] = status;
		System.out.println("[Resultado do ataque]");
		boards[player].printBoard();
	}
	
	public String getResult(Point coordinates){
		Cell c = boards[id].cells[coordinates.x][coordinates.y];
		if(c == Cell.SHIP)
			boards[id].cells[coordinates.x][coordinates.y] = Cell.SUNK0;
		return c.toString();
	}
	
	public String getShipStatus(Point coordinates){
		Point[] c = null;
		// Find the coordinates of the ship hit
		for(Point[] p : shipsCoordinates) {
			for (int i = 0; i < p.length && c == null; i++) {
				if(p[i].x == coordinates.x && p[i].y == coordinates.y){
					c = p;
				}	
			}
		}
		// Creates the string with the coordinates
		String result = "0", temp = "";
		int i;
		for (i = 0; i < c.length && boards[id].cells[c[i].x][c[i].y] == Cell.SUNK0; i++)
			temp += ","+c[i].x+","+c[i].y;
		if (i == c.length) {
			result = "1"+temp+";";
			System.out.println("[getShipStatus] Seu navio foi afundado.");
			if(++sunkenShips[id] == 2) {
				activePlayers[id] = false;
				Game.gameOver();
			}
		}
		else
			result = "0,0,0,0,0,0,0;";
		System.out.println("[result] "+result);
		return result;
	}
	
	public void shipSank(int player, Point[] coordinates) {
		++sunkenShips[player];
		System.out.print("[shipSank] "+sunkenShips[player]+" navio afundou nas coordenadas:");
		Cell c = null;
		if(sunkenShips[player] == 1)
			c = Cell.SUNK1;
		else
			c = Cell.SUNK2;
		for (Point shipCoordinates : coordinates) {
			System.out.print(shipCoordinates.x+", "+shipCoordinates.y+"; ");
			boards[player].cells[shipCoordinates.x][shipCoordinates.y] = c;
		}
		System.out.println();
		boards[player].printBoard();
		if(sunkenShips[player] == 2) {
			System.out.println("Jogador"+(player+1)+" teve todos seus navios afundados e está fora do jogo.");
			activePlayers[player] = false;
		}
	}
}
