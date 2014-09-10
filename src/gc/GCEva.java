package gc;

import java.io.InputStream;
import java.io.OutputStream;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

import ot.FakeOTReceiver;
import ot.OTExtReceiver;
import ot.OTReceiver;
import flexsc.CompEnv;
import flexsc.Flag;
import flexsc.Party;

public class GCEva extends GCCompEnv {
	OTReceiver rcv;
	Garbler gb;

	public int nonFreeGate = 0;
	long gid = 0;

	public GCEva(InputStream is, OutputStream os) throws Exception {
		super(is, os, Party.Bob);

		if(Flag.FakeOT)
			rcv = new FakeOTReceiver(is, os);
		else
			rcv = new OTExtReceiver(is, os);
		
		gb = new Garbler();
		gtt[0][0] = GCSignal.ZERO;
	}
	
	public GCEva(InputStream is, OutputStream os, boolean NoOT) throws Exception {
		super(is, os, Party.Bob);

		rcv = null;
		gb = new Garbler();
		gtt[0][0] = GCSignal.ZERO;
	}

	public GCSignal inputOfAlice(boolean in) throws Exception {
		Flag.sw.startOT();
		GCSignal signal = GCSignal.receive(is);
		Flag.sw.stopOT();
		return signal;
	}

	public GCSignal inputOfBob(boolean in) throws Exception {
		Flag.sw.startOT();
		GCSignal signal = rcv.receive(in);
		Flag.sw.stopOT();
		return signal; 
	}

	public GCSignal[] inputOfBob(boolean[] x) throws Exception {
		Flag.sw.startOT();
		GCSignal[] signal = rcv.receive(x);
		Flag.sw.stopOT();
		return signal;
	}

	public GCSignal[] inputOfAlice(boolean[] x) throws Exception {
		Flag.sw.startOT();
		GCSignal[] result = new GCSignal[x.length];
		for(int i = 0; i < x.length; ++i)
			result[i] = GCSignal.receive(is);
		Flag.sw.stopOT();
		return result;
	}


	public boolean outputToAlice(GCSignal out) throws Exception {
		if (!out.isPublic())
			out.send(os);
		return false;
	}

	public boolean outputToBob(GCSignal out) throws Exception {
		if (out.isPublic())
			return out.v;

		GCSignal lb = GCSignal.receive(is);
		if (lb.equals(out))
			return false;
//		else if (lb.equals(R.xor(out)))
		else
			return true;
	}
	
	public boolean[] outputToAlice(GCSignal[] out) throws Exception {
		boolean [] result = new boolean[out.length];
		
		for(int i = 0; i < result.length; ++i) {
			if (!out[i].isPublic())
				out[i].send(os);
		}
		os.flush();
		
		for(int i = 0; i < result.length; ++i)
			result[i] = false;
		return result;
	}
	
	public boolean[] outputToBob(GCSignal[] out) throws Exception {
		boolean [] result = new boolean[out.length];
		for(int i = 0; i < result.length; ++i) {
			result[i] = outputToBob(out[i]);
		}
		return result;
	}

	private GCSignal[][] gtt = new GCSignal[2][2];

	private void receiveGTT() {
		try {
			Flag.sw.startGCIO();
			gtt[0][1] = GCSignal.receive(is);
			gtt[1][0] = GCSignal.receive(is);
			gtt[1][1] = GCSignal.receive(is);
			Flag.sw.stopGCIO();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public GCSignal and(GCSignal a, GCSignal b) throws IllegalBlockSizeException, BadPaddingException {
		Flag.sw.startGC();

		GCSignal res;
		if (a.isPublic() && b.isPublic())
			res =  new GCSignal(a.v && b.v);
		else if (a.isPublic())
			res =  a.v ? b : new GCSignal(false);
		else if (b.isPublic())
			res = b.v ? a : new GCSignal(false);
		else {
			receiveGTT();

			int i0 = a.getLSB() ? 1 : 0;
			int i1 = b.getLSB() ? 1 : 0;

			res = gb.dec(a, b, gid, gtt[i0][i1]);
			gid++;
		}
		Flag.sw.stopGC();
		return res;
	}


	public GCSignal xor(GCSignal a, GCSignal b) {
		if (a.isPublic() && b.isPublic())
			return new GCSignal(a.v ^ b.v);
		else if (a.isPublic())
			return a.v ? not(b) : new GCSignal(b);
		else if (b.isPublic())
			return b.v ? not(a) : new GCSignal(a);
		else
			return a.xor(b);
	}

	public GCSignal not(GCSignal a) {
		if (a.isPublic())
			return new GCSignal(!a.v);
		else {
			return new GCSignal(a);
		}
	}

	@Override
	public CompEnv<GCSignal> getNewInstance(InputStream in, OutputStream os) throws Exception {
		return new GCEva(in, os, true);
	}
}