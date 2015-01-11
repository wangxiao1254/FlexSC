package example;

import util.EvaRunnable;
import util.GenRunnable;
import util.Utils;
import circuits.BitonicSortLib;
import flexsc.CompEnv;

public class Sort {
	public static class Generator<T> extends GenRunnable<T> {

		T[][] inputB;
		
		@Override
		public void prepareInput(CompEnv<T> gen) {
			inputB = gen.newTArray(4000, 0);
			for(int i = 0; i < inputB.length; ++i)
				inputB[i] = gen.inputOfBob(new boolean[16]);
		}
		
		@Override
		public void secureCompute(CompEnv<T> gen) {
			BitonicSortLib<T> lib = new  BitonicSortLib<T>(gen); 
			lib.sort(inputB, lib.SIGNAL_ONE);
		}
		@Override
		public void prepareOutput(CompEnv<T> gen) {
		}
	}
	
	public static class Evaluator<T> extends EvaRunnable<T> {
		T[][] inputB;
		T[] scResult;
		
		@Override
		public void prepareInput(CompEnv<T> gen) {
			inputB = gen.newTArray(4000, 0);
			for(int i = 0; i < inputB.length; ++i)
				inputB[i] = gen.inputOfBob(Utils.fromInt(CompEnv.rnd.nextInt(), 16));
		}
		
		@Override
		public void secureCompute(CompEnv<T> gen) {
			BitonicSortLib<T> lib = new  BitonicSortLib<T>(gen); 
			lib.sort(inputB, lib.SIGNAL_ONE);
		}
		
		@Override
		public void prepareOutput(CompEnv<T> gen) {
		}
	}
}
