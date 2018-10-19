import java.util.Observable;

public class Logica extends Observable {

	private ControlCliente cs;
	private ControlServidor servidor;

	public Logica() {
		// Se inicia el Servidor
		servidor = new ControlServidor(this);
		new Thread(servidor).start();
	}

	public synchronized void update(Observable o, Object arg) {

	}

	// GETTERS Y SETTERS
	public ControlCliente getCs() {
		return cs;
	}

	public void setCs(ControlCliente cs) {
		this.cs = cs;
	}

}
