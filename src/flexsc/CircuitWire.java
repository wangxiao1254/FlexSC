package flexsc;

public class CircuitWire {

	public static int wid = 0;
	public boolean v;
	public int wireId;

	public CircuitWire(boolean b, int wireId) {
		this.v = b;
		this.wireId = wireId;
	}
}
