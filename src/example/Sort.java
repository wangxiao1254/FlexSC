package example;

import util.EvaRunnable;
import util.GenRunnable;
import util.Utils;
import circuits.BitonicSortLib;
import flexsc.CompEnv;

public class Sort {
	static public<T> T[][] compute(CompEnv<T> gen, T[][] inputA, T[][] inputB){
		T[][] in = gen.newTArray(inputA.length+inputB.length, 0);
		System.arraycopy(inputA, 0, in, 0, inputA.length);
		System.arraycopy(inputB, 0, in,inputA.length, inputB.length);
		BitonicSortLib<T> lib = new  BitonicSortLib<T>(gen); 
		lib.sort(in, lib.SIGNAL_ONE);
		return in;
	}
	
	public static class Generator<T> extends GenRunnable<T> {
		T[][] inputB;
		T[][] inputA;
		T[][] in;

		@Override
		public void prepareInput(CompEnv<T> gen) {
			inputB = gen.newTArray(10, 0);
			for(int i = 0; i < 10; ++i)
				inputB[i] = gen.inputOfBob(new boolean[32]);
			inputA = gen.newTArray(10, 0);
			for(int i = 0; i < 10; ++i)
				inputA[i] = gen.inputOfAlice(Utils.fromInt(i, 32));
		}
		
		@Override
		public void secureCompute(CompEnv<T> gen) {
			in = compute(gen, inputA, inputB);
		}
		@Override
		public void prepareOutput(CompEnv<T> gen) {
			for(int i = 0; i < 20; ++i)
				System.out.println(Utils.toInt(gen.outputToAlice(in[i])));
		}
	}
	
	public static class Evaluator<T> extends EvaRunnable<T> {
		T[][] inputB;
		T[] scResult;
		T[][] inputA;
		T[][] in;
		
		@Override
		public void prepareInput(CompEnv<T> gen) {
			inputB = gen.newTArray(10, 0);
			for(int i = 0; i < 10; ++i)
				inputB[i] = gen.inputOfBob(Utils.fromInt(i, 32));
			
			inputA = gen.newTArray(10, 0);
			inputA = gen.inputOfAlice(new boolean[10][32]);
		}
		
		@Override
		public void secureCompute(CompEnv<T> gen) {
			in = compute(gen, inputA, inputB);
		}
		
		@Override
		public void prepareOutput(CompEnv<T> gen) {
			for(int i = 0; i < 20; ++i)
				gen.outputToAlice(in[i]);
		}
		
	}
}
