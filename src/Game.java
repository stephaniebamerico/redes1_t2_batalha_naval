import java.awt.Point;
import java.util.Scanner;

public class Game {
	
	public static void main(String[] args) {
		Player player;
		int id_target;
		boolean gameOver, myTurn;
		Point coordinates = new Point();
		Scanner input = new Scanner(System.in);
		
		// TODO: correctly read the position of the ships
		Point[][] navios = new Point[2][3];
		navios[0][0] = new Point(0,0);
		navios[0][1] = new Point(1,0);
		navios[0][2] = new Point(2,0);
		
		navios[1][0] = new Point(1,2);
		navios[1][1] = new Point(1,3);
		navios[1][2] = new Point(1,4);
		
		// TODO: test in different ip addresses
		Player[] others = new Player[3];
		for (int i = 2; i <= 4; i++) {
			others[i-2] = new Player(i, navios);
		}
		
		player = new Player(1, navios);
		if(player.getId() == 1)
			myTurn = true;
		else
			myTurn = false;
		gameOver = false;
		while(!gameOver) {
			if(myTurn) {
				System.out.println("[Iniciando turno de ataque]");
				
				System.out.println("Entre com o id do jogador alvo: ");
				id_target = input.nextInt();
				
				System.out.println("Entre com as coordenadas da célula alvo: ");
				coordinates.x = input.nextInt();
				coordinates.y = input.nextInt();
				
				player.attack(id_target, coordinates);
				System.out.println("[Turno de ataque encerrado]");
				myTurn = false;
			}
			
			try {Thread.sleep(500);}
			catch (InterruptedException e) {e.printStackTrace();}
		}
		
		input.close();
	}
}
