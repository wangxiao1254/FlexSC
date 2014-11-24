package gc.halfANDs;

import flexsc.Flag;
import gc.GCEvaComp;
import gc.GCSignal;

import java.io.InputStream;
import java.io.OutputStream;

public class GCEva extends GCEvaComp {
	Garbler gb;

	public GCEva(InputStream is, OutputStream os) {
		super(is, os);
		gb = new Garbler();
	}

	public GCSignal and(GCSignal a, GCSignal b) {
		Flag.sw.startGC();
		GCSignal res;
		if (a.isPublic() && b.isPublic())
			res =  new GCSignal(a.v && b.v);
		else if (a.isPublic())
			res =  a.v ? b : new GCSignal(false);
		else if (b.isPublic())
			res = b.v ? a : new GCSignal(false);
		else {

			int i0 = a.getLSB() ? 1 : 0;
			int i1 = b.getLSB() ? 1 : 0;

			GCSignal TG = GCSignal.ZERO, WG, TE = GCSignal.ZERO, WE;
			try {
				Flag.sw.startGCIO();
				TG = GCSignal.receive(is);
				TE = GCSignal.receive(is);
				Flag.sw.stopGCIO();
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}

			WG = gb.hash(a, gid, false).xor((i0 == 1) ? TG : GCSignal.ZERO);
			WE = gb.hash(b, gid, true).xor((i1 == 1) ? (TE.xor(a)) : GCSignal.ZERO);
			
			GCSignal out = WG.xor(WE);
			
			gid++;
			res =  out;
		}
		Flag.sw.stopGC();
		return res;
	}
}