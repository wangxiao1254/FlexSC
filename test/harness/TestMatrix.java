package harness;

import java.util.Random;

import org.junit.Assert;

import circuits.arithmetic.DenseMatrixLib;
import circuits.arithmetic.FixedPointLib;
import circuits.arithmetic.FloatLib;
import flexsc.CompEnv;
import flexsc.Mode;
import flexsc.PMCompEnv;
import flexsc.Party;


public class TestMatrix extends TestHarness {
	public static final int len = 40;
	public static final int offset = 20;
	public static final int VLength = 24;
	public static final int PLength = 8;
	public static final boolean testFixedPoint = true;

	public static abstract class Helper {
		double[][] a, b;

		public Helper(double[][] a, double[][] b) {
			this.b = b;
			this.a = a;
		}

		public abstract<T> T[][][] secureCompute(T[][][] a, T[][][] b,
				DenseMatrixLib<T> lib) throws Exception;

		public abstract double[][] plainCompute(double[][] a, double[][] b);
	}

	static Random rng = new Random();

	public static double[][] randomMatrix(int n, int m, double s) {
		double[][] d1 = new double[n][m];
		for (int k = 0; k < d1.length; ++k)
			for (int j = 0; j < d1[0].length; ++j)
				d1[k][j] = rng.nextInt(1000) % 100.0;
		return d1;
	}
	
	public static double[][] randomMatrix(int n, int m) {
		return randomMatrix( n,  m, 1);
	}

	public static void PrintMatrix(double[][] result) {
		System.out.print("[\n");
		for (int i = 0; i < result.length; ++i) {
			for (int j = 0; j < result[0].length; ++j)
				System.out.print(result[i][j] + " ");
			System.out.print(";\n");
		}
		System.out.print("]\n");
	}

	public static class GenRunnable<T> extends network.Server implements Runnable {
		Helper h;
		double[][] z;
		DenseMatrixLib<T> lib;

		GenRunnable(Helper h) {
			this.h = h;
		}

		public void run() {
			try {
				listen(54321);
				@SuppressWarnings("unchecked")
				CompEnv<T> gen = CompEnv.getEnv(m, Party.Alice, is, os);

				// PrintMatrix(h.a);
				if (testFixedPoint)
					lib = new DenseMatrixLib<T>(gen, new FixedPointLib<T>(gen, len, offset));
				else
					lib = new DenseMatrixLib<T>(gen, new FloatLib<T>(gen, VLength,PLength));

				T[][][] fgc1 = gen.newTArray(h.a.length, h.a[0].length, 1);
				T[][][] fgc2 = gen.newTArray(h.b.length, h.b[0].length, 1);
				for (int i = 0; i < h.a.length; ++i)
					for (int j = 0; j < h.a[0].length; ++j) 
							fgc1[i][j] = lib.lib.inputOfAlice(h.a[i][j]);
					
				for (int i = 0; i < h.b.length; ++i)
					for (int j = 0; j < h.b[0].length; ++j)
							fgc2[i][j] = lib.lib.inputOfAlice(h.b[i][j]);
					

				T[][][] re = h.secureCompute(fgc1, fgc2, lib);
				z = new double[re.length][re[0].length];
				for (int i = 0; i < re.length; ++i)
					for (int j = 0; j < re[0].length; ++j)
							z[i][j] = lib.lib.outputToAlice(re[i][j]);

				disconnect();
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}

	public static class EvaRunnable<T> extends network.Client implements Runnable {
		Helper h;
		DenseMatrixLib<T> lib;
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

				if (testFixedPoint)
					lib = new DenseMatrixLib<T>(env, new FixedPointLib<T>(env, len, offset));
				else
					lib = new DenseMatrixLib<T>(env, new FloatLib<T>(env, VLength,PLength));
							


				T[][][] fgc1 = env.newTArray(h.a.length, h.a[0].length, 1);
				T[][][] fgc2 = env.newTArray(h.b.length, h.b[0].length, 1);
				for (int i = 0; i < h.a.length; ++i)
					for (int j = 0; j < h.a[0].length; ++j)
							fgc1[i][j] = lib.lib.inputOfAlice(h.a[i][j]);
					
				for (int i = 0; i < h.b.length; ++i)
					for (int j = 0; j < h.b[0].length; ++j)
						fgc2[i][j] = lib.lib.inputOfAlice(h.b[i][j]);

				if (m == Mode.COUNT) {
					((PMCompEnv) env).statistic.flush();
				}

				T[][][] re = h.secureCompute(fgc1, fgc2, lib);
				if (m == Mode.COUNT) {
					((PMCompEnv) env).statistic.finalize();
					andgates = ((PMCompEnv) env).statistic.andGate;
					encs = ((PMCompEnv) env).statistic.NumEncAlice;
				}

				for (int i = 0; i < re.length; ++i)
					for (int j = 0; j < re[0].length; ++j)
						env.outputToAlice(re[i][j]);

				disconnect();
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}

	public static <T>void runThreads(Helper h, double errorLim) throws Exception {
		GenRunnable<T> gen = new GenRunnable<T>(h);
		EvaRunnable<T> env = new EvaRunnable<T>(h);

		Thread tGen = new Thread(gen);
		Thread tEva = new Thread(env);
		tGen.start();
		Thread.sleep(1);
		tEva.start();
		tGen.join();

		double[][] result = h.plainCompute(h.a, h.b);

		 PrintMatrix(result);
		 PrintMatrix(gen.z);
		if (m == Mode.COUNT) {
			System.out.println(env.andgates + " " + env.encs);
		} else {

			for (int i = 0; i < result.length; ++i)
				for (int j = 0; j < result[0].length; ++j) {
					double error = 0;
					if (Math.abs(result[i][j]) > 0.1)
						error = Math.abs((result[i][j] - gen.z[i][j])
								/ gen.z[i][j]);
					else
						error = Math.abs((result[i][j] - gen.z[i][j]));

					if (error > errorLim)
						System.out.print(error + " " + gen.z[i][j] + " "
								+ result[i][j] + "(" + i + "," + j + ")\n");
					Assert.assertTrue(error < errorLim);
				}
		}
	}
	public static <T>void runThreads(Helper h) throws Exception {
		runThreads(h, 0.01);
	}
}