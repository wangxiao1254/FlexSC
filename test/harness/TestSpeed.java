package harness;


import java.math.BigInteger;

import org.junit.Test;

import util.Utils;
import circuits.arithmetic.IntegerLib;
import flexsc.CompEnv;
import flexsc.Flag;
import flexsc.Mode;
import flexsc.Party;

public class TestSpeed extends TestHarness {

	public <T>T[] secureCompute(T[] a, T[] b, CompEnv<T> env) {
		IntegerLib<T> lib = new IntegerLib<T>(env);
		T[] res = null;

		Flag.sw.ands = 0;

		double t1 = System.nanoTime();
		for(int i = 0; i<10000000; ++i) {
			a = lib.and(a, b);
			double t2 = System.nanoTime();
			double t = (t2-t1)/1000000000.0;
			System.out.println(i+" "+t +"\t"+ (env.numOfAnds/t)+" "+env.getParty());
		}
		
		return res;
	}
	int LEN = 1024*50;
	class GenRunnable<T> extends network.Server implements Runnable {
		boolean[] z;

		public void run() {
			try {
				listen(5001);
				@SuppressWarnings("unchecked")
				CompEnv<T> gen = CompEnv.getEnv(Mode.REAL, Party.Alice, this);

				T[] a = gen.inputOfAlice(Utils.fromBigInteger(BigInteger.ONE, LEN));
				T[] b = gen.inputOfBob(new boolean[LEN]);

				T[] d = secureCompute(a, b, gen);
				os.flush();

				disconnect();

			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}

	class EvaRunnable<T> extends network.Client implements Runnable {
		public EvaRunnable(String s) {
			this.s = s;
		}
		
		public EvaRunnable() {
			this.s = "localhost";
		}

		
		String s;
		public void run() {
			try {
				connect(s, 5001);
				@SuppressWarnings("unchecked")
				CompEnv<T> env = CompEnv.getEnv(Mode.REAL, Party.Bob, this);

				T[] a = env.inputOfAlice(new boolean[LEN]);
				T[] b = env.inputOfBob(Utils.fromBigInteger(BigInteger.ONE, LEN));
				
				T[] d = secureCompute(a, b, env);
				
				os.flush();


				disconnect();
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}

	@Test
	public <T>void runThreads() throws Exception {
		GenRunnable<T> gen = new GenRunnable<T>();
		EvaRunnable<T> env = new EvaRunnable<T>();
		Thread tGen = new Thread(gen);
		Thread tEva = new Thread(env);
		tGen.start();
		Thread.sleep(5);
		tEva.start();
		tGen.join();
		tEva.join();
	}
	
	public static void main(String args[]) throws Exception {
		 TestSpeed test = new TestSpeed();
		 if(args.length == 0){
			 	GenRunnable gen = test.new GenRunnable();
				EvaRunnable env = test.new EvaRunnable();
				Thread tGen = new Thread(gen);
				Thread tEva = new Thread(env);
				tGen.start();
				Thread.sleep(5);
				tEva.start();
				tGen.join();
				tEva.join();
		 }
		 if(new Integer(args[0]) == 0)
			 test.new GenRunnable().run();
		 else test.new EvaRunnable(args[1]).run();
	}
}
