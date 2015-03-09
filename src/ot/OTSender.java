// Copyright (C) 2013 by Yan Huang <yhuang@cs.umd.edu>
// Improved by Xiao Shaun Wang <wangxiao@cs.umd.edu>

package ot;

import gc.GCSignal;

import java.io.IOException;

import network.Network;

public abstract class OTSender {
	Network channel;
	int msgBitLength;

	public OTSender(int bitLen, Network channel) {
		this.channel = channel;
		msgBitLength = bitLen;
	}

	public abstract void send(GCSignal[] m) throws IOException;

	public abstract void send(GCSignal[][] m) throws IOException;
}