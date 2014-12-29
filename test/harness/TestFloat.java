package harness;

import org.junit.Assert;

import util.Utils;
import circuits.arithmetic.FloatLib;
import flexsc.CompEnv;
import flexsc.Mode;
import flexsc.PMCompEnv;
import flexsc.Party;

public class TestFloat extends TestHarness {
	public static int widthV = 24, widthP = 8;

	public static abstract class Helper {
		double a, b;

		public Helper(double a, double b) {
			this.b = b;
			this.a = a;
		}

		public abstract<T> T[] secureCompute(T[] a, T[] b, FloatLib<T> env)
				throws Exception;

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
				CompEnv<T> gen = CompEnv.getEnv(m, Party.Alice, is, os);

				FloatLib<T> lib = new FloatLib<T>(gen, widthV, widthP);
				T[] f1 = lib.inputOfAlice(h.a);
				T[] f2 = lib.inputOfBob(0);
				T[] re = h.secureCompute(f1, f2, lib);
				Assert.assertTrue(re.length == widthP + widthV + 1);
				z = Utils.toFloat(gen.outputToAlice(re), widthV, widthP);

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

				FloatLib<T> lib = new FloatLib<T>(env, widthV, widthP);
				T[] f1 = lib.inputOfAlice(0);
				T[] f2 = lib.inputOfBob(h.b);
				if (m == Mode.COUNT) {
					((PMCompEnv) env).statistic.flush();
				}

				T[] re = h.secureCompute(f1, f2, lib);

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


	static public <T>void runThreads(Helper h) throws Exception {
		GenRunnable<T> gen = new GenRunnable<T>(h);
		EvaRunnable<T> env = new EvaRunnable<T>(h);

		Thread tGen = new Thread(gen);
		Thread tEva = new Thread(env);
		tGen.start();
		Thread.sleep(1);
		tEva.start();
		tGen.join();
		if (TestHarness.m == Mode.COUNT) {
			System.out.println(env.andgates + " " + env.encs);
		} else {
			double error = 0;
			if (gen.z != 0)
				error = Math.abs((h.plainCompute(h.a, h.b) - gen.z) / gen.z);
			else
				error = Math.abs((h.plainCompute(h.a, h.b) - gen.z));

			if (Math.abs((h.plainCompute(h.a, h.b) - gen.z) / gen.z) > 1E-3)
				System.out.print(error + " " + gen.z + " "
						+ h.plainCompute(h.a, h.b) + " " + h.a + " " + h.b
						+ "\n");
			Assert.assertTrue(error <= 1E-3);
		}
	}
}