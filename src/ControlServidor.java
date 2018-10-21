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
	private int bayasJug1, bayasJug2, bayasJug3, bayasJug4;
	private int reservaBayasGeneralMes;
	private int arbolesPlantadosGen;

	private List<Integer> turnos = new ArrayList<Integer>(3);
	private int indexTurn;

	public ControlServidor() {
		reservaBayasGeneralMes=250;
		clientes = new ArrayList<>();
		creadorClientes = new CreadorClientes();
		creadorClientes.addObserver(this);
		jugadoresListos = 0;
		indexTurn = -1;
		estacion = 0;
		totalturnos = -1;
		ronda = 0;
		arbolesPlantadosGen =0 ;
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
						clientes.get(i).enviarMensaje(new Mensaje("start", clientes.size(), 1, null));

					}
				}
				if (jugadoresListos == 4) {
					orderPlayers();
					sortTurnos();
					indexTurn++;
					int tempTurno = sigTurno();
					int tempReserva = reservaBayasGeneralMes;
					for (int i = 0; i < clientes.size(); i++) {
						System.out.println("RESERVA esta en " + reservaBayasGeneralMes);
						clientes.get(i).enviarMensaje(new Mensaje("turno", tempTurno, tempReserva, null));
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

				clientes.get(clientes.size() - 1).enviarMensaje(new Mensaje("equipo", clientes.size(), 1, null));

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
				// Valor total por turno
				if (m.getM().equalsIgnoreCase("infoTurno")) {
					System.out.println("infoTurno: " + m.getValor());
				}

				//
				//
				//
				//
				if (m.getM().equalsIgnoreCase("turnoterminado")) {
					int tempReserva = reservaBayasGeneralMes = reservaBayasGeneralMes - m.getIndex();
					if (indexTurn <= 2) {
						indexTurn++;
						int tempTurno = sigTurno();
						
						for (int i = 0; i < clientes.size(); i++) {
							clientes.get(i).enviarMensaje(new Mensaje("turno", tempTurno, tempReserva, null));
							System.out.println("RESERVA QUEDA EN " + reservaBayasGeneralMes);
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
						int tempReserva2 = sigMesReserva();
						for (int i = 0; i < clientes.size(); i++) {
							clientes.get(i).enviarMensaje(new Mensaje("turno", tempTurno2, tempReserva2, null));
						}

					}
				}
				System.out.println("Mes" + estacion);

			}

		}
	}

	private int sigMesReserva() {
		switch (estacion) {
		case 0:
			return 250;
		case 1:
			return 200;
		case 2:
			return 120;
		case 3:
			return 100;

		default:
			return 0;	
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
			clientes.get(i).enviarMensaje(new Mensaje("nuevoMes", mes, 1, null));
		}
		System.out.println("Empieza Nuevo Mes!");

	}

}
