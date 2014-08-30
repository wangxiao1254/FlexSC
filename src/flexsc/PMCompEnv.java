// Copyright (C) 2013 by Yan Huang <yhuang@cs.umd.edu>
// 					 and Xiao Shaun Wang <wangxiao@cs.umd.edu>

package flexsc;

import java.io.InputStream;
import java.io.OutputStream;

import test.Utils;

/*
 * The computational environment for performance measurement. 
 */
public class PMCompEnv extends BooleanCompEnv {
	public static class Statistics {
		public int andGate = 0;
		public int xorGate = 0;
		public int OTs = 0;
		public int NumEncAlice = 0;
		public int NumEncBob = 0;
		public void flush() {
			andGate = 0;
			xorGate = 0;
			OTs = 0;
			NumEncAlice = 0;
			NumEncBob = 0;
		}
		public void add(Statistics s2) {
			andGate += s2.andGate;
			xorGate += s2.xorGate;
			OTs += s2.OTs;
			NumEncAlice += s2.NumEncAlice;
			NumEncBob+=s2.NumEncBob;
		}
		public void finalize() {
			NumEncAlice = andGate*4+OTs*2;
			NumEncBob= andGate*1+OTs*1;
		}
		
		public Statistics newInstance() {
			Statistics s = new Statistics();
			s.andGate = andGate;
			s.xorGate = xorGate;
			s.OTs = OTs;
			s.NumEncAlice = NumEncAlice;
			s.NumEncBob = NumEncBob;
			return s;
		}
	}
	
	public Statistics statistic;
	Boolean t = true;
	Boolean f = false;

	public PMCompEnv(InputStream is, OutputStream os, Party p) {
		super(is, os, p, Mode.COUNT);
		this.p = p;
		t = true;
		f = false;
		statistic = new Statistics();
	}
	
	@Override
	public Boolean inputOfAlice(boolean in) throws Exception {
		++statistic.OTs;
		return f;
	}

	@Override
	public Boolean inputOfBob(boolean in) throws Exception {
		return f;
	}

	@Override
	public boolean outputToAlice(Boolean out) throws Exception {
		return false;
	}

	@Override
	public Boolean and(Boolean a, Boolean b) throws Exception {
		++statistic.andGate;
		return f;
	}

	@Override
	public Boolean xor(Boolean a, Boolean b) {
		++statistic.xorGate;
		return f;
	}

	@Override
	public Boolean not(Boolean a) {
		++statistic.xorGate;
		return f;
	}

	@Override
	public Boolean ONE() {
		return t;
	}

	@Override
	public Boolean ZERO() {
		return f;
	}

	@Override
	public boolean[] outputToAlice(Boolean[] out) throws Exception {
		return Utils.tobooleanArray(out);
	}

	@Override
	public Boolean[] inputOfAlice(boolean[] in) throws Exception {
		statistic.OTs += in.length;
		return Utils.toBooleanArray(in);	
	}

	@Override
	public Boolean[] inputOfBob(boolean[] in) throws Exception {
		return Utils.toBooleanArray(in);
	}

	@Override
	public CompEnv<Boolean> getNewInstance(InputStream in, OutputStream os) {
		return new PMCompEnv(in, os, this.getParty());
	}
}