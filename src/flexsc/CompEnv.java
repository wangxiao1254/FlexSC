// Copyright (C) 2013 by Yan Huang <yhuang@cs.umd.edu>
// 					 and Xiao Shaun Wang <wangxiao@cs.umd.edu>

package flexsc;

import gc.GCEva;
import gc.GCGen;

import java.io.InputStream;
import java.io.OutputStream;

public abstract class CompEnv<T> {
	@SuppressWarnings("rawtypes")
	public static CompEnv getEnv(Mode mode, Party p, InputStream is, OutputStream os) throws Exception{
		if(mode == Mode.REAL)
			if(p == Party.Bob)
				return new GCEva(is, os);
			else
				return new GCGen(is, os);
		else if(mode == Mode.VERIFY)
			return new CVCompEnv(is,os, p);
		else if(mode == Mode.COUNT)
			return new PMCompEnv(is,os, p);
		else return null;
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
	
	public abstract T inputOfAlice(boolean in) throws Exception;
	public 	abstract T inputOfBob(boolean in) throws Exception;
	public abstract boolean outputToAlice(T out) throws Exception;
	
	public abstract T[] inputOfAlice(boolean[] in) throws Exception;
	public abstract T[] inputOfBob(boolean[] in) throws Exception;
	public abstract boolean[] outputToAlice(T[] out) throws Exception;
	
	public abstract T and(T a, T b) throws Exception;
	public abstract T xor(T a, T b);
	public abstract T not(T a);
	
	public abstract T ONE();
	public abstract T ZERO();
	
	public abstract T[] newTArray(int len);
	public abstract T[][] newTArray(int d1, int d2);
	public abstract T[][][] newTArray(int d1, int d2, int d3);
	public abstract T newT(boolean v);
	
	abstract public CompEnv<T> getNewInstance(InputStream in, OutputStream os) throws Exception;
	public Party getParty(){
		return p;
	}
	
	public void flush() throws Exception {
		os.flush();
	}

}