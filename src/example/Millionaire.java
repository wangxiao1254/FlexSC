package example;

import java.util.Scanner;

import util.EvaRunnable;
import util.GenRunnable;
import util.Utils;
import circuits.arithmetic.IntegerLib;
import flexsc.CompEnv;

public class Millionaire {
	public static class Generator<T> extends GenRunnable<T> {

		T[] inputA;
		T[] inputB;
		T scResult;
		
		@Override
		public void prepareInput(CompEnv<T> gen) {
			Scanner scanner = new Scanner(System.in);
			inputA = gen.inputOfAlice(Utils.fromInt(scanner.nextInt(), 32));
			gen.flush();
			inputB = gen.inputOfBob(new boolean[32]);
			scanner.close();
		}
		
		@Override
		public void secureCompute(CompEnv<T> gen) {
			scResult = new IntegerLib<T>(gen).geq(inputA, inputB);
			
		}
		@Override
		public void prepareOutput(CompEnv<T> gen) {
			System.out.println(gen.outputToAlice(scResult));
		}
	}
	
	public static class Evaluator<T> extends EvaRunnable<T> {
		T[] inputA;
		T[] inputB;
		T scResult;
		
		@Override
		public void prepareInput(CompEnv<T> gen) {
			Scanner scanner = new Scanner(System.in);
			inputA = gen.inputOfAlice(new boolean[32]);
			gen.flush();
			inputB = gen.inputOfBob(Utils.fromInt(scanner.nextInt(), 32));
			scanner.close();
		}
		
		@Override
		public void secureCompute(CompEnv<T> gen) {
			scResult = new IntegerLib<T>(gen).geq(inputA, inputB);
		}
		
		@Override
		public void prepareOutput(CompEnv<T> gen) {
			gen.outputToAlice(scResult);
		}
	}
}
