package gc;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.SecureRandom;

import ot.FakeOTSender;
import ot.OTExtSender;
import ot.OTSender;
import flexsc.CompEnv;
import flexsc.Flag;
import flexsc.Party;

public class GCGen extends GCCompEnv {

	static public GCSignal R = null;
	static{
		R = GCSignal.freshLabel(new SecureRandom());
		R.setLSB();
	}

	OTSender snd;
	Garbler gb;

	long gid = 0;
	public GCGen(InputStream is, OutputStream os) throws Exception {
		super(is, os, Party.Alice);
		
		if(Flag.FakeOT)
			snd = new FakeOTSender(80, is, os);
		else
			snd = new OTExtSender(80, is, os);
		gb = new Garbler();
	}

	public GCGen(InputStream is, OutputStream os, boolean NoOT) throws Exception {
		super(is, os, Party.Alice);		
		gb = new Garbler();
	}

	private GCSignal[] genPair() {
		GCSignal[] label = new GCSignal[2];
		label[0] = GCSignal.freshLabel(rnd);
		label[1] = R.xor(label[0]);
		return label;
	}

	public GCSignal inputOfAlice(boolean in) throws Exception {
		Flag.sw.startOT();
		GCSignal[] label = genPair();
		label[in ? 1 : 0].send(os);
		os.flush();
		Flag.sw.stopOT();
		return label[0];
	}

	public GCSignal inputOfBob(boolean in) throws Exception {
		Flag.sw.startOT();
		GCSignal[] label = genPair();
		snd.send(label);
		Flag.sw.stopOT();
		return label[0];
	}

	public GCSignal[] inputOfAlice(boolean[] x) throws Exception {
		Flag.sw.startOT();
		GCSignal[][] pairs = new GCSignal[x.length][2];
		GCSignal[] result = new GCSignal[x.length];
		for(int i = 0; i < x.length; ++i){
			pairs[i] = genPair();
			result[i] = pairs[i][0];
		}
		for(int i = 0; i < x.length; ++i)
			pairs[i][x[i] ? 1 : 0].send(os);
		os.flush();
		Flag.sw.stopOT();
		return result;
	}

	public GCSignal[] inputOfBob(boolean[] x) throws Exception {
		Flag.sw.startOT();
		GCSignal[][] pair = new GCSignal[x.length][2];
		for(int i = 0; i < x.length; ++i)
			pair[i] = genPair();
		snd.send(pair);
		GCSignal[] result = new GCSignal[x.length];
		for(int i = 0; i < x.length; ++i)
			result[i] = pair[i][0];
		Flag.sw.stopOT();
		return result;

	}

	boolean gatesRemain = false;
	public boolean outputToAlice(GCSignal out) throws Exception {
		if(gatesRemain){
			gatesRemain = false;
			os.flush();
			//			Flag.sw.ands += ands;
			//			ands = 0;
		}
		if (out.isPublic())
			return out.v;

		GCSignal lb = GCSignal.receive(is);
		if (lb.equals(out))
			return false;
		else if (lb.equals(R.xor(out)))
			return true;
		
		throw new Exception("bad label at final output.");
	}
	
	public boolean outputToBob(GCSignal out) throws Exception {
		if (!out.isPublic())
			out.send(os);
		return false;
	}
	
	public boolean[] outputToBob(GCSignal[] out) throws Exception {
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


	public boolean[] outputToAlice(GCSignal[] out) throws Exception {
		boolean [] result = new boolean[out.length];
		for(int i = 0; i < result.length; ++i) {
			result[i] = outputToAlice(out[i]);
		}
		return result;
	}

	private GCSignal[][] gtt = new GCSignal[2][2];
	private GCSignal labelL[] = new GCSignal[2];
	private GCSignal labelR[] = new GCSignal[2];

	private GCSignal garble(GCSignal a, GCSignal b) {
		labelL[0] = a;
		labelL[1] = R.xor(labelL[0]);
		labelR[0] = b;
		labelR[1] = R.xor(labelR[0]);

		int cL = a.getLSB() ? 1 : 0;
		int cR = b.getLSB() ? 1 : 0;

		GCSignal[] lb = new GCSignal[2];
		lb[cL & cR] = gb.enc(labelL[cL], labelR[cR], gid, GCSignal.ZERO);
		lb[1 - (cL & cR)] = R.xor(lb[cL & cR]);

		gtt[0 ^ cL][0 ^ cR] = lb[0];
		gtt[0 ^ cL][1 ^ cR] = lb[0];
		gtt[1 ^ cL][0 ^ cR] = lb[0];
		gtt[1 ^ cL][1 ^ cR] = lb[1];

		if (cL != 0 || cR != 0)
			gtt[0 ^ cL][0 ^ cR] = gb.enc(labelL[0], labelR[0], gid,
					gtt[0 ^ cL][0 ^ cR]);
		if (cL != 0 || cR != 1)
			gtt[0 ^ cL][1 ^ cR] = gb.enc(labelL[0], labelR[1], gid,
					gtt[0 ^ cL][1 ^ cR]);
		if (cL != 1 || cR != 0)
			gtt[1 ^ cL][0 ^ cR] = gb.enc(labelL[1], labelR[0], gid,
					gtt[1 ^ cL][0 ^ cR]);
		if (cL != 1 || cR != 1)
			gtt[1 ^ cL][1 ^ cR] = gb.enc(labelL[1], labelR[1], gid,
					gtt[1 ^ cL][1 ^ cR]);

		// assert(gb.enc(labelL[cL], labelR[cR], gid,
		// gtt[0][0]).equals(Label.ZERO)) : "Garbling problem.";
		return lb[0];
	}

	private void sendGTT() {
		try {
			Flag.sw.startGCIO();
			gtt[0][1].send(os);
			gtt[1][0].send(os);
			gtt[1][1].send(os);
			Flag.sw.stopGCIO();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public GCSignal and(GCSignal a, GCSignal b) {
		++Flag.sw.ands;

		Flag.sw.startGC();
		GCSignal res;
		if (a.isPublic() && b.isPublic())
			res = new GCSignal(a.v && b.v);
		else if (a.isPublic())
			res = a.v ? b : new GCSignal(false);
		else if (b.isPublic())
			res = b.v ? a : new GCSignal(false);
		else {

			GCSignal ret;
			ret = garble(a, b);

			sendGTT();
			gid++;
			gatesRemain = true;
			res = ret;
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
				else {
					return a.xor(b);
				}
	}

	public GCSignal not(GCSignal a) {
		if (a.isPublic())
			return new GCSignal(!a.v);
		else 
			return R.xor(a);
	}

	@Override
	public CompEnv<GCSignal> getNewInstance(InputStream in, OutputStream os) throws Exception {
		return new GCGen(in, os, true);
	}
}
