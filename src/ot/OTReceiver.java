// Copyright (C) 2013 by Yan Huang <yhuang@cs.umd.edu>
// Improved by Xiao Shaun Wang <wangxiao@cs.umd.edu>

package ot;

import gc.GCSignal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class OTReceiver {
	InputStream is;
	OutputStream os;
	int msgBitLength;

	public OTReceiver(InputStream in, OutputStream out) {
		is = in;
		os = out;
	}

	public abstract GCSignal receive(boolean c) throws IOException;

	public abstract GCSignal[] receive(boolean[] c) throws IOException;
}
