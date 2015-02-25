// Copyright (C) by Xiao Shaun Wang <wangxiao@cs.umd.edu>

package ot;

import flexsc.CompEnv;
import flexsc.Flag;
import gc.GCSignal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

public class OTPreprocessReceiver  extends OTReceiver {
	GCSignal[] buffer = new GCSignal[OTPreprocessSender.bufferSize];
	boolean[] choose = new boolean[OTPreprocessSender.bufferSize];
	int bufferusage = 0;

	public void fillup() {
		try {
			os.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		while(bufferusage < OTPreprocessSender.bufferSize) {
			int l = Math.min(OTPreprocessSender.fillLength, OTPreprocessSender.bufferSize-bufferusage);
			
			for(int i = bufferusage; i < bufferusage+l; ++i)
				choose[i] = CompEnv.rnd.nextBoolean();
			GCSignal[] kc = null;
			try {
				kc = reciever.receive(Arrays.copyOfRange(choose, bufferusage, bufferusage+l));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.arraycopy(kc, 0, buffer, bufferusage, kc.length);
			bufferusage += l;
		}
		try {
			os.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	OTExtReceiver reciever;
	public OTPreprocessReceiver(InputStream in, OutputStream out) {
		super(in, out);
		reciever = new OTExtReceiver(in, out);
		fillup();
	}


	public GCSignal receive(boolean b) throws IOException {
		bufferusage--;
		byte z = (b^choose[bufferusage]) ? (byte)1 : (byte)0;
		Flag.sw.startOTIO();
		os.write(z);
		os.flush();
		GCSignal[] y = new GCSignal[]{GCSignal.receive(is),  GCSignal.receive(is)};
		Flag.sw.stopOTIO();
		if(bufferusage == 0)
			fillup();
		return y[b?1:0].xor(buffer[bufferusage]);
	}

	public GCSignal[] receive(boolean[] b) throws IOException {
		if(bufferusage < b.length)
			fillup();
		byte[] z = new byte[b.length];
		int tmp = bufferusage;
		for(int i = 0; i < b.length; ++i) {
			--tmp;
			z[i] = (b[i]^choose[tmp]) ? (byte)1 : (byte)0;
		}
		Flag.sw.startOTIO();
		os.write(z);
		os.flush();
		Flag.sw.stopOTIO();
		GCSignal[] ret = new GCSignal[b.length];
		for(int i = 0; i < b.length; ++i) {
			bufferusage--;
			Flag.sw.startOTIO();
			GCSignal[] y = new GCSignal[]{GCSignal.receive(is),  GCSignal.receive(is)};
			Flag.sw.stopOTIO();
			ret[i] = y[b[i]?1:0].xor(buffer[bufferusage]);
		}
		return ret;
	}
}
