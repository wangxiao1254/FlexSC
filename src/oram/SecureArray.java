// Copyright (C) 2014 by Xiao Shaun Wang <wangxiao@cs.umd.edu>
package oram;

import java.util.Arrays;

import flexsc.CompEnv;
import gc.BadLabelException;

public class SecureArray<T> {
	static final int threshold = 256;
	boolean useTrivialOram = false;
	public LinearScanOram<T> trivialOram = null;
	public RecursiveCircuitOram<T> circuitOram = null;
	public int lengthOfIden;

	public SecureArray(CompEnv<T> env, int N, int dataSize) throws Exception {
		useTrivialOram = N <= threshold;
		if (useTrivialOram) {
			trivialOram = new LinearScanOram<T>(env, N, dataSize);
			lengthOfIden = trivialOram.lengthOfIden;
		} else {
			circuitOram = new RecursiveCircuitOram<T>(env, N, dataSize);
			lengthOfIden = circuitOram.lengthOfIden;
		}
	}

	public T[] readAndRemove(T[] iden) throws BadLabelException {
		return circuitOram.clients.get(0).readAndRemove(iden, 
				Arrays.copyOfRange(circuitOram.clients.get(0).lib.declassifyToBoth(iden), 0, circuitOram.clients.get(0).lengthOfPos), false);
	}

	public T[] read(T[] iden) throws BadLabelException {
		if (useTrivialOram)
			return trivialOram.read(iden);
		else
			return circuitOram.read(iden);
	}

	public void write(T[] iden, T[] data) throws Exception {
		if (useTrivialOram)
			trivialOram.write(iden, data);
		else
			circuitOram.write(iden, data);
	}
	
	public void conditionalWrite(T[] iden, T[]data, T condition) throws BadLabelException {
		if(useTrivialOram) {
		T[] readData = trivialOram.readAndRemove(iden);
		T[] toAdd = trivialOram.lib.mux(readData, data, condition);
		trivialOram.putBack(iden, toAdd);
		}
		else {
			//op == 1 means write, 0 means read
			circuitOram.access(iden, data, condition);
		}
	}
}