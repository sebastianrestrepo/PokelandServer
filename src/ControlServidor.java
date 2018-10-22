import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
	private int mes, estacion, ronda, totalturnos;
	private int bayasJug1, bayasJug2, bayasJug3, bayasJug4;
	private int reservaBayasGeneralMes;
	private int arbolesPlantadosGen;
	private boolean turnoinicial;

	private List<Integer> turnos = new ArrayList<Integer>(3);
	private int indexTurn;
	
	private ArrayList<String> infoTurnos, infoTurnos1, infoTurnos2, infoTurnos3;
	private ArrayList<String> infoFinales, infoFinales1, infoFinales2, infoFinales3;

	public ControlServidor() {
		reservaBayasGeneralMes = 250;
		clientes = new ArrayList<>();
		creadorClientes = new CreadorClientes();
		creadorClientes.addObserver(this);
		jugadoresListos = 0;
		indexTurn = -2;
		mes = 0;
		estacion= 0;
		totalturnos = -1;
		ronda = 0;
		arbolesPlantadosGen = 0;
		turnoinicial = false;
		//
		infoTurnos = new ArrayList<>();
		infoTurnos1 = new ArrayList<>();
		infoTurnos2 = new ArrayList<>();
		infoTurnos3 = new ArrayList<>();
		//
		infoFinales = new ArrayList<>();
		infoFinales1 = new ArrayList<>();
		infoFinales2 = new ArrayList<>();
		infoFinales3 = new ArrayList<>();
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
		orderPlayers();
		sortTurnos();
		indexTurn = 0;
		for (int i = 0; i < turnos.size(); i++) {
			System.out.println("Arreglo de turnos" +  turnos.get(i).intValue() + "/" + indexTurn);
		}
		
	}

	public void orderPlayers() {
		turnos.clear();
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
					System.out.println("Index del Array Turnos" + indexTurn);
					if (reservaBayasGeneralMes <= 0) {
						reservaBayasGeneralMes = 0;
						reservaBayasGeneralMes = 0;
					}
					if (indexTurn<4) {
						for (int i = 0; i < clientes.size(); i++) {
							clientes.get(i).enviarMensaje(
									new Mensaje("turno", sigTurno(), estacion, reservaBayasGeneralMes + ""));
						}
						System.out.println("Turno enviado" + sigTurno() + "/ Index " + indexTurn);
						
					} else if (indexTurn >= 4) {
						System.out.println("Empezar nuevo Mes/Ronda");
						iniciarRonda();
						reservaBayasGeneralMes = sigMesReserva();
						nuevoMes(sigTurno(),calcularestacion(), reservaBayasGeneralMes);
					}  
					
				}
			}
		}
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

	private int calcularestacion() {
		ronda++;
		mes++;
		if (ronda % 3 == 0) {
			estacion++;
			ronda = 0;
		}
		return estacion;
	}
	private void nuevoMes(int _turno, int _estacion, int _reserva) {
		for (int i = 0; i < clientes.size(); i++) {
			clientes.get(i)
					.enviarMensaje(new Mensaje("turno", _turno, _estacion, _reserva+""));
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
		arbolesPlantadosGen = arbolesPlantadosGen + tempArbolesPlantados - 1;
		reservaBayasGeneralMes = reservaBayasGeneralMes - tempBayasTurno;
		System.out.println("Bayas almacenadas " + tempValue[0] + "Arboles plantados" + tempValue[1]
				+ "Pokemon Adoptados " + tempValue[2] + "Bayas del turno " + tempValue[3] + "Arboles del Turno"
				+ tempValue[4] + "Pokes del Turno " + tempValue[5]);
		
		switch (m.getEquipo()) {
		case 0:
			System.out.println("Acabe turno equipo amarillo");
			infoTurnos.add(new String("Bayas almacenadas " + tempBayasAlmacenadas + " Arboles plantados" + tempArbolesPlantados
					+ " Pokemon Adoptados " + tempPokeAdoptados + " Bayas del turno " + tempBayasTurno + " Arboles del Turno"
					+ tempArbolesTurno + "Pokes del Turno " + tempPokeTurno));
			crearTxtPokeNum(m.getEquipo());
			break;
		case 1:
			System.out.println("Acabe turno equipo rojo");
			infoTurnos1.add(new String("Bayas almacenadas " + tempBayasAlmacenadas + " Arboles plantados" + tempArbolesPlantados
					+ " Pokemon Adoptados " + tempPokeAdoptados + " Bayas del turno " + tempBayasTurno + " Arboles del Turno"
					+ tempArbolesTurno + "Pokes del Turno " + tempPokeTurno));			crearTxtPokeNum(m.getEquipo());
			break;
		case 2:
			System.out.println("Acabe turno equipo verde");
			infoTurnos2.add(new String("Bayas almacenadas " + tempBayasAlmacenadas + " Arboles plantados" + tempArbolesPlantados
					+ " Pokemon Adoptados " + tempPokeAdoptados + " Bayas del turno " + tempBayasTurno + " Arboles del Turno"
					+ tempArbolesTurno + "Pokes del Turno " + tempPokeTurno));			crearTxtPokeNum(m.getEquipo());
			break;
		case 3:
			System.out.println("Acabe turno equipo azul");
			infoTurnos3.add(new String("Bayas almacenadas " + tempBayasAlmacenadas + " Arboles plantados" + tempArbolesPlantados
					+ " Pokemon Adoptados " + tempPokeAdoptados + " Bayas del turno " + tempBayasTurno + " Arboles del Turno"
					+ tempArbolesTurno + " Pokes del Turno " + tempPokeTurno));			crearTxtPokeNum(m.getEquipo());
			break;
		default:
			break;
		}
		
		return;

	}

	public void crearTxtPokeNum(int equipo) {
		try {
			File archivo = new File("data/PokemonesTurno/usuario_" + equipo + ".txt");
			archivo.createNewFile();
			BufferedWriter bw = new BufferedWriter(new FileWriter(archivo));

			
			System.out.println("Archivo creado");
			
			switch(equipo) {
			case 0:
				for (int j = 0; j < infoTurnos.size(); j++) {
					bw.write(infoTurnos.get(j));
					bw.newLine();
				}
				break;
			case 1:
				for (int j = 0; j < infoTurnos1.size(); j++) {
					bw.write(infoTurnos1.get(j));
					bw.newLine();
				}
				break;
			case 2:
				for (int j = 0; j < infoTurnos2.size(); j++) {
					bw.write(infoTurnos2.get(j));
					bw.newLine();
				}
				break;
			case 3:
				for (int j = 0; j < infoTurnos3.size(); j++) {
					bw.write(infoTurnos3.get(j));
					bw.newLine();
				}
				break;
			default:
				break;
			}

			bw.flush();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/*public void crearTxtFinales(int equipo) {
		try {
			File archivo = new File("data/ResultadosFinales/usuario_" + equipo + ".txt");
			archivo.createNewFile();
			BufferedWriter bw = new BufferedWriter(new FileWriter(archivo));

			
			System.out.println("Archivo creado");
			
				switch(equipo) {
				case 0:
					for (int j = 0; j < infoFinales.size(); j++) {
						bw.write(infoFinales.get(j));
						bw.newLine();
					}
					break;
				case 1:
					for (int j = 0; j < infoFinales1.size(); j++) {
						bw.write(infoFinales1.get(j));
						bw.newLine();
					}
					break;
				case 2:
					for (int j = 0; j < infoFinales2.size(); j++) {
						bw.write(infoFinales2.get(j));
						bw.newLine();
					}
					break;
				case 3:
					for (int j = 0; j < infoFinales3.size(); j++) {
						bw.write(infoFinales3.get(j));
						bw.newLine();
					}
					break;
				default:
					break;
				}
				
				
			bw.flush();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}*/
	


}
