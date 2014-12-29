package harness;

import org.junit.Assert;

import util.Utils;
import flexsc.CompEnv;
import flexsc.Mode;
import flexsc.PMCompEnv;
import flexsc.Party;

public class TestFixedPoint extends TestHarness {
	public static final int width = 40, offset = 20;

	public static abstract class Helper {
		double a, b;

		public Helper(double a, double b) {
			this.b = b;
			this.a = a;
		}

		public abstract <T>T[] secureCompute(T[] a, T[] b, int offset,
				CompEnv<T> env) throws Exception;

		public abstract double plainCompute(double a, double b);
	}

	static public class GenRunnable<T> extends network.Server implements Runnable {
		Helper h;
		double z;

		GenRunnable(Helper h) {
			this.h = h;
		}

		public void run() {
			try {
				listen(54321);
				@SuppressWarnings("unchecked")
				CompEnv<T> env = CompEnv.getEnv(m, Party.Alice, is, os);

				T[] f1 = env.inputOfAlice(Utils
						.fromFixPoint(h.a, width, offset));
				T[] f2 = env.inputOfBob(Utils.fromFixPoint(0, width, offset));
				T[] re = h.secureCompute(f1, f2, offset, env);
				z = Utils.toFixPoint(env.outputToAlice(re), offset);

				disconnect();
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}

	static public class EvaRunnable<T> extends network.Client implements Runnable {
		Helper h;
		public double andgates;
		public double encs;

		EvaRunnable(Helper h) {
			this.h = h;
		}

		public void run() {
			try {
				connect("localhost", 54321);
				@SuppressWarnings("unchecked")
				CompEnv<T> env = CompEnv.getEnv(m, Party.Bob, is, os);

				T[] f1 = env.inputOfAlice(Utils.fromFixPoint(0, width, offset));
				T[] f2 = env.inputOfBob(Utils.fromFixPoint(h.b, width, offset));

				if (m == Mode.COUNT) {
					((PMCompEnv) env).statistic.flush();
					;
				}
				T[] re = h.secureCompute(f1, f2, offset, env);
				if (m == Mode.COUNT) {
					((PMCompEnv) env).statistic.finalize();
					andgates = ((PMCompEnv) env).statistic.andGate;
					encs = ((PMCompEnv) env).statistic.NumEncAlice;
				}

				env.outputToAlice(re);

				disconnect();
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}

	static public <T>void runThreads(Helper h, double error) throws Exception {
		GenRunnable<T> gen = new GenRunnable<T>(h);
		EvaRunnable<T> env = new EvaRunnable<T>(h);
		Thread tGen = new Thread(gen);
		Thread tEva = new Thread(env);
		tGen.start();
		Thread.sleep(1);
		tEva.start();
		tGen.join();

		if (m == Mode.COUNT) {
			System.out.println(env.andgates + " " + env.encs);
		} else {
			if (Math.abs(h.plainCompute(h.a, h.b) - gen.z) > error)
				System.out.print(Math.abs(h.plainCompute(h.a, h.b) - gen.z)
						+ " " + gen.z + " " + h.plainCompute(h.a, h.b) + " "
						+ h.a + " " + h.b + "\n");
			Assert.assertTrue(Math.abs(h.plainCompute(h.a, h.b) - gen.z) < error);
		}
	}

	public static void runThreads(Helper h) throws Exception {
		runThreads(h, 0.005);
	}

}