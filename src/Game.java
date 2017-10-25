import java.awt.Point;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Scanner;

// TODO: test in different ip addresses
// TODO: verificar se há 4 jogadores na rede
// TODO: comentarios em ingles
public class Game {
	private static TokenRing network;
	private static Player player;
	private static boolean gameOver, myTurn;
	private static ArrayList<String> bufferMessages;
	public static Scanner input;
	
	private static void init_game(Point[][] ships) {
		bufferMessages = new ArrayList<String>();
		input = new Scanner(System.in);
		/* Start Network configuration */
		// Find ip address
		InetAddress my_ip = TokenRing.getLocalIP(), next_ip = null;
		int next_id = 0, player_id = 0;
		String ip = my_ip.toString().split("/")[1];
			
		try {
			// Open file with ip addresses
	        File f = new File("src/ip_addresses.txt");
	        BufferedReader buffer = new BufferedReader(new FileReader(f));
	        String line = "";
	            
	    	// Find id based on ip address
	        while (((line = buffer.readLine()) != null) && !(line.split(" ")[0].equals("*")));
	        if(line == null || (line != null && !line.split(" ")[2].equals(ip))) {
	        	System.err.println("[init_game] Erro ao encontrar id do jogador");
	        	System.exit(0);
	        }
	        
	        player_id = Integer.parseInt(line.split(" ")[1]);
	        System.out.println("[init_game] id do jogador: "+player_id);
	        
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
				
		player = new Player(player_id-1, ships);
		
		next_id = (player_id+1)%5 + (player_id+1)/5;
		network = new TokenRing(player_id, next_id, next_ip);
		/* End Network configuration */
	}
	
	public static void main(String[] args) {
		int id_target;
		Point coordinates = new Point();
		//Scanner input = new Scanner(System.in);
		
		// TODO: correctly read the position of the ships
		Point[][] navios = new Point[2][3];
		navios[0][0] = new Point(0,0);
		navios[0][1] = new Point(1,0);
		navios[0][2] = new Point(2,0);
		
		navios[1][0] = new Point(1,2);
		navios[1][1] = new Point(1,3);
		navios[1][2] = new Point(1,4);
		
		init_game(navios);
				
		if(TokenRing.getMyId() == 1)
			myTurn = true;
		else
			myTurn = false;
		
		// Starts listening on the network
		network.listener();
		
		gameOver = false;
		while(!gameOver) {
			try {
				while(bufferMessages.size() > 0) {
					treatMessage(bufferMessages.remove(0));
				}
				if(myTurn) {
					
					System.out.println("[Iniciando turno de ataque]");
					
					System.out.println("Entre com o id do jogador alvo: ");
					id_target = input.nextInt();
					
					System.out.println("Entre com as coordenadas da célula alvo: ");
					coordinates.x = input.nextInt();
					coordinates.y = input.nextInt();
					
					attack(id_target, coordinates);
					System.out.println("[Turno de ataque encerrado]");
					myTurn = false;
				}
				
				Thread.sleep(100);
			}
			catch (InterruptedException e) {e.printStackTrace();}
		}
		
		input.close();
	}
	
	public static boolean getGameStatus(){
		return gameOver;
	}
	
	public static void gameOver(){
		System.out.println("GAME OVER");
		gameOver = true;
	}
	
	public static void nextTurn() {
		// Origem; Destino; AKI; 0;
		int next = Player.nextAvaible();
		//System.out.println("[nextTurn] Next avalible: "+next);
		if(next == TokenRing.getMyId()) {
			System.out.println("Você venceu!!!");
			gameOver();
		}
		else {
			String msg = new String(TokenRing.getMyId()+";"+next+";0;0;");
			network.speaker(true, msg);
		}
	}
	
	public static void attack(int id_target, Point coordinates) {
		// Origem; Destino; AKI; 1; X; Y; Resultado; Afundou, X_1; Y_1; X_2, Y_2, X_3, Y_3;
		if(Player.getCell(id_target-1, coordinates) == Cell.UNKNOWN) {
			String msg = new String(TokenRing.getMyId()+";"+id_target+";0;1;"
					+coordinates.x+";"+coordinates.y+";0;0,0,0,0,0,0,0;");
			network.speaker(true, msg);
		}
		else
			nextTurn();
	}
	
	public static void updateBoard(int target, Point coordinates, String result, String sank) {
		//  Origem; Destino; AKI; 2; X; Y; Resultado; Afundou, X_1, Y_1, X_2, Y_2, X_3, Y_3; Alvo;
		String msg = null;
		for (int player = 1; player <= 4; player++) {
			if(player != TokenRing.getMyId() && player != target && Player.isActive(player-1)) {
				msg = TokenRing.getMyId()+";"+player+";0;2;"+coordinates.x+";"+coordinates.y+";"
						+result+";"+sank+";"+target+";";
				network.speaker(true, msg);
			}
		}
	}
	
	public static void addBufferMessage(String msg) {
		bufferMessages.add(msg);
	}
	
	public static void treatMessage(String msg) {		
		// Message: Origem; Destino; AKI; Operação; <Parâmetros>;
		String[] mensagem = msg.split(";");
		if(mensagem.length < 4) {
			System.err.println("[treatMessage] Mensagem recebida inválida: tamanho inválido.");
			System.exit(0);
		}
		if(Integer.parseInt(mensagem[0]) == TokenRing.getMyId() || mensagem[3].equals("2")) {
			// Takes the result of an attack or update board
			Cell result = null;
			switch (mensagem[6]) {
			case "SHIP":
				result = Cell.SHIP;
				break;
			case "WATER":
				result = Cell.WATER;
				break;
			default:
				System.err.println("[treatMessage] "+msg+": mensagem inválida");
				System.exit(0);
				break;
			}
			int target = 0;
			if(Integer.parseInt(mensagem[0]) == TokenRing.getMyId())
				target = Integer.parseInt(mensagem[1]);
			else
				target = Integer.parseInt(mensagem[8]);
			Point coordinates = new Point(Integer.parseInt(mensagem[4]),
					Integer.parseInt(mensagem[5]));
			player.setBoardCell(target-1, coordinates, result);
			
			boolean origin_me = false;
			if(Integer.parseInt(mensagem[0]) == TokenRing.getMyId()) {
				updateBoard(target, coordinates, result.toString(), mensagem[7]);	
				origin_me = true;
			}
			else {
				mensagem[2] = "1"; // AKI flag
				network.speaker(false, String.join(";", mensagem)+";");
			}
			mensagem = mensagem[7].split(","); // Afundou, X_1, Y_1, X_2, Y_2, X_3, Y_3;
			if(mensagem[0].equals("1")) {
				Point[] t = new Point[3];
				for (int i = 0, x = 1; i < 3; i++, x+=2) {
					t[i] = new Point(Integer.parseInt(mensagem[x]), Integer.parseInt(mensagem[x+1]));					
				}
				player.shipSank(target-1, t);
			}
			
			if(origin_me) {
				// Now that you've updated everyone on the outcome of the attack, pass the token
				nextTurn();
			}
		}			
		else {
			switch(mensagem[3]) {
			case "0": // Received token: Origin; Destino; AKI; 0;
				mensagem[2] = "1"; // AKI flag
				msg = String.join(";", mensagem)+";";
				network.speaker(false, msg);
				
				myTurn = true;
				break;
			case "1": // Origem; Destino; AKI; 1; X; Y; Resultado, Afundou, X_1, Y_1, X_2, Y_2, X_3, Y_3;
				mensagem[2] = "1"; // AKI flag
				Point coordinates = new Point(Integer.parseInt(mensagem[4]),
						Integer.parseInt(mensagem[5]));
				mensagem[6] = player.getResult(coordinates); // Result flag
				if(mensagem[6].equals(Cell.SHIP.toString())) {
					// Hit the ship
					mensagem[7] = player.getShipStatus(coordinates);
					System.out.println("[treatMessage] Navio nas coordenadas ("+coordinates.x+","+coordinates.y+") foi atingido.");
				}
				
				msg = String.join(";", mensagem);
				network.speaker(false, msg);
				break;
			default:
				System.err.println("[treatMessage] "+msg+": operação inválida");
				System.exit(0);
				break;
			}
		}
	}
}
