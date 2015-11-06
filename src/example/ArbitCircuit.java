package example;

import util.EvaRunnable;
import util.GenRunnable;
import flexsc.CompEnv;
import gc.BadLabelException;

public class ArbitCircuit {
	
	
	static public<T> T compute(CompEnv<T> gen, T inputA, T inputB){
		T c = gen.and(inputA, inputB);
		T d = gen.xor(inputA, c);
		T e = gen.xor(c, d);
		return gen.and(gen.and(c, d), gen.not(e));
	}
	
	public static class Generator<T> extends GenRunnable<T> {

		T inputA;
		T inputB;
		T scResult;
		
		@Override
		public void prepareInput(CompEnv<T> gen) {
			inputA = gen.inputOfAlice(Boolean.parseBoolean(args[0])); // real input
			gen.flush();
			inputB = gen.inputOfBob(false); // fake input
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
		T inputA;
		T inputB;
		T scResult;
		
		@Override
		public void prepareInput(CompEnv<T> gen) {
			inputA = gen.inputOfAlice(false);
			gen.flush();
			inputB = gen.inputOfBob(Boolean.parseBoolean(args[0]));
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
