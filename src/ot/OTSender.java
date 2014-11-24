// Copyright (C) 2013 by Yan Huang <yhuang@cs.umd.edu>
// Improved by Xiao Shaun Wang <wangxiao@cs.umd.edu>

package ot;

import gc.GCSignal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class OTSender {
	InputStream is;
	OutputStream os;
	int msgBitLength;

	public OTSender(int bitLen, InputStream in, OutputStream out) {
		is = in;
		os = out;
		msgBitLength = bitLen;
	}

	public abstract void send(GCSignal[] m) throws IOException;

	public abstract void send(GCSignal[][] m) throws IOException;
}