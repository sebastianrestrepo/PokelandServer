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
					if (indexTurn == -2) {
						orderPlayers();
						sortTurnos();
						indexTurn=0;
						
						int tempTurno = sigTurno();
						System.out.println("Turno enviado NORMI" + tempTurno + "/" + indexTurn);
						int tempReserva = reservaBayasGeneralMes;
						for (int i = 0; i < clientes.size(); i++) {
							System.out.println("RESERVA esta en " + reservaBayasGeneralMes);
							clientes.get(i).enviarMensaje(new Mensaje("turno", tempTurno, tempReserva, reservaBayasGeneralMes + ""));
							System.out.println("Turno enviado" + tempTurno);
							
						}
						jugadoresListos = 5;
					}
					
				}

				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void orderPlayers() {
		indexTurn = 0;
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
					if (indexTurn== -1) {
						indexTurn=0;
						int tempTurno = sigTurno();
						if (reservaBayasGeneralMes<=0) {
							reservaBayasGeneralMes = 0;
							reservaBayasGeneralMes= 0;
						}
						for (int i = 0; i < clientes.size(); i++) {
							clientes.get(i).enviarMensaje(new Mensaje("turno", tempTurno, estacion,  reservaBayasGeneralMes + ""));
						}
						System.out.println("Turno enviado INICIAL MES" + tempTurno + "/" + indexTurn);
					}
					if (indexTurn> 0 && indexTurn <= 2) {		
						indexTurn++;
						int tempTurno = sigTurno();
						if (reservaBayasGeneralMes<=0) {
							reservaBayasGeneralMes = 0;
							reservaBayasGeneralMes= 0;
						}
						for (int i = 0; i < clientes.size(); i++) {
							clientes.get(i).enviarMensaje(new Mensaje("turno", tempTurno, estacion,  reservaBayasGeneralMes + ""));
							
						}
						System.out.println("Turno enviado NORMI" + tempTurno + "/" + indexTurn);
					}  else if (indexTurn == 3) {
						reservaBayasGeneralMes = sigMesReserva();
						sortTurnos();
						indexTurn = -1;
						System.out.println("Turno enviado MES" + 0 + "/" + indexTurn);
						calcularestacion();
						nuevoMes(0, reservaBayasGeneralMes);
					}
				}
				System.out.println("Mes " + estacion);

			}

		}
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
		System.out.println("reservaBayasGeneralMes ----------- " + reservaBayasGeneralMes + "/" + tempBayasTurno );
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

	private void nuevoMes(int tempTurno2, int tempReserva2) {
		
		for (int i = 0; i < clientes.size(); i++) {
			clientes.get(i).enviarMensaje(new Mensaje("turnoMes", tempTurno2 ,estacion,  Integer.toString(tempReserva2)));
		}
		System.out.println("Empieza Nuevo Mes!");
	}

}
