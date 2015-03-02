package harness;

import org.junit.Assert;

import util.EvaRunnable;
import util.GenRunnable;
import util.Utils;
import flexsc.CompEnv;
import flexsc.Mode;
import flexsc.PMCompEnv;
import gc.BadLabelException;

public class TestFixedPoint extends TestHarness {
	public static final int width = 40, offset = 20;

	public static abstract class Helper {
		double a, b;

		public Helper(double a, double b) {
			this.b = b;
			this.a = a;
		}

		public abstract <T>T[] secureCompute(T[] a, T[] b, int offset, CompEnv<T> env);

		public abstract double plainCompute(double a, double b);
	}

	public static class GenRunnableTestFixedPoint<T> extends GenRunnable<T> {
		public double andgates;
		public double encs;
		public double error;
		
		double z;
		Helper h;
		public GenRunnableTestFixedPoint(Helper h) {
			this.h = h;
		}
		
		T[] a;
		T[] b;
		T[] d;
		@Override
		public void prepareInput(CompEnv<T> gen) {
			a = gen.inputOfAlice(Utils.fromFixPoint(h.a, width, offset));
			b = gen.inputOfBob(Utils.fromFixPoint(0, width, offset));
		}

		@Override
		public void secureCompute(CompEnv<T> gen) {
			d = h.secureCompute(a, b, offset, gen);
		}

		@Override
		public void prepareOutput(CompEnv<T> gen) throws BadLabelException {
			z = Utils.toFixPoint(gen.outputToAlice(d), offset);
			if (m == Mode.COUNT) {
					((PMCompEnv) gen).statistic.finalize();
					andgates = ((PMCompEnv) gen).statistic.andGate;
					encs = ((PMCompEnv) gen).statistic.NumEncAlice;
				System.out.println(andgates + " " + encs);
			} else {
				if (Math.abs(h.plainCompute(h.a, h.b) - z) > error)
					System.out.print(Math.abs(h.plainCompute(h.a, h.b) - z)
							+ " " + z + " " + h.plainCompute(h.a, h.b) + " "
							+ h.a + " " + h.b + "\n");
				Assert.assertTrue(Math.abs(h.plainCompute(h.a, h.b) - z) < error);
			}
		}
	}

	public static class EvaRunnableTestFixedPoint<T> extends EvaRunnable<T> {		
		EvaRunnableTestFixedPoint(Helper h) {
			this.h = h;
		}

		Helper h;
		T[] a;
		T[] b;
		T[] d;

		@Override
		public void prepareInput(CompEnv<T> env) {
			a = env.inputOfAlice(Utils.fromFixPoint(0, width, offset));
			b = env.inputOfBob(Utils.fromFixPoint(h.b, width, offset));
		}

		@Override
		public void secureCompute(CompEnv<T> env) {
			d = h.secureCompute(a, b, offset, env);
		}

		@Override
		public void prepareOutput(CompEnv<T> env) throws BadLabelException {
			env.outputToAlice(d);
		}
	}
	
	static public <T>void runThreads(Helper h, double error) throws Exception {
		GenRunnableTestFixedPoint<T> gen = new GenRunnableTestFixedPoint<T>(h);
		gen.setParameter(m, 54321);
		gen.error = error;
		EvaRunnable<T> env = new EvaRunnableTestFixedPoint<T>(h);
		env.setParameter(m, "localhost", 54321);
		Thread tGen = new Thread(gen);
		Thread tEva = new Thread(env);
		tGen.start();
		Thread.sleep(5);
		tEva.start();
		tGen.join();
		tEva.join();
	}
	
	static public <T>void runThreads(Helper h) throws Exception {
		runThreads(h, 1e-3);
	}

}