// Copyright (C) 2014 by Xiao Shaun Wang <wangxiao@cs.umd.edu>
package oram;

import util.Utils;
import circuits.arithmetic.IntegerLib;
import flexsc.CompEnv;

public class LinearScanOram<T> {
	T[][] content;
	CompEnv<T> env;
	public int lengthOfIden;
	public IntegerLib<T> lib;
	public LinearScanOram(CompEnv<T> env, int N, int dataSize) {
		this.env = env;
		lib = new IntegerLib<T>(env);
		content = env.newTArray(N, 0);
		lengthOfIden = Utils.log2(N);
		for(int i = 0; i < N; ++i)
			content[i] = lib.zeros(dataSize);
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

	public T[] read(int index) {
		return content[index];
	}

	public void write(int index, T[] d) {
		content[index] = d;
	}

	public T[] read(T[] iden) {
		return readAndRemove(iden, false);
	}

	public void write(T[] iden, T[] data) {
		add(iden, data);
	}

	public void putBack(T[] scIden, T[] scData) {
		add(scIden, scData);
	}
}
