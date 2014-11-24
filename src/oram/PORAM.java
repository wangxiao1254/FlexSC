package oram;

import flexsc.CompEnv;

public class PORAM<T> {
	CompEnv<T> env;
	static final int threshold = 128;
	boolean useTrivialOram = false;
	TrivialPrivateOram<T> trivialOram = null;
	CircuitOram<T> circuitOram = null;
	public int lengthOfIden;

	public PORAM(CompEnv<T> env, int N, int dataSize) {
		this.env = env;
		useTrivialOram = N < threshold;
		if(useTrivialOram)
			trivialOram = new TrivialPrivateOram<T>(env, N, dataSize);
		else circuitOram = new CircuitOram<T>(env, N, dataSize);
	}
	
	public T[] conditionalReadAndRemove(T[] scIden, T[] pos, T condition) {
		if (useTrivialOram)
			return trivialOram.conditionalReadAndRemove(scIden, condition);
		else
			return circuitOram.conditionalReadAndRemove(scIden, pos, condition);
	}

	public void conditionalPutBack(T[] scIden, T[] scNewPos, T[] scData,
			T condition) throws Exception {
		if (useTrivialOram)
			trivialOram.conditionalPutBack(scIden, scData, condition);
		else
			circuitOram.conditionalPutBack(scIden, scNewPos, scData, condition);
	}
}
