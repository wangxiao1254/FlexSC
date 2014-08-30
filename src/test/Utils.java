package test;

import java.math.BigInteger;

public class Utils {
	public static Boolean[] toBooleanArray(boolean[] a) {
		Boolean[] res = new Boolean[a.length];
		for (int i = 0; i < a.length; i++)
			res[i] = a[i];
		return res;
	}
	
	public static boolean[] tobooleanArray(Boolean[] a) {
		boolean[] res = new boolean[a.length];
		for (int i = 0; i < a.length; i++)
			res[i] = a[i];
		return res;
	}
	
	public static boolean[] fromInt(int value, int width) {
		boolean[] res = new boolean[width];
		for (int i = 0; i < width; i++)
			res[i] = (((value >> i) & 1) == 0) ? false : true;
		
		return res;
	}
	
	public static int toInt(boolean[] value) {
		int res = 0;
		for (int i = 0; i < value.length; i++)
			res =  (value[i]) ? (res | (1<<i)) : res;
		
		return res;
	}
	
	public static long toUnSignedInt(boolean[] v) {
		long result = 0;
		for(int i = 0; i < v.length; ++i) {
			if(v[i])
				result += ((long)1<<i);
		}
		return result;
	}
	
	public static long toSignedInt(boolean [] v) {
		int i = 0;
		if(v[v.length-1] == false) return toUnSignedInt(v);
		
		boolean[] c2 = new boolean[v.length];
		while(v[i] != true){
			c2[i] = v[i];
			++i;
		}
		c2[i] = v[i];
		++i;
		for(; i < v.length; ++i)
			c2[i] = !v[i];
		return toUnSignedInt(c2)*-(long)(1);
	}
	
	public static boolean[] fromLong(long value, int width) {
		boolean[] res = new boolean[width];
		for (int i = 0; i < width; i++)
			res[i] = (((value >> i) & 1) == 0) ? false : true;
		
		return res;
	}
	
	public static long toLong(boolean[] value) {
		long res = 0;
		for (int i = 0; i < value.length; i++)
			res =  (value[i]) ? (res | (1<<i)) : res;
		
		return res;
	}

	public static float toFloat(boolean[] value) {
		return Float.intBitsToFloat(toInt(value));
	}
	
	public static boolean[] fromFloat(float value) {
		return fromInt(Float.floatToIntBits(value), 32);
	}
	
	final static int[] mask = { 0b00000001, 0b00000010, 0b00000100, 0b00001000,
			0b00010000, 0b00100000, 0b01000000, 0b10000000 };

	public static boolean[] fromBigInteger(BigInteger bd, int length) {
		byte[] b = bd.toByteArray();
		boolean[] result = new boolean[length];
		for (int i = 0; i < b.length; ++i) {
			for (int j = 0; j < 8 && i * 8 + j < length; ++j)
				result[i * 8 + j] = (((b[b.length - i - 1] & mask[j]) >> j) == 1);
		}
		return result;
	}

	public static BigInteger toBigInteger(boolean[] b) {
		BigInteger res = new BigInteger("0");
		BigInteger c = new BigInteger("1");
		for (int i = 0; i < b.length; i++) {
			if (b[i])
				res = res.add(c);
			c = c.multiply(new BigInteger("2"));
		}
		return res;
	}
	
	public static boolean[] fromFixPoint(double a, int width, int offset) {
		a *= Math.pow(2, offset);
		return Utils.fromLong( (long) a, width);
	}
	
	public static double toFixPoint(boolean[] b, int width, int offset) {
		double a = toSignedInt(b);
		a /= Math.pow(2, offset);
		return a;
	}

}
