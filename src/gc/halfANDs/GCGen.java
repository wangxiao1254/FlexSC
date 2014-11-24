package gc.halfANDs;

import flexsc.Flag;
import gc.GCGenComp;
import gc.GCSignal;

import java.io.InputStream;
import java.io.OutputStream;

public class GCGen extends GCGenComp {
	Garbler gb;

	public long ands = 0;
	public GCGen(InputStream is, OutputStream os){
		super(is, os);
		gb = new Garbler();
	}

	private GCSignal labelL[] = new GCSignal[2];
	private GCSignal labelR[] = new GCSignal[2];

	private GCSignal TG, WG, TE, WE;
	
	private GCSignal garble(GCSignal a, GCSignal b) {
		labelL[0] = a;
		labelL[1] = R.xor(labelL[0]);
		labelR[0] = b;
		labelR[1] = R.xor(labelR[0]);

		int cL = a.getLSB() ? 1 : 0;
		int cR = b.getLSB() ? 1 : 0;

		// first half gate
		GCSignal G1 = gb.hash(labelL[0], gid, false);
		TG = G1.xor(gb.hash(labelL[1], gid, false)).xor((cR == 1) ? R : GCSignal.ZERO);
		WG = G1.xor((cL == 1) ? TG : GCSignal.ZERO);
		
		// second half gate
		G1 = gb.hash(labelR[0], gid, true);
		TE = G1.xor(gb.hash(labelR[1], gid, true)).xor(labelL[0]);
		WE = G1.xor((cR == 1) ? (TE.xor(labelL[0])) : GCSignal.ZERO);
		
		// send the encrypted gate
		try {
			Flag.sw.startGCIO();
			TG.send(os);
			TE.send(os);
			Flag.sw.stopGCIO();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		// combine halves
		return WG.xor(WE);
	}
	
	public GCSignal and(GCSignal a, GCSignal b) {
		++ands;
		Flag.sw.startGC();
		GCSignal res;
		if (a.isPublic() && b.isPublic())
			res = new GCSignal(a.v && b.v);
		else if (a.isPublic())
			res = a.v ? b : new GCSignal(false);
		else if (b.isPublic())
			res = b.v ? a : new GCSignal(false);
		else {
			GCSignal ret = garble(a, b);
			gid++;
			res = ret;
			gatesRemain = true;
		}
		Flag.sw.stopGC();
		return res;
	}
}