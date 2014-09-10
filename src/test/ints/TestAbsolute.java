package test.ints;

import flexsc.CompEnv;
import gc.GCSignal;

import java.util.Random;

import org.junit.Test;

import test.harness.Test_1Input1Output;
import circuits.IntegerLib;

public class TestAbsolute extends Test_1Input1Output<GCSignal>{
	@Test
	public void testAllCases() throws Exception {
		Random rnd = new Random();

		for (int i = 0; i < testCases; i++) {
			runThreads(
					new Helper(rnd.nextInt(1<<30)) {
						public  GCSignal[] secureCompute(GCSignal[] Signala, CompEnv<GCSignal> e) throws Exception {
							return new IntegerLib<GCSignal>(e).absolute(Signala);
						}

						public int plainCompute(int x) {
							return (int)(Math.abs(x));
						}
					});
		}		
	}
}

