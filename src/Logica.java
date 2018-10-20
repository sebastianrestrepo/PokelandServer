import java.util.Observable;
import java.util.Observer;

import processing.core.PApplet;

public class Logica extends Observable implements Observer{

	private ControlCliente cs;
	private ControlServidor servidor;
	private PApplet app;

	public Logica(PApplet app) {
		this.setApp(app);
		servidor = new ControlServidor();
		new Thread(servidor).start();
	}

	public void pintar () {
		
	}
	public  void update(Observable o, Object arg) {

	}

	//---------------------GETTERS Y SETTERS
	public ControlCliente getCs() {
		return cs;
	}

	public void setCs(ControlCliente cs) {
		this.cs = cs;
	}

	public PApplet getApp() {
		return app;
	}

	public void setApp(PApplet app) {
		this.app = app;
	}
	
}
