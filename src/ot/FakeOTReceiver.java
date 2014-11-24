// Copyright (C) 2013 by Yan Huang <yhuang@cs.umd.edu>
// Improved by Xiao Shaun Wang <wangxiao@cs.umd.edu>

package ot;

import gc.GCSignal;

import java.io.InputStream;
import java.io.OutputStream;

public class FakeOTReceiver extends OTReceiver {
	public FakeOTReceiver(InputStream in, OutputStream out) {
		super(in, out);
	}

	@Override
	public GCSignal receive(boolean c) {
		GCSignal[] m = new GCSignal[2];
		m[0] = GCSignal.receive(is);
		m[1] = GCSignal.receive(is);
		return m[c ? 1 : 0];
	}

	@Override
	public GCSignal[] receive(boolean[] c) {
		GCSignal[] res = new GCSignal[c.length];
		for (int i = 0; i < c.length; i++) {
			GCSignal[] m = new GCSignal[2];
			m[0] = GCSignal.receive(is);
			m[1] = GCSignal.receive(is);
			res[i] = m[c[i] ? 1 : 0];
		}
		return res;
	}
}
