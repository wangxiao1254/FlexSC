// Copyright (C) 2014 by Xiao Shaun Wang <wangxiao@cs.umd.edu>
package flexsc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Security;

import rand.ISAACProvider;

public abstract class CompEnv<T> {

	public static SecureRandom rnd;
	static{
		Security.addProvider(new ISAACProvider());
		try {
			rnd = SecureRandom.getInstance("ISAACRandom");

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("rawtypes")
	public static CompEnv getEnv(Mode mode, Party p, InputStream is,
			OutputStream os) {
		if (mode == Mode.REAL)
			if (p == Party.Bob)
				return new gc.regular.GCEva(is, os);
			else
				return new gc.regular.GCGen(is, os);		
		else if (mode == Mode.OPT)
			if (p == Party.Bob)
				return new gc.halfANDs.GCEva(is, os);
			else
				return new gc.halfANDs.GCGen(is, os);
		else if (mode == Mode.VERIFY)
			return new CVCompEnv(is, os, p);
		else if (mode == Mode.COUNT)
			return new PMCompEnv(is, os, p);
		else {
			try {
				throw new Exception("not a supported Mode!");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

	}

	public InputStream is;
	public OutputStream os;
	public Party p;
	public Mode m;

	public CompEnv(InputStream is, OutputStream os, Party p, Mode m) {
		this.is = is;
		this.os = os;
		this.m = m;
		this.p = p;
	}

	public abstract T inputOfAlice(boolean in);

	public abstract T inputOfBob(boolean in);

	public abstract boolean outputToAlice(T out);

	public abstract boolean outputToBob(T out);

	public abstract T[] inputOfAlice(boolean[] in);

	public abstract T[] inputOfBob(boolean[] in);

	public abstract boolean[] outputToAlice(T[] out);

	public abstract boolean[] outputToBob(T[] out);

	public abstract T and(T a, T b);

	public abstract T xor(T a, T b);

	public abstract T not(T a);

	public abstract T ONE();

	public abstract T ZERO();

	public abstract T[] newTArray(int len);

	public abstract T[][] newTArray(int d1, int d2);

	public abstract T[][][] newTArray(int d1, int d2, int d3);

	public abstract T newT(boolean v);

	public Party getParty() {
		return p;
	}

	public void flush() {
		try {
			os.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void sync() throws IOException {
		if (getParty() == Party.Alice) {
			is.read();
			os.write(0);
			os.flush(); // dummy I/O to prevent dropping connection earlier than
						// protocol payloads are received.
		} else {
			os.write(0);
			os.flush();
			is.read(); // dummy write to prevent dropping connection earlier
						// than
			// protocol payloads are received.
		}
	}
}