import java.io.Serializable;

public class Mensaje implements Serializable {

	private String m;

	public Mensaje(String m) {
		this.m = m;
	}

	public String getM() {
		return m;
	}

	public void setM(String m) {
		this.m = m;
	}
	
}