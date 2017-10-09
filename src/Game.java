import java.awt.Point;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Scanner;

public class Game {
	private static TokenRing network = null;
	private static Player player = null;
	
	private static void init_game(int player_id, Point[][] ships) {
		/* Start Network configuration */
		// Find ip address
		InetAddress my_ip = TokenRing.getLocalIP(), next_ip = null;
		int next_id = 0;
		
		if (player_id == 0) {
			// Players have different ip address
			String ip = my_ip.toString().split("/")[1];
			
			try {
				// Open file with ip addresses
	            File f = new File("src/ip_addresses.txt");
	            BufferedReader buffer = new BufferedReader(new FileReader(f));
	            String line = "";
	            
	            // Find id based on ip address
	            while (((line = buffer.readLine()) != null) && !(line.split(" ")[1].equals(ip)));
	            player_id = Integer.parseInt(line.split(" ")[0]);
	            
	            // Find the next player's ip address
	            if ((line = buffer.readLine()) != null) {
	            	line = line.split(" ")[1];
	            }
	            else {
	            	buffer.close();
	            	buffer = new BufferedReader(new FileReader(f));
	            	line = buffer.readLine().split(" ")[1];
	            }
	            next_ip = InetAddress.getByName(line);
	         
	            buffer.close();
	        } catch (IOException e) {e.printStackTrace();}
		}
		else {
			// Players have the same ip address
			next_ip = my_ip;
		}
		
		player = new Player(player_id, ships);
		
		next_id = (player_id+1)%5 + (player_id+1)/5;
		network = new TokenRing(player_id, next_ip, 10100+player_id, 10100+next_id);
		/* End Network configuration */
	}
	
	public static void main(String[] args) {
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
		
		init_game(1, navios);
		
		// TODO: test in different ip addresses		
		if(player.getId() == 1)
			myTurn = true;
		else
			myTurn = false;
		
		// Starts listening on the network
		network.listener();
		
		gameOver = false;
		while(!gameOver) {
			if(myTurn) {
				System.out.println("[Iniciando turno de ataque]");
				
				System.out.println("Entre com o id do jogador alvo: ");
				id_target = input.nextInt();
				
				System.out.println("Entre com as coordenadas da célula alvo: ");
				coordinates.x = input.nextInt();
				coordinates.y = input.nextInt();
				
				attack(player, id_target, coordinates);
				System.out.println("[Turno de ataque encerrado]");
				myTurn = false;
			}
			
			try {Thread.sleep(500);}
			catch (InterruptedException e) {e.printStackTrace();}
		}
		
		input.close();
	}
	
	public static void treatMessage(String msg) {
		System.out.println("Recebido por player "+player.getId()+": "+msg);
	}
	
	public static void attack(Player player, int id_target, Point coordinates) {
		String msg = new String(player.getId()+";"+id_target+";1;"+coordinates.x+";"+coordinates.y+";");
		System.out.println(msg);
		network.speaker(msg);
	}
	
}
