// Copyright (C) 2014 by Xiao Shaun Wang <wangxiao@cs.umd.edu>
package flexsc;

import java.io.InputStream;
import java.io.OutputStream;

public abstract class BooleanCompEnv extends CompEnv<Boolean> {
	Boolean t = true;
	Boolean f = false;
	public BooleanCompEnv(InputStream is, OutputStream os, Party p, Mode m) {
		super(is, os, p, m);
	}

	@Override
	public Boolean[] newTArray(int len) {
		Boolean[] res = new Boolean[len];
		return res;
	}

	@Override
	public Boolean newT(boolean v) {
		return v;
	}

	@Override
	public Boolean[][] newTArray(int d1, int d2) {
		return new Boolean[d1][d2];
	}

	@Override
	public Boolean[][][] newTArray(int d1, int d2, int d3) {
		return new Boolean[d1][d2][d3];
	}
	
	@Override
	public Boolean ONE() {
		return t;
	}

	@Override
	public Boolean ZERO() {
		return f;
	}
}
