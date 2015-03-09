package gc.offline;

import gc.GCSignal;

import java.nio.ByteBuffer;
import java.security.MessageDigest;

final class Garbler {
	private MessageDigest sha1 = null;
	Garbler() {
        try {
            sha1 = MessageDigest.getInstance("SHA-1");
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
	
	
	public GCSignal enc(GCSignal lb0, GCSignal lb1, long k, GCSignal m) {
		return getPadding(lb0, lb1, k).xor(m);
	}

	public GCSignal dec(GCSignal lb0, GCSignal lb1, long k, GCSignal c) {
		return getPadding(lb0, lb1, k).xor(c);
	}
	
	ByteBuffer buffer = ByteBuffer.allocate(GCSignal.len*2+8);
	private GCSignal getPadding(GCSignal lb0, GCSignal lb1, long k) {
		buffer.clear();
		  sha1.update((buffer.put(lb0.bytes).put(lb1.bytes).putLong(k)));
          GCSignal ret = GCSignal.newInstance(sha1.digest());
        return ret;
    }
}