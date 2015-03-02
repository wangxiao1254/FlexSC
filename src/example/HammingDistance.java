package example;

import util.EvaRunnable;
import util.GenRunnable;
import circuits.arithmetic.IntegerLib;
import flexsc.CompEnv;
import gc.BadLabelException;

public class HammingDistance {
	
	static public<T> T[] compute(CompEnv<T> gen, T[] inputA, T[] inputB){
		return  new IntegerLib<T>(gen).hammingDistance(inputA, inputB);
	}

	
	public static class Generator<T> extends GenRunnable<T> {

		T[] inputA;
		T[] inputB;
		T[] scResult;
		
		@Override
		public void prepareInput(CompEnv<T> gen) {
			boolean[] in = new boolean[10000];
			for(int i = 0; i < in.length; ++i)
				in[i] = CompEnv.rnd.nextBoolean();
			inputA = gen.inputOfAlice(in);
			gen.flush();
			inputB = gen.inputOfBob(new boolean[10000]);
		}
		
		@Override
		public void secureCompute(CompEnv<T> gen) {
			scResult = compute(gen, inputA, inputB);
		}
		@Override
		public void prepareOutput(CompEnv<T> gen) throws BadLabelException {
			System.out.println(gen.outputToAlice(scResult));
		}
		
	}
	
	public static class Evaluator<T> extends EvaRunnable<T> {
		T[] inputA;
		T[] inputB;
		T[] scResult;
		
		@Override
		public void prepareInput(CompEnv<T> gen) {
			boolean[] in = new boolean[10000];
			for(int i = 0; i < in.length; ++i)
				in[i] = CompEnv.rnd.nextBoolean();
			inputA = gen.inputOfAlice(new boolean[10000]);
			gen.flush();
			inputB = gen.inputOfBob(in);
		}
		
		@Override
		public void secureCompute(CompEnv<T> gen) {
			scResult = compute(gen, inputA, inputB);
		}
		
		@Override
		public void prepareOutput(CompEnv<T> gen) throws BadLabelException {
			gen.outputToAlice(scResult);
		}
	}
}
