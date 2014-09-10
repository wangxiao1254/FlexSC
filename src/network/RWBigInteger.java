package network;

import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;

public final class RWBigInteger {
	public static void writeBI(OutputStream os, BigInteger bi) {
		try {
//			byte[] rep = bi.toByteArray();
//			os.write(ByteBuffer.allocate(4).putInt(rep.length).array());
//			os.write(rep);
			Server.writeByte(os, bi.toByteArray());
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public static BigInteger readBI(InputStream is) {
		byte[] rep = null;
		try {
//			is.read(temp);
//			rep = new byte[ByteBuffer.wrap(temp).getInt()];
//			is.read(rep);
			rep = Server.readBytes(is);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		
//		try{ if (len == 0) throw new Exception("unbelievable"); } catch (Exception e) {e.printStackTrace();}
		
		return (rep == null)?BigInteger.ZERO:new BigInteger(rep);
	}	
}
