// Copyright (C) 2014 by Xiao Shaun Wang <wangxiao@cs.umd.edu>
package oram;

import circuits.arithmetic.IntegerLib;
import flexsc.CompEnv;

public class LinearScanOram<T> {
	public T[][] content;
	public CompEnv<T> env;
	public int lengthOfIden;
	public IntegerLib<T> lib;
	public int dataSize;
	public LinearScanOram(CompEnv<T> env, int N, int dataSize) {
		this.env = env;
		this.dataSize = dataSize;
		lib = new IntegerLib<T>(env);
		content = env.newTArray(N, 0);
		long a = 1;
		lengthOfIden = 1;
		while (a < N) {
			a *= 2;
			++lengthOfIden;
		}

		--lengthOfIden;
		for(int i = 0; i < N; ++i)
			content[i] = lib.zeros(dataSize);
	}

	public void add(T[] iden, T[] data, T dummy) {
		T[] iden1 = lib.padSignal(iden, lengthOfIden);
		for(int i = 0; i < content.length; ++i) {
			T eq = lib.eq(iden1, lib.toSignals(i, lengthOfIden));
			eq = lib.and(eq, dummy);
			content[i] = lib.mux(content[i], data, eq);
		}
	}
	
	public void add(T[] iden, T[] data) {
		T[] iden1 = lib.padSignal(iden, lengthOfIden);
		for(int i = 0; i < content.length; ++i) {
			T eq = lib.eq(iden1, lib.toSignals(i, lengthOfIden));
			content[i] = lib.mux(content[i], data, eq);
		}
	}

	public T[] readAndRemove(T[] iden) {
		return readAndRemove(iden, false);
	}

	public T[] readAndRemove(T[] iden, boolean randomWhennotFound) {
		T[] iden1 = lib.padSignal(iden, lengthOfIden);
		T[] res = lib.zeros(content[0].length);
		for(int i = 0; i < content.length; ++i) {
			T eq = lib.eq(iden1, lib.toSignals(i, lengthOfIden));
			res = lib.mux(res, content[i],  eq);
		}
		return res;
	}

	public T[] read(T[] iden) {
		return readAndRemove(iden, false);
	}

	public void write(T[] iden, T[] data) {
		add(iden, data);
	}

	public void write(T[] iden, T[] data, T dummy) {
		add(iden, data, dummy);
	}
	
	public void putBack(T[] scIden, T[] scData) {
		add(scIden, scData);
	}
	public T[] read(int index) {
		return content[index];
	}
}
