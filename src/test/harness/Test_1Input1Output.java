package test.harness;

import org.junit.Assert;

import test.Utils;
import flexsc.CompEnv;
import flexsc.Mode;
import flexsc.Party;

public class Test_1Input1Output<T> extends TestHarness<T>{
	public abstract class Helper {
		int intA;
		boolean[] a;
		Mode m;
		public Helper(int aa, Mode m) {
			this.m = m;
			intA = aa;
			a = Utils.fromInt(aa, 32);
		}
		
		public abstract T[] secureCompute(T[] Signala, CompEnv<T> e) throws Exception;
		public abstract int plainCompute(int x);
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
				T[] a = gen.inputOfBob(new boolean[32]);
				T[] d = h.secureCompute(a, gen);
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
				T[] a = eva.inputOfBob(h.a);
				T[] d = h.secureCompute(a, eva);
				eva.outputToAlice(d);
				os.flush();
				disconnect();
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}

	public void runThreads(Helper helper) throws Exception {
		GenRunnable gen = new GenRunnable(helper);
		EvaRunnable eva = new EvaRunnable(helper);
		Thread tGen = new Thread(gen);
		Thread tEva = new Thread(eva);
		tGen.start(); Thread.sleep(5);
		tEva.start();
		tGen.join();

		Assert.assertEquals(helper.plainCompute(helper.intA), Utils.toInt(gen.z));
	}
}