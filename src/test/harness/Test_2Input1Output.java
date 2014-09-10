package test.harness;

import org.junit.Assert;

import test.Utils;
import flexsc.CompEnv;
import flexsc.Party;


public class Test_2Input1Output<T>  extends TestHarness<T>{
	
	public abstract class Helper {
		int intA, intB;
		boolean[] a;
		boolean[] b;
		public Helper(int aa, int bb) {
			intA = aa;
			intB = bb;

			a = Utils.fromInt(aa, 32);
			b = Utils.fromInt(bb, 32);
		}
		public abstract T[] secureCompute(T[] Signala, T[] Signalb, CompEnv<T> e) throws Exception;
		public abstract int plainCompute(int x, int y);
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
				CompEnv<T> gen = CompEnv.getEnv(m, Party.Alice, is, os);						
				
				T[] a = gen.inputOfAlice(h.a);
				T[] b = gen.inputOfBob(new boolean[32]);

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
				CompEnv<T> eva = CompEnv.getEnv(m, Party.Bob, is, os);
				
				T[] a = eva.inputOfAlice(new boolean[32]);
				T[] b = eva.inputOfBob(h.b);

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
		tEva.join();

		//System.out.println(Arrays.toString(gen.z));
		Assert.assertEquals(h.plainCompute(h.intA, h.intB), Utils.toSignedInt(gen.z));
	}

	

}