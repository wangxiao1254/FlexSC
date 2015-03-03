package gc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import ot.FakeOTSender;
import ot.OTExtSender;
import ot.OTPreprocessSender;
import ot.OTSender;
import flexsc.CompEnv;
import flexsc.Flag;
import flexsc.Mode;
import flexsc.Party;

public abstract class GCGenComp extends GCCompEnv{

	static public GCSignal R = null;
	static {
		R = GCSignal.freshLabel(CompEnv.rnd);
		R.setLSB();
	}

	OTSender snd;
	protected long gid = 0;

	public GCGenComp(InputStream is, OutputStream os, Mode m) {
		super(is, os, Party.Alice, m);

		if (Flag.FakeOT)
			snd = new FakeOTSender(80, is, os);
		else if (Flag.ProprocessOT)
			snd = new OTPreprocessSender(80, is, os);
		else
			snd = new OTExtSender(80, is, os);
	}

	public static GCSignal[] genPair() {
		GCSignal[] label = new GCSignal[2];
		label[0] = GCSignal.freshLabel(rnd);
		label[1] = R.xor(label[0]);
		return label;
	}

	public GCSignal inputOfAlice(boolean in) {
		Flag.sw.startOT();
		GCSignal[] label = genPair();
		Flag.sw.startOTIO();
		label[in ? 1 : 0].send(os);
		flush();
		Flag.sw.stopOTIO();
		Flag.sw.stopOT();
		return label[0];
	}

	public GCSignal inputOfBob(boolean in) {
		Flag.sw.startOT();
		GCSignal[] label = genPair();
		try {
			snd.send(label);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Flag.sw.stopOT();
		return label[0];
	}

	public GCSignal[] inputOfAlice(boolean[] x) {
		Flag.sw.startOT();
		GCSignal[][] pairs = new GCSignal[x.length][2];
		GCSignal[] result = new GCSignal[x.length];
		for (int i = 0; i < x.length; ++i) {
			pairs[i] = genPair();
			result[i] = pairs[i][0];
			pairs[i][x[i] ? 1 : 0].send(os);
		}
		
		Flag.sw.startOTIO();
//		for (int i = 0; i < x.length; ++i)
			
		flush();
		Flag.sw.stopOTIO();
		Flag.sw.stopOT();
		return result;
	}

	public GCSignal[] inputOfBob(boolean[] x) {
		GCSignal[] ret = new GCSignal[x.length];
		for(int i = 0; i < x.length; i+=Flag.OTBlockSize) {
			GCSignal[] tmp = inputOfBobInter(Arrays.copyOfRange(x, i, Math.min(i+Flag.OTBlockSize, x.length)));
			System.arraycopy(tmp, 0, ret, i, tmp.length);
		}
		return ret;
	}
	
	public GCSignal[] inputOfBobInter(boolean[] x) {
		Flag.sw.startOT();
		GCSignal[][] pair = new GCSignal[x.length][2];
		for (int i = 0; i < x.length; ++i)
			pair[i] = genPair();
		try {
			snd.send(pair);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		GCSignal[] result = new GCSignal[x.length];
		for (int i = 0; i < x.length; ++i)
			result[i] = pair[i][0];
		Flag.sw.stopOT();
		return result;

	}

	protected boolean gatesRemain = false;

	public boolean outputToAlice(GCSignal out) throws BadLabelException {
		if (gatesRemain) {
			gatesRemain = false;
			flush();
		}
		if (out.isPublic())
			return out.v;

		GCSignal lb = GCSignal.receive(is);
		if (lb.equals(out))
			return false;
		else if (lb.equals(R.xor(out)))
			return true;

		throw new BadLabelException("bad label at final output.");
	}

	public boolean outputToBob(GCSignal out) {
		if (!out.isPublic())
			out.send(os);
		return false;
	}

	public boolean[] outputToBob(GCSignal[] out) {
		boolean[] result = new boolean[out.length];

		for (int i = 0; i < result.length; ++i) {
			if (!out[i].isPublic())
				out[i].send(os);
		}
		flush();

		for (int i = 0; i < result.length; ++i)
			result[i] = false;
		return result;
	}

	public boolean[] outputToAlice(GCSignal[] out) throws BadLabelException {
		boolean[] result = new boolean[out.length];
		for (int i = 0; i < result.length; ++i) {
			result[i] = outputToAlice(out[i]);
		}
		return result;
	}


	public GCSignal xor(GCSignal a, GCSignal b) {
		if (a.isPublic() && b.isPublic())
			return  ((a.v ^ b.v) ?_ONE:_ZERO);
		else if (a.isPublic())
			return a.v ? not(b) : b;
		else if (b.isPublic())
			return b.v ? not(a) : a;
		else {
			return a.xor(b);
		}
	}

	public GCSignal not(GCSignal a) {
		if (a.isPublic())
			return ((!a.v) ?_ONE:_ZERO);//new GCSignal(!a.v);
		else
			return R.xor(a);
	}
}
