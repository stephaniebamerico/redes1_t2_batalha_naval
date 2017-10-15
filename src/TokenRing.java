import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class TokenRing {
	private static boolean waitingAKI;
	private DatagramSocket socketListener, socketSpeaker;
	private static int my_id, next_id;
	private int my_port, next_port;
	private InetAddress next_ip;
	
	public TokenRing(int my_id, int next_id, InetAddress next_ip) {
		TokenRing.waitingAKI = false;
		TokenRing.my_id = my_id;
		TokenRing.next_id = next_id;
		this.next_ip = next_ip;
		this.my_port = 10100+my_id;
		this.next_port = 10100+next_id;
	}
	
	public void listener() {
		byte[] receive_msg = new byte[1024]; // buffer for messages
		
		// Creates the socket
		try {socketListener = new DatagramSocket(my_port);} 
		catch (SocketException e) {e.printStackTrace();}
		
		// Starts listening to messages
		new Thread() {
			@Override
			public void run() {
				while(true) {
					DatagramPacket packet = new DatagramPacket(receive_msg, receive_msg.length);
					// Receive the message
					try {socketListener.receive(packet);}
					catch (IOException e) {e.printStackTrace();}
					String msg = new String(packet.getData(), packet.getOffset(), packet.getLength());
					
					String[] mensagem = msg.split(";");
					// Treats the message
					if(Integer.parseInt(mensagem[1]) == my_id && !Game.getGameStatus()) {
						// The message is for me
						System.out.println("[listener] Recebido por player "+TokenRing.getMyId()+": "+msg);
						Game.addBufferMessage(msg);
					}
					else if(Integer.parseInt(mensagem[0]) == my_id && Integer.parseInt(mensagem[2]) == 1) {
						// Received AKI: The origin is me and AKI flag enabled
						waitingAKI = false;
						if(Integer.parseInt(mensagem[3]) == 1)
							Game.addBufferMessage(msg);
					}
					else {
						// The message is not for me, I just send it to the next one
						speaker(false, msg);
					}
				}
			}
		}.start();
	}
	
	public void speaker(boolean wait, String msg) {
		byte[] send_msg = new byte[1024]; // buffer for messages
			
		try {
			socketSpeaker = new DatagramSocket();
			System.out.println("[speaker] Enviando: "+msg);
			
			// Send the message
			send_msg = msg.getBytes();
			DatagramPacket pack = new DatagramPacket(send_msg, send_msg.length, next_ip, next_port);
			socketSpeaker.send(pack);
			if(wait){
				waitingAKI = true;
				waitAKI();
			}
		} 
		catch (IOException e) {e.printStackTrace();}
		
		socketSpeaker.close();
	}
	
	public static void waitAKI() {
		int count;
		// Try 3 times
		for (count = 3; count > 0 && waitingAKI ; count--) {
			for(int timeout = 5; waitingAKI && timeout > 0; --timeout){
				try {Thread.sleep(100);}
				catch (InterruptedException e) {e.printStackTrace();}
			}			
		}
		// timeout and did not receive AKI
		if(count == 0) {
			System.err.println("[waitAKI] Falha ao enviar a mensagem");
			System.exit(0);
		}
	}
	
	public static InetAddress getLocalIP() {
		try {return InetAddress.getByName("localhost");}
		catch (UnknownHostException e) {e.printStackTrace();}
		return null;
	}
	
	public static int getMyId(){
		return my_id;
	}
	
	public static int getNextId(){
		return next_id;
	}
	
}
