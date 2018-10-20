import processing.core.PApplet;

public class PokelandServerMain extends PApplet {

	private Logica log;
	
	public static void main(String[] args) {
		PApplet.main("PokelandServerMain");
	}
	
	public void setup() {
		log = new Logica(this);
	}
	
	public void draw() {
		log.pintar();
	}

}
