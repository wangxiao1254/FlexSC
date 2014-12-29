package harness;

import java.math.BigInteger;
import java.util.Arrays;

import org.junit.Assert;

import util.Utils;
import flexsc.CompEnv;
import flexsc.Mode;
import flexsc.PMCompEnv;
import flexsc.Party;



public class TestBigInteger extends TestHarness{
	public static final int LENGTH = 799;
	final static int RANGE = LENGTH;
	public static abstract class Helper {
		BigInteger intA, intB;
		boolean[] a;
		boolean[] b;
		public Helper(BigInteger aa, BigInteger bb) {
			intA = aa;
			intB = bb;

			a = Utils.fromBigInteger(aa, RANGE);
			b = Utils.fromBigInteger(bb, RANGE);
		}
		public abstract <T>T[] secureCompute(T[] Signala, T[] Signalb, CompEnv<T> e) throws Exception;
		public abstract BigInteger plainCompute(BigInteger x, BigInteger y);
	}

	public static class GenRunnable<T> extends network.Server implements Runnable {
		boolean[] z;
		Helper h;
		GenRunnable (Helper h) {
			this.h = h;
		}

		public void run() {
			try {
				listen(54321);
				@SuppressWarnings("unchecked")
				CompEnv<T> gen = CompEnv.getEnv(m, Party.Alice, is, os);
				
				T [] a = gen.inputOfAlice(h.a);
				T[]b = gen.inputOfBob(new boolean[h.b.length]);
				T[] d = h.secureCompute(a, b, gen);
				os.flush();
		          
				z = gen.outputToAlice(d);
				disconnect();
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}

	public static class EvaRunnable<T> extends network.Client implements Runnable {
		Helper h;
		public double andgates;
		public double encs;
		EvaRunnable (Helper h) {
			this.h = h;
		}

		public void run() {
			try {
				connect("localhost", 54321);				
				@SuppressWarnings("unchecked")
				CompEnv<T> env = CompEnv.getEnv(m, Party.Bob, is, os);
				
				T [] a = env.inputOfAlice(new boolean[h.a.length]);
				T [] b = env.inputOfBob(h.b);
				if (m == Mode.COUNT) {
					((PMCompEnv) env).statistic.flush();
				}

				T[] d = h.secureCompute(a, b, env);
				if (m == Mode.COUNT) {
					((PMCompEnv) env).statistic.finalize();
					andgates = ((PMCompEnv) env).statistic.andGate;
					encs = ((PMCompEnv) env).statistic.NumEncAlice;
				}
				
				env.outputToAlice(d);
				os.flush();
				disconnect();
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}


	static public <T>void runThreads(Helper h) throws Exception {
		GenRunnable<T> gen = new GenRunnable<T>(h);
		EvaRunnable<T> eva = new EvaRunnable<T>(h);

		Thread tGen = new Thread(gen);
		Thread tEva = new Thread(eva);
		tGen.start(); Thread.sleep(5);
		tEva.start();
		tGen.join();
		if(m == Mode.COUNT)
			System.out.println(eva.andgates);
//		System.out.println(Utils.toBigInteger(h.a)+" "+Utils.toBigInteger(h.b)+" "+
//		h.intA+" "+h.intB+"\n");
//		System.out.println(Arrays.toString(h.a));
//		System.out.println(Arrays.toString(h.b));
		System.out.println(Arrays.toString( Utils.fromBigInteger(h.plainCompute(h.intA, h.intB),gen.z.length)));
		System.out.println(Arrays.toString(Utils.fromBigInteger(Utils.toBigInteger(gen.z),gen.z.length)));
		
		Assert.assertEquals(h.plainCompute(h.intA, h.intB), Utils.toBigInteger(gen.z));
	}
}