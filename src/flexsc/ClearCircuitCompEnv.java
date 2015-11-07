package flexsc;

import network.Network;

public class ClearCircuitCompEnv extends CompEnv<CircuitWire> {

	public ClearCircuitCompEnv(Network w, Party p) {
		super(w, p, Mode.CIRCUIT);
	}

	@Override
	public CircuitWire inputOfAlice(boolean in) {
		Boolean res = in;
		if (party == Party.Alice)
			channel.writeInt(in ? 1 : 0);
		else {
			int re = channel.readInt();
			res = re == 1;
		}
		channel.flush();
		return new CircuitWire(res, CircuitWire.wid++);
	}

	@Override
	public CircuitWire inputOfBob(boolean in) {
		Boolean res = null;
		channel.flush();
		res = in;
		if (party == Party.Bob)
			channel.writeInt(in ? 1 : 0);
		else {
			int re = channel.readInt();
			res = re == 1;
		}
		channel.flush();
		return new CircuitWire(res, CircuitWire.wid++);
	}

	@Override
	public boolean outputToAlice(CircuitWire out) {
		if (party == Party.Alice)
			System.out.println("OUT = " + out.wireId + " VALUE = " + out.v);
		return false;
	}

	@Override
	public boolean outputToBob(CircuitWire out) {
		if (party == Party.Alice)
			System.out.println("OUT = " + out.wireId + " VALUE = " + out.v);
		return false;
	}

	@Override
	public CircuitWire[] inputOfAlice(boolean[] in) {
		CircuitWire[] signal = new CircuitWire[in.length];
		for (int i = 0; i < in.length; i++)
			signal[i] = inputOfAlice(in[i]);
		return signal;
	}

	@Override
	public CircuitWire[] inputOfBob(boolean[] in) {
		CircuitWire[] signal = new CircuitWire[in.length];
		for (int i = 0; i < in.length; i++)
			signal[i] = inputOfBob(in[i]);
		return signal;
	}

	@Override
	public boolean[] outputToAlice(CircuitWire[] out) {
		for (int i = 0; i < out.length; i++)
			System.out.println("OUT = " + out[i].wireId + " VALUE = " + out[i].v);
		return null;
	}

	@Override
	public boolean[] outputToBob(CircuitWire[] out) {
		for (int i = 0; i < out.length; i++)
			System.out.println("OUT = " + out[i].wireId + " VALUE = " + out[i].v);
		return null;
	}

	@Override
	public CircuitWire and(CircuitWire a, CircuitWire b) {
		int id = CircuitWire.wid++;
		if (party == Party.Alice)
			System.out.println(a.wireId + " AND " + b.wireId + " = " + id);
		return new CircuitWire(a.v & b.v, id);
	}

	@Override
	public CircuitWire xor(CircuitWire a, CircuitWire b) {
		int id = CircuitWire.wid++;
		if (party == Party.Alice) 
			System.out.println(a.wireId + " XOR " + b.wireId + " = " + id);
		return new CircuitWire(a.v ^ b.v, id);
	}

	@Override
	public CircuitWire not(CircuitWire a) {
		int id = CircuitWire.wid++;
		if (party == Party.Alice)
			System.out.println("NOT " + a.wireId + " = " + id);
		return new CircuitWire(!a.v, id);
	}

	@Override
	public CircuitWire ONE() {
		return new CircuitWire(true, CircuitWire.wid++);
	}

	@Override
	public CircuitWire ZERO() {
		return new CircuitWire(false, CircuitWire.wid++);
	}

	@Override
	public CircuitWire[] newTArray(int len) {
		return new CircuitWire[len];
	}

	@Override
	public CircuitWire[][] newTArray(int d1, int d2) {
		return new CircuitWire[d1][d2];
	}

	@Override
	public CircuitWire[][][] newTArray(int d1, int d2, int d3) {
		return new CircuitWire[d1][d2][d3];
	}

	@Override
	public CircuitWire newT(boolean v) {
		return new CircuitWire(v, CircuitWire.wid++);
	}

}
