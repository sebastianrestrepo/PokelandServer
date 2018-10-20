import java.net.Socket;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

public class ControlServidor implements Observer, Runnable {

	private CreadorClientes creadorClientes;
	private ArrayList<ControlCliente> clientes;

	public ControlServidor() {
		clientes = new ArrayList<>();
		creadorClientes = new CreadorClientes();
		creadorClientes.addObserver(this);
	}

	@Override
	public void run() {
		while (true) {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void update(Observable o, Object arg) {
		if (o instanceof CreadorClientes) {
			if (arg instanceof Socket) {
				Socket s = (Socket) arg;
				ControlCliente nuevoCliente = new ControlCliente(s);
				nuevoCliente.addObserver(this);
				clientes.add(nuevoCliente);
				
				clientes.get(clientes.size() - 1).enviarMensaje(new Mensaje("equipo", clientes.size(), 1));
				
				Thread t = new Thread(nuevoCliente);
				t.start();
				
				System.out.println("nuevoCliente:" + nuevoCliente.toString());
			}
			
			if (clientes.size() >= 4) {
				for (int i = 0; i < clientes.size(); i++) {
					clientes.get(i).enviarMensaje(new Mensaje("start", clientes.size(), 1));

				}
			}
			
		}
	
	}

}
