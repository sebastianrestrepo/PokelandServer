import java.io.IOException;
import java.net.*;
import java.util.Observable;

public class CreadorClientes extends Observable implements Runnable {

	private ServerSocket ss;
	private final int PUERTO=5000;

	public CreadorClientes() {
		try {
			ss = new ServerSocket(PUERTO);
		} catch (IOException e) {
			e.printStackTrace();
		}

		new Thread(this).start();
	}

	@Override
	public void run() {
		/*
		 * Se recibe el Socket, se acepta la conexión, y se le informa al observador
		 * (ControlServidor) para que lo adicione a su ArrayList
		 */
		while (true) {
			try {
				System.out.println("Esperando nuevo cliente...");
				Socket nuevoSocket = ss.accept();
				setChanged();
				notifyObservers(nuevoSocket);
				clearChanged();
				Thread.sleep(1000);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

}
