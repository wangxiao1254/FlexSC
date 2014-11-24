// Copyright (C) 2014 by Xiao Shaun Wang <wangxiao@cs.umd.edu>
package circuits;

import java.io.IOException;
import java.util.Arrays;

import flexsc.CompEnv;
import flexsc.Mode;
import flexsc.Party;
import gc.GCSignal;

public class CircuitLib<T> {
	public CompEnv<T> env;
	public final T SIGNAL_ZERO;
	public final T SIGNAL_ONE;

	public CircuitLib(CompEnv<T> e) {
		env = e;
		SIGNAL_ZERO = e.ZERO();
		SIGNAL_ONE = e.ONE();
	}

	public T[] toSignals(int a, int width) {
		T[] result = env.newTArray(width);
		for (int i = 0; i < width; ++i) {
			if ((a & 1) == 1)
				result[i] = SIGNAL_ONE;
			else
				result[i] = SIGNAL_ZERO;
			a >>= 1;
		}
		return result;
	}

	public T[] randBools(int length) {
		if(env.m == Mode.COUNT) {
			return zeros(length);
		}
		boolean[] res = new boolean[length];
		for (int i = 0; i < length; ++i)
			res[i] = CompEnv.rnd.nextBoolean();
		T[] alice = env.inputOfAlice(res);
		T[] bob = env.inputOfBob(res);
		T[] resSC = xor(alice, bob);

		return resSC;
	}

	public boolean[] declassifyToAlice(T[] x) {
		return env.outputToAlice(x);
	}

	public boolean[] declassifyToBob(T[] x) {
		return env.outputToBob(x);
	}

	public boolean[] declassifyToBoth(T[] x) {
		if(env.m == Mode.COUNT){
			return new boolean[x.length];
		}
		boolean[] pos = env.outputToAlice(x);
		try {
			if (env.getParty() == Party.Alice) {

				env.os.write(new byte[] { (byte) pos.length });
				byte[] tmp = new byte[pos.length];
				for (int i = 0; i < pos.length; ++i)
					tmp[i] = (byte) (pos[i] ? 1 : 0);
				env.os.write(tmp);
				env.flush();
			} else {
				byte[] l = new byte[1];
				env.is.read(l);
				byte tmp[] = new byte[l[0]];
				env.is.read(tmp);
				pos = new boolean[l[0]];
				for (int k = 0; k < tmp.length; ++k) {
					pos[k] = ((tmp[k] - 1) == 0);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return pos;
	}

	// Defaults to 32 bit constants.
	public T[] toSignals(int value) {
		return toSignals(value, 32);
	}

	public GCSignal[] toSignals(GCSignal[] value) {
		return value;
	}

	public T[] zeros(int length) {
		T[] result = env.newTArray(length);
		for (int i = 0; i < length; ++i) {
			result[i] = SIGNAL_ZERO;
		}
		return result;
	}

	public T[] ones(int length) {
		T[] result = env.newTArray(length);
		for (int i = 0; i < length; ++i) {
			result[i] = SIGNAL_ONE;
		}
		return result;
	}

	/*
	 * Basic logical operations on Signal and Signal[]
	 */
	public T and(T x, T y) {
		assert (x != null && y != null) : "CircuitLib.and: bad inputs";

		return env.and(x, y);
	}

	public T[] and(T[] x, T[] y) {
		assert (x != null && y != null && x.length == y.length) : "CircuitLib.and[]: bad inputs";

		T[] result = env.newTArray(x.length);
		for (int i = 0; i < x.length; ++i) {
			result[i] = and(x[i], y[i]);
		}
		return result;
	}

	public T xor(T x, T y) {
		assert (x != null && y != null) : "CircuitLib.xor: bad inputs";

		return env.xor(x, y);
	}

	public T[] xor(T[] x, T[] y) {
		assert (x != null && y != null && x.length == y.length) : "CircuitLib.xor[]: bad inputs";

		T[] result = env.newTArray(x.length);
		for (int i = 0; i < x.length; ++i) {
			result[i] = xor(x[i], y[i]);
		}
		return result;
	}

	public T not(T x) {
		assert (x != null) : "CircuitLib.not: bad input";

		return env.xor(x, SIGNAL_ONE);
	}

	// tested
	public T[] not(T[] x) {
		assert (x != null) : "CircuitLib.not[]: bad input";

		T[] result = env.newTArray(x.length);
		for (int i = 0; i < x.length; ++i) {
			result[i] = not(x[i]);
		}
		return result;
	}

	public T or(T x, T y) {
		assert (x != null && y != null) : "CircuitLib.or: bad inputs";

		return xor(xor(x, y), and(x, y)); // http://stackoverflow.com/a/2443029
	}

	public T[] or(T[] x, T[] y) {
		assert (x != null && y != null && x.length == y.length) : "CircuitLib.or[]: bad inputs";

		T[] result = env.newTArray(x.length);
		for (int i = 0; i < x.length; ++i) {
			result[i] = or(x[i], y[i]);
		}
		return result;
	}

	/*
	 * Output x when c == 0; Otherwise output y.
	 */
	public T mux(T x, T y, T c) {
		assert (x != null && y != null && c != null) : "CircuitLib.mux: bad inputs";
		T t = xor(x, y);
		t = and(t, c);
		T ret = xor(t, x);
		return ret;
	}

	public T[] mux(T[] x, T[] y, T c) {
		assert (x != null && y != null && x.length == y.length) : "CircuitLib.mux[]: bad inputs";

		T[] ret = env.newTArray(x.length);
		for (int i = 0; i < x.length; i++)
			ret[i] = mux(x[i], y[i], c);

		return ret;
	}

	public T[] padSignal(T[] a, int length) {
		T[] res = zeros(length);
		for (int i = 0; i < a.length && i < length; ++i)
			res[i] = a[i];
		return res;
	}

	public T[] padSignedSignal(T[] a, int length) {
		T[] res = env.newTArray(length);
		for (int i = 0; i < a.length && i < length; ++i)
			res[i] = a[i];
		for (int i = a.length; i < length; ++i)
			res[i] = a[a.length - 1];
		return res;
	}

	public T[] copy(T[] x) {
		return Arrays.copyOf(x, x.length);
	}
}