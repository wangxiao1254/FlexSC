package gc.regular;

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

	public void enc(GCSignal lb0, GCSignal lb1, long k, GCSignal m, GCSignal ret) {
		getPadding(lb0, lb1, k, ret);
		GCSignal.xor(ret, m, ret);
	}

	public void dec(GCSignal lb0, GCSignal lb1, long k, GCSignal c, GCSignal ret) {
		getPadding(lb0, lb1, k, ret);
		GCSignal.xor(ret, c, ret);
	}
	ByteBuffer buffer = ByteBuffer.allocate(GCSignal.len*2+8); 
	private void getPadding(GCSignal lb0, GCSignal lb1, long k, GCSignal ret) {
		buffer.clear();
		sha1.update((buffer.put(lb0.bytes).put(lb1.bytes).putLong(k)).array());
		System.arraycopy(sha1.digest(), 0, ret.bytes, 0, GCSignal.len);
	}
}
