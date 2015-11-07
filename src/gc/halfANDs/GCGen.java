package gc.halfANDs;

import flexsc.Flag;
import flexsc.Mode;
import gc.GCGenComp;
import gc.GCSignal;
import network.Network;

public class GCGen extends GCGenComp {
	Garbler gb;

	public GCGen(Network channel){
		super(channel, Mode.OPT);
		gb = new Garbler();
		labelL[0] = new GCSignal(new byte[10]);
		labelL[1] = new GCSignal(new byte[10]);
		labelR[0] = new GCSignal(new byte[10]);
		labelR[1] = new GCSignal(new byte[10]);
		TG = new GCSignal(new byte[10]);
		WG = new GCSignal(new byte[10]);
		TE = new GCSignal(new byte[10]);
		WE = new GCSignal(new byte[10]);
		G1 = new GCSignal(new byte[10]);
		TMP = new GCSignal(new byte[10]);
	}

	private GCSignal labelL[] = new GCSignal[2];
	private GCSignal labelR[] = new GCSignal[2];

	private GCSignal TG, WG, TE, WE, G1, TMP;
	
	private GCSignal garble(GCSignal a, GCSignal b) {
		labelL[0] = a;
		GCSignal.xor(R, labelL[0], labelL[1]);
//		labelL[1] = R.xor(labelL[0]);
		labelR[0] = b;
		GCSignal.xor(R, labelR[0], labelR[1]);
//		labelR[1] = R.xor(labelR[0]);

		int cL = a.getLSB();
		int cR = b.getLSB();

		// first half gate
//		GCSignal G1 = gb.hash(labelL[0], gid, false);
		gb.hash(labelL[0], gid, false, G1);
		gb.hash(labelL[1], gid, false, TMP);
//		TG = G1.xor(TMP).xor((cR == 1) ? R : GCSignal.ZERO);
		GCSignal.xor(G1, TMP, TMP);
		GCSignal.xor(TMP, (cR == 1) ? R : GCSignal.ZERO, TG);
//		WG = G1.xor((cL == 1) ? TG : GCSignal.ZERO);
		GCSignal.xor(G1, (cL == 1) ? TG : GCSignal.ZERO, WG);
		
		// second half gate
//		G1 = gb.hash(labelR[0], gid, true);
		gb.hash(labelR[0], gid, true, G1);
		gb.hash(labelR[1], gid, true, TMP);
//		TE = G1.xor(TMP).xor(labelL[0]);
		GCSignal.xor(G1, TMP, TMP);
		GCSignal.xor(TMP, labelL[0], TE);
//		WE = G1.xor((cR == 1) ? (TE.xor(labelL[0])) : GCSignal.ZERO);
		GCSignal.xor(G1, (cR == 1) ? (TE.xor(labelL[0])) : GCSignal.ZERO, WE);
		
		// send the encrypted gate
		try {
			Flag.sw.startGCIO();
			TG.send(channel);
			TE.send(channel);
			Flag.sw.stopGCIO();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		// combine halves
		return WG.xor(WE);
	}
	
	public GCSignal and(GCSignal a, GCSignal b) {

		Flag.sw.startGC();
		GCSignal res;
		if (a.isPublic() && b.isPublic())
			res = ((a.v && b.v)? new GCSignal(true): new GCSignal(false));
		else if (a.isPublic())
			res = a.v ? b : new GCSignal(false);
		else if (b.isPublic())
			res = b.v ? a : new GCSignal(false);
		else {
			++numOfAnds;
			GCSignal ret = garble(a, b);
			gid++;
			res = ret;
			gatesRemain = true;
		}
		Flag.sw.stopGC();
		return res;
	}
}