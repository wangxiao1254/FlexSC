package test.harness;

import java.math.BigInteger;

import org.junit.Assert;

import test.Utils;
import flexsc.CompEnv;
import flexsc.Mode;
import flexsc.Party;



public class TestBigInteger<T>  extends TestHarness<T>{
	public final int LENGTH = 10;
	final int RANGE = LENGTH;
	public abstract class Helper {
		BigInteger intA, intB;
		boolean[] a;
		boolean[] b;
		Mode m;
		public Helper(BigInteger aa, BigInteger bb, Mode m) {
			intA = aa;
			intB = bb;
			this.m = m;

			a = Utils.fromBigInteger(aa, RANGE);
			b = Utils.fromBigInteger(bb, RANGE);
		}
		public abstract T[] secureCompute(T[] Signala, T[] Signalb, CompEnv<T> e) throws Exception;
		public abstract BigInteger plainCompute(BigInteger x, BigInteger y);
	}

	class GenRunnable extends network.Server implements Runnable {
		boolean[] z;
		Helper h;
		GenRunnable (Helper h) {
			this.h = h;
		}

		public void run() {
			try {
				listen(54321);
				@SuppressWarnings("unchecked")
				CompEnv<T> gen = CompEnv.getEnv(h.m, Party.Alice, is, os);
				
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

	class EvaRunnable extends network.Client implements Runnable {
		Helper h;
		EvaRunnable (Helper h) {
			this.h = h;
		}

		public void run() {
			try {
				connect("localhost", 54321);				
				@SuppressWarnings("unchecked")
				CompEnv<T> eva = CompEnv.getEnv(h.m, Party.Bob, is, os);
				
				T [] a = eva.inputOfAlice(new boolean[h.a.length]);
				T [] b = eva.inputOfBob(h.b);
				
				T[] d = h.secureCompute(a, b, eva);
				
				eva.outputToAlice(d);
				os.flush();
				disconnect();
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}

	public void runThreads(Helper h) throws Exception {
		GenRunnable gen = new GenRunnable(h);
		EvaRunnable eva = new EvaRunnable(h);
		Thread tGen = new Thread(gen);
		Thread tEva = new Thread(eva);
		tGen.start(); Thread.sleep(5);
		tEva.start();
		tGen.join();

		/*System.out.println(Utils.toBigInteger(h.a)+" "+Utils.toBigInteger(h.b)+" "+
		h.intA+" "+h.intB+"\n");
		System.out.println(Arrays.toString(h.a));
		System.out.println(Arrays.toString(h.b));
		System.out.println(Arrays.toString( Utils.fromBigInteger(h.plainCompute(h.intA, h.intB),gen.z.length)));
		System.out.println(Arrays.toString(Utils.fromBigInteger(Utils.toBigInteger(gen.z),gen.z.length)));
		*/
		Assert.assertEquals(h.plainCompute(h.intA, h.intB), Utils.toBigInteger(gen.z));
	}
}