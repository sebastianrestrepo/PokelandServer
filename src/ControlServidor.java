import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class ControlServidor implements Observer, Runnable {

	private CreadorClientes creadorClientes;
	private ArrayList<ControlCliente> clientes;
	private int jugadoresListos;
	private int estacion, ronda, totalturnos;

	private List<Integer> turnos = new ArrayList<Integer>(3);
	private int indexTurn;

	public ControlServidor() {
		clientes = new ArrayList<>();
		creadorClientes = new CreadorClientes();
		creadorClientes.addObserver(this);
		jugadoresListos = 0;
		indexTurn = -1;
		estacion = 0;
		totalturnos = -1;
		ronda = 0;
	}

	@Override
	public void run() {

		while (true) {
			try {
				//
				//
				//
				// Están todos los jugadoree, empieza la app
				if (clientes.size() >= 4) {
					for (int i = 0; i < clientes.size(); i++) {
						clientes.get(i).enviarMensaje(new Mensaje("start", clientes.size(), 1));

					}
				}
				if (jugadoresListos == 4) {
					orderPlayers();
					sortTurnos();
					indexTurn++;
					int tempTurno = sigTurno();
					for (int i = 0; i < clientes.size(); i++) {

						clientes.get(i).enviarMensaje(new Mensaje("turno", tempTurno, estacion));
						System.out.println("Turno enviado" + tempTurno);
						jugadoresListos = 5;
					}
				}

				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void orderPlayers() {
		for (int i = 0; i < 4; i++)
			turnos.add(i);
	}

	public void sortTurnos() {
		Collections.shuffle(turnos);
	}

	public int sigTurno() {

		return turnos.get(indexTurn);
	}

	@Override
	public void update(Observable o, Object arg) {
		if (o instanceof CreadorClientes) {
			//
			//
			//
			// Recibir jugador
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

		}

		if (o instanceof ControlCliente) {
			if (arg instanceof Mensaje) {
				//
				//
				//
				//
				// Todos los jugadores vieron las instrucciones, asignar turno
				Mensaje m = (Mensaje) arg;
				System.out.println("Jugadores listos" + jugadoresListos);

				if (m.getM().equalsIgnoreCase("listo")) {
					jugadoresListos++;
					System.out.println("Jugadores listos" + jugadoresListos);

				}

				//
				//
				//
				//
				if (m.getM().equalsIgnoreCase("turnoterminado")) {

					if (indexTurn <= 2) {
						indexTurn++;
						int tempTurno = sigTurno();
						for (int i = 0; i < clientes.size(); i++) {
							clientes.get(i).enviarMensaje(new Mensaje("turno", tempTurno, 1));
						}
						System.out.println("Ronda: " + ronda + "Estacion " + estacion);

					} else if (indexTurn == 3) {
						System.out.println("NUEVA RONDA" + totalturnos);
						System.out.println("Ronda: " + ronda + "Estacion " + estacion);
						sortTurnos();
						calcularestacion();
						nuevoMes();

						indexTurn = 0;
						int tempTurno2 = sigTurno();
						for (int i = 0; i < clientes.size(); i++) {
							clientes.get(i).enviarMensaje(new Mensaje("turno", tempTurno2, 1));
						}

					}
				}
				System.out.println("Mes" + estacion);

			}

		}
	}

	private void calcularestacion() {
		ronda++;
		if (ronda % 3 == 0) {
			estacion++;
			ronda = 0;

		}
	}

	private void nuevoMes() {
		int mes = estacion;
		for (int i = 0; i < clientes.size(); i++) {
			clientes.get(i).enviarMensaje(new Mensaje("nuevoMes", mes, 1));
		}
		System.out.println("Empieza Nuevo Mes!");

	}

}
