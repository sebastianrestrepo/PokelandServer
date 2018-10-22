import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import processing.core.PApplet;

public class ControlServidor implements Observer, Runnable {

	private CreadorClientes creadorClientes;
	private ArrayList<ControlCliente> clientes;
	private int jugadoresListos;
	private int estacion, ronda, totalturnos;
	private int bayasJug1, bayasJug2, bayasJug3, bayasJug4;
	private int reservaBayasGeneralMes;
	private int arbolesPlantadosGen;
	private boolean turnoinicial;

	private List<Integer> turnos = new ArrayList<Integer>(3);
	private int indexTurn;

	public ControlServidor() {
		reservaBayasGeneralMes = 250;
		clientes = new ArrayList<>();
		creadorClientes = new CreadorClientes();
		creadorClientes.addObserver(this);
		jugadoresListos = 0;
		indexTurn = -2;
		estacion = 0;
		totalturnos = -1;
		ronda = 0;
		arbolesPlantadosGen = 0;
		turnoinicial = false;
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
					turnoinicial = true;
				}

				if (turnoinicial) {
					iniciarRonda();
					for (int i = 0; i < clientes.size(); i++) {
						clientes.get(i).enviarMensaje(
								new Mensaje("turno", sigTurno(), estacion, reservaBayasGeneralMes + ",nada"));
						System.out.println("Turno enviado Inicial" + sigTurno());
					}
					jugadoresListos=5;
					turnoinicial = false;
				}
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void iniciarRonda() {
		indexTurn = 0;
		orderPlayers();
		sortTurnos();
		System.out.println("Turno enviado Inicial" + sigTurno() + "/" + indexTurn);
		
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

				}
				//
				//
				//
				// Cuando alguien termina un turno
				if (m.getM().equalsIgnoreCase("turnoterminado")) {
					getTurnInfo(m);
					indexTurn++;
					if (reservaBayasGeneralMes <= 0) {
						reservaBayasGeneralMes = 0;
						reservaBayasGeneralMes = 0;
					}
					if (indexTurn >= 4) {
						iniciarRonda();
						reservaBayasGeneralMes = sigMesReserva();
						System.out.println("Turno enviado MES" + sigTurno() + "/" + indexTurn);
						calcularestacion();
						nuevoMes(sigTurno(), reservaBayasGeneralMes);
					} else {
						for (int i = 0; i < clientes.size(); i++) {
							clientes.get(i).enviarMensaje(
									new Mensaje("turno", sigTurno(), estacion, reservaBayasGeneralMes + ",nada"));
						}
						System.out.println("Turno enviado NORMI" + sigTurno() + "/" + indexTurn);
					}
				}
			}
		}
	}


	private void nuevoMes(int turno, int reserva) {

		for (int i = 0; i < clientes.size(); i++) {
			clientes.get(i)
					.enviarMensaje(new Mensaje("turno", turno, estacion, reserva + ",mes"));
		}
		System.out.println("Empieza Nuevo Mes!");
	}

	private void getTurnInfo(Mensaje m) {
		Mensaje _m = (Mensaje) m;

		String[] tempValue = PApplet.splitTokens(_m.getValor(), ",");

		int tempBayasAlmacenadas = Integer.parseInt(tempValue[0]);
		int tempArbolesPlantados = Integer.parseInt(tempValue[1]);
		int tempPokeAdoptados = Integer.parseInt(tempValue[2]);
		int tempBayasTurno = Integer.parseInt(tempValue[3]);
		int tempArbolesTurno = Integer.parseInt(tempValue[4]);
		int tempPokeTurno = Integer.parseInt(tempValue[5]);

		arbolesPlantadosGen = arbolesPlantadosGen + tempArbolesTurno;
		reservaBayasGeneralMes = reservaBayasGeneralMes - tempBayasTurno;
		System.out.println("reservaBayasGeneralMes ----------- " + reservaBayasGeneralMes + "/" + tempBayasTurno);
		System.out.println("Bayas almacenadas " + tempValue[0] + "Arboles plantados" + tempValue[1]
				+ "Pokemon Adoptados " + tempValue[2] + "Bayas del turno " + tempValue[3] + "Arboles del Turno"
				+ tempValue[4] + "Pokes del Turno " + tempValue[5]);
		switch (m.getEquipo()) {
		case 0:
			System.out.println("Acabe turno equipo amarillo");
			break;
		case 1:
			System.out.println("Acabe turno equipo rojo");
			break;
		case 2:
			System.out.println("Acabe turno equipo verde");
			break;
		case 3:
			System.out.println("Acabe turno equipo azul");
			break;
		default:
			break;
		}
		return;

	}
	

	private int sigMesReserva() {
		switch (estacion) {
		case 0:
			return 250 + arbolesPlantadosGen * 5;
		case 1:
			return 200 + arbolesPlantadosGen * 4;
		case 2:
			return 120 + arbolesPlantadosGen * 3;
		case 3:
			return 100 + arbolesPlantadosGen * 2;

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

}
