
public class Teste_speaker {

	public static void main(String[] args) {
		TokenRing t1 = new TokenRing(2, 1, TokenRing.getLocalIP());
		
		t1.speaker(true, "oi");
		
	}
	
}
