import java.io.Serializable;

public class Mensaje implements Serializable {

	private String m, valor;
	private int index;
	int equipo;

	public Mensaje(String m, int index, int equipo, String valor) {
		this.m = m; 
		this.index = index;
		this.equipo= equipo;
		this.valor = valor;
	}

	public String getM() {
		return m;
	}

	public void setM(String m) {
		this.m = m;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}
	
	public int getEquipo() {
		return equipo;
	}

	public void setEquipo(int index) {
		this.equipo = index;
	}

	public String getValor() {
		return valor;
	}

	public void setValor(String valor) {
		this.valor = valor;
	}
	
}