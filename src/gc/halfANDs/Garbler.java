package gc.halfANDs;

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
	
	public GCSignal hash(GCSignal lb, long k, boolean b) {
		sha1.update(ByteBuffer.allocate(GCSignal.len+9).put(lb.bytes).putLong(k).put(b?(byte)1:(byte)0));
		return GCSignal.newInstance(sha1.digest());
	}
}
