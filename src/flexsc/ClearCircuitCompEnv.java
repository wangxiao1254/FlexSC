package flexsc;

import gc.GCSignal;
import network.Network;

public class ClearCircuitCompEnv extends CompEnv<GCSignal> {

	public ClearCircuitCompEnv(Network w, Party p) {
		super(w, p, Mode.CIRCUIT);
	}

	@Override
	public GCSignal inputOfAlice(boolean in) {
		Boolean res = in;
		if (party == Party.Alice)
			channel.writeInt(in ? 1 : 0);
		else {
			int re = channel.readInt();
			res = re == 1;
		}
		channel.flush();
		return new GCSignal(res, GCSignal.wid++);
	}

	@Override
	public GCSignal inputOfBob(boolean in) {
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
		return new GCSignal(res, GCSignal.wid++);
	}

	@Override
	public boolean outputToAlice(GCSignal out) {
		if (party == Party.Alice)
			System.out.println("OUT = " + out.wireId + " VALUE = " + out.v);
		return false;
	}

	@Override
	public boolean outputToBob(GCSignal out) {
		if (party == Party.Alice)
			System.out.println("OUT = " + out.wireId + " VALUE = " + out.v);
		return false;
	}

	@Override
	public GCSignal[] inputOfAlice(boolean[] in) {
		GCSignal[] signal = new GCSignal[in.length];
		for (int i = 0; i < in.length; i++)
			signal[i] = inputOfAlice(in[i]);
		return signal;
	}

	@Override
	public GCSignal[] inputOfBob(boolean[] in) {
		GCSignal[] signal = new GCSignal[in.length];
		for (int i = 0; i < in.length; i++)
			signal[i] = inputOfBob(in[i]);
		return signal;
	}

	@Override
	public boolean[] outputToAlice(GCSignal[] out) {
		for (int i = 0; i < out.length; i++)
			System.out.println("OUT = " + out[i].wireId + " VALUE = " + out[i].v);
		return null;
	}

	@Override
	public boolean[] outputToBob(GCSignal[] out) {
		for (int i = 0; i < out.length; i++)
			System.out.println("OUT = " + out[i].wireId + " VALUE = " + out[i].v);
		return null;
	}

	@Override
	public GCSignal and(GCSignal a, GCSignal b) {
		int id = GCSignal.wid++;
		if (party == Party.Alice)
			System.out.println(a.wireId + " AND " + b.wireId + " = " + id);
		return new GCSignal(a.v & b.v, id);
	}

	@Override
	public GCSignal xor(GCSignal a, GCSignal b) {
		int id = GCSignal.wid++;
		if (party == Party.Alice) 
			System.out.println(a.wireId + " XOR " + b.wireId + " = " + id);
		return new GCSignal(a.v ^ b.v, id);
	}

	@Override
	public GCSignal not(GCSignal a) {
		int id = GCSignal.wid++;
		if (party == Party.Alice)
			System.out.println("NOT " + a.wireId + " = " + id);
		return new GCSignal(!a.v, id);
	}

	@Override
	public GCSignal ONE() {
		return new GCSignal(true, GCSignal.wid++);
	}

	@Override
	public GCSignal ZERO() {
		return new GCSignal(false, GCSignal.wid++);
	}

	@Override
	public GCSignal[] newTArray(int len) {
		return new GCSignal[len];
	}

	@Override
	public GCSignal[][] newTArray(int d1, int d2) {
		return new GCSignal[d1][d2];
	}

	@Override
	public GCSignal[][][] newTArray(int d1, int d2, int d3) {
		return new GCSignal[d1][d2][d3];
	}

	@Override
	public GCSignal newT(boolean v) {
		return new GCSignal(v, GCSignal.wid++);
	}

}
