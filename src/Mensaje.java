import java.io.Serializable;

public class Mensaje implements Serializable {

	private String m;
	private int index;
	int equipo;

	public Mensaje(String m, int index, int equipo) {
		this.m = m; 
		this.index = index;
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
		this.equipo = equipo;
	}
	
}