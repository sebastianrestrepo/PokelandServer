import java.io.Serializable;

public class Mensaje implements Serializable {

	private String m;
	private int index;

	public Mensaje(String m, int index) {
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
	
}