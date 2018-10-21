import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Observable;

public class ControlCliente extends Observable implements Runnable {

	private Socket s;
	private ObjectInputStream entrada;
	private ObjectOutputStream salida;
	private Object atraparMensaje;
	private boolean conectado;
	private Logica log;

	/*
	 * Esta clase se encarga de la Lógica del enlace de flujos, de recibir y enviar
	 * mensajes, por medio del Socket que le asigna el ControlServidor y que llega
	 * desde el CreadorClientes
	 */

	public ControlCliente(Socket s) {
		this.s = s;
		this.log = log;
		conectado = false;

		System.out.println("[Conectado Socket...]");
		try {
			salida = new ObjectOutputStream(s.getOutputStream());
			entrada = new ObjectInputStream(s.getInputStream());
			System.out.println("...Enlace completo :)]");
			conectado = true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void run() {
		while (conectado) {
			try {
				Thread.sleep(200);
				recibirMensaje();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void recibirMensaje() {
		if (conectado) {
			try {
				atraparMensaje = entrada.readObject();
				if (atraparMensaje instanceof Mensaje) {
					Mensaje mensaje = (Mensaje) atraparMensaje;
					setChanged();
					notifyObservers(mensaje);
					clearChanged();
				}

			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void enviarMensaje(Object obj) {
		Thread t = new Thread(new Runnable() {

			public void run() {

				try {
					Mensaje m = (Mensaje) obj;
					if (conectado) {
						if (s.isConnected()) {
							salida.writeObject(m);
							salida.flush();
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		t.start();
	}

	// ----------GETTERS Y SETTERS--------//
	public Object getAtraparMensaje() {
		return atraparMensaje;
	}

	public void setAtraparMensaje(Object atraparMensaje) {
		this.atraparMensaje = atraparMensaje;
	}

	public boolean isConectado() {
		return conectado;
	}

	public void setConectado(boolean conectado) {
		this.conectado = conectado;
	}

	// ----------FINAL DE LA CLASE COMUNICACIONSERVIDOR--------//
}