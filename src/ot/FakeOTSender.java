// Copyright (C) 2013 by Yan Huang <yhuang@cs.umd.edu>
// Improved by Xiao Shaun Wang <wangxiao@cs.umd.edu>

package ot;

import gc.GCSignal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FakeOTSender extends OTSender {
	public FakeOTSender(int bitLen, InputStream in, OutputStream out) {
		super(bitLen, in, out);
	}

	@Override
	public void send(GCSignal[] m) {
		m[0].send(os);
		m[1].send(os);
	}

	@Override
	public void send(GCSignal[][] m) throws IOException {
		for (int i = 0; i < m.length; i++) {
			m[i][0].send(os);
			m[i][1].send(os);
		}
		os.flush();
	}
}