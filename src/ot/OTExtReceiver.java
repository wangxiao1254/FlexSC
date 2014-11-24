// Copyright (C) 2013 by Yan Huang <yhuang@cs.umd.edu>
// Improved by Xiao Shaun Wang <wangxiao@cs.umd.edu>

package ot;

import flexsc.Flag;
import gc.GCSignal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Arrays;

import network.RWBigInteger;
import ot.OTExtSender.SecurityParameter;
import rand.ISAACProvider;

public class OTExtReceiver extends OTReceiver {
	// private static SecureRandom rnd = new SecureRandom();
	static SecureRandom rnd;
	static {
		Security.addProvider(new ISAACProvider());
		try {
			rnd = SecureRandom.getInstance("ISAACRandom");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private int msgBitLength;

	private OTSender snder;
	private GCSignal[][] keyPairs;

	Cipher cipher;

	public OTExtReceiver(InputStream in, OutputStream out) {
		super(in, out);

		cipher = new Cipher();

		try {
			initialize();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	boolean[] s = new boolean[SecurityParameter.k1];

	public GCSignal[] receive(boolean[] choices) throws IOException {
		GCSignal[] keys = new GCSignal[SecurityParameter.k1];

		boolean[] c = new boolean[SecurityParameter.k1 + choices.length];
		for (int i = 0; i < SecurityParameter.k1; i++)
			c[i] = rnd.nextBoolean();
		for (int i = SecurityParameter.k1; i < c.length; i++)
			c[i] = choices[i - SecurityParameter.k1];

		GCSignal[] received = reverseAndExtend(keyPairs, c, msgBitLength, is,
				os, cipher);

		for (int i = 0; i < OTExtSender.SecurityParameter.k1; i++) {
			keys[i] = received[i];
			s[i] = c[i];
		}
		for (int i = 0; i < OTExtSender.SecurityParameter.k1; i++) {
			keyPairs[i][0] = GCSignal.freshLabel(rnd);
			keyPairs[i][1] = GCSignal.freshLabel(rnd);
		}
		OTExtSender.reverseAndExtend(s, keys, msgBitLength, keyPairs, is, os,
				cipher);

		return Arrays.copyOfRange(received, SecurityParameter.k1,
				received.length);
	}

	static GCSignal[] reverseAndExtend(GCSignal[][] keyPairs,
			boolean[] choices, int msgBitLength, InputStream is,
			OutputStream os, Cipher cipher) throws IOException {
		BigInteger[][] msgPairs = new BigInteger[SecurityParameter.k1][2];
		BigInteger[][] cphPairs = new BigInteger[SecurityParameter.k1][2];

		BitMatrix T = new BitMatrix(choices.length, SecurityParameter.k1);
		T.initialize(rnd);

		BigInteger biChoices = OTExtSender.fromBoolArray(choices);
		for (int i = 0; i < SecurityParameter.k1; i++) {
			msgPairs[i][0] = T.data[i];
			msgPairs[i][1] = T.data[i].xor(biChoices);

			cphPairs[i][0] = cipher.encrypt(keyPairs[i][0].bytes,
					msgPairs[i][0], choices.length);
			cphPairs[i][1] = cipher.encrypt(keyPairs[i][1].bytes,
					msgPairs[i][1], choices.length);
		}

		Flag.sw.startOTIO();
		for (int i = 0; i < SecurityParameter.k1; i++) {
			RWBigInteger.writeBI(os, cphPairs[i][0]);
			RWBigInteger.writeBI(os, cphPairs[i][1]);
		}
		os.flush();
		Flag.sw.stopOTIO();

		BitMatrix tT = T.transpose();
		GCSignal[] res = new GCSignal[choices.length];

		GCSignal[][] y = new GCSignal[choices.length][2];

		Flag.sw.startOTIO();
		for (int i = 0; i < choices.length; i++) {
			y[i][0] = GCSignal.receive(is);
			y[i][1] = GCSignal.receive(is);
		}
		Flag.sw.stopOTIO();

		for (int i = 0; i < choices.length; i++) {
			int sigma = choices[i] ? 1 : 0;
			res[i] = cipher.dec(GCSignal.newInstance(tT.data[i].toByteArray()),
					y[i][sigma], i);
		}

		return res;
	}

	private void initialize() throws Exception {
		Flag.sw.startOTIO();
		msgBitLength = is.read();
		Flag.sw.stopOTIO();

		snder = new NPOTSender(OTExtSender.SecurityParameter.k1, is, os);

		keyPairs = new GCSignal[OTExtSender.SecurityParameter.k1][2];
		for (int i = 0; i < OTExtSender.SecurityParameter.k1; i++) {
			keyPairs[i][0] = GCSignal.freshLabel(rnd);
			keyPairs[i][1] = GCSignal.freshLabel(rnd);
		}

		snder.send(keyPairs);
		os.flush();
	}

	GCSignal[] pool;
	int poolIndex = 0;

	@Override
	public GCSignal receive(boolean c) {
		try {
			throw new Exception(
					"It doesn't make sense to do single OT with OT extension!");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}