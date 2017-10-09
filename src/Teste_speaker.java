
public class Teste_speaker {

	public static void main(String[] args) {
		TokenRing t1 = new TokenRing(2, TokenRing.getLocalIP(), 10101, 10100);
		
		t1.speaker("oi");
	}
	
}
