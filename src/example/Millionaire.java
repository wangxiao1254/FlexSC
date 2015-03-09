package example;

import util.EvaRunnable;
import util.GenRunnable;
import util.Utils;
import circuits.arithmetic.IntegerLib;
import flexsc.CompEnv;
import gc.BadLabelException;

public class Millionaire {
	
	
	static public<T> T compute(CompEnv<T> gen, T[] inputA, T[] inputB){
		return new IntegerLib<T>(gen).geq(inputA, inputB);
	}
	
	public static class Generator<T> extends GenRunnable<T> {

		T[] inputA;
		T[] inputB;
		T scResult;
		
		@Override
		public void prepareInput(CompEnv<T> gen) {
			inputA = gen.inputOfAlice(Utils.fromInt(new Integer(args[0]), 32));
			gen.flush();
			inputB = gen.inputOfBob(new boolean[32]);
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
		T scResult;
		
		@Override
		public void prepareInput(CompEnv<T> gen) {
			inputA = gen.inputOfAlice(new boolean[32]);
			gen.flush();
			inputB = gen.inputOfBob(Utils.fromInt(new Integer(args[0]), 32));
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
