package gc.offline;

import flexsc.Flag;
import flexsc.Mode;
import gc.GCEvaComp;
import gc.GCSignal;
import network.Network;

public class GCEva extends GCEvaComp {
	Garbler gb;
	GCSignal[][] gtt = new GCSignal[2][2];

	public GCEva(Network channel) {
		super(channel, Mode.OFFLINE);
		gb = new Garbler();
		gtt[0][0] = GCSignal.ZERO;
	}

	private void receiveGTT() {
		try {
			Flag.sw.startGCIO();
			gtt[0][1] = GCSignal.receive(channel);
			gtt[1][0] = GCSignal.receive(channel);
			gtt[1][1] = GCSignal.receive(channel);
			Flag.sw.stopGCIO();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public GCSignal and(GCSignal a, GCSignal b) {
		Flag.sw.startGC();

		GCSignal res;
		if (a.isPublic() && b.isPublic())
			res = ( (a.v && b.v) ? new GCSignal(true) :new GCSignal(false));
		else if (a.isPublic())
			res = a.v ? b :new GCSignal(false);
		else if (b.isPublic())
			res = b.v ? a :new GCSignal(false);
		else {
			++numOfAnds;
			receiveGTT();

			int i0 = a.getLSB();
			int i1 = b.getLSB();

			res = gb.dec(a, b, gid, gtt[i0][i1]);
			gid++;
		}
		Flag.sw.stopGC();
		return res;
	}
}