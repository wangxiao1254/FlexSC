// Copyright (C) 2014 by Xiao Shaun Wang <wangxiao@cs.umd.edu>
package oram;

import flexsc.CompEnv;

//this should be the only interface used in practice for ram model secure computation
// it determine the best oram scheme according to size of array.
public class SecureArray<T> {
	static final int threshold = 128;
	boolean useTrivialOram = false;
	TrivialPrivateOram<T> trivialOram = null;
	RecursiveCircuitOram<T> circuitOram = null;
	public SecureArray(CompEnv<T> env, int N, int dataSize) throws Exception{
		useTrivialOram = N <= threshold;
		if(useTrivialOram)
			trivialOram = new TrivialPrivateOram<T>(env, N, dataSize);
		else 
			circuitOram = new RecursiveCircuitOram<T>(env, N, dataSize);
	}
	
	public T[] read(T[] iden) throws Exception{
		if(useTrivialOram)
			return trivialOram.read(iden);
		else
			return circuitOram.read(iden);
	}
	
	public void write(T[] iden, T[] data) throws Exception{
		if(useTrivialOram)
			trivialOram.write(iden, data);
		else
			circuitOram.write(iden, data);
	}
}
