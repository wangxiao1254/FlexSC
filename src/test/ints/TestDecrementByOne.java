package test.ints;

import flexsc.CompEnv;
import flexsc.Mode;
import gc.GCSignal;

import java.util.Random;

import org.junit.Test;

import test.harness.Test_1Input1Output;
import circuits.IntegerLib;

public class TestDecrementByOne extends Test_1Input1Output<GCSignal> {

	@Test
	public void testAllCases() throws Exception {
		Random rnd = new Random();

		for (int i = 0; i < testCases; i++) {
			runThreads(new Helper(rnd.nextInt(1 << 30), Mode.REAL) {
				public GCSignal[] secureCompute(GCSignal[] Signala, CompEnv<GCSignal> e)
						throws Exception {
					return new IntegerLib<GCSignal>(e).decrementByOne(Signala);
				}

				public int plainCompute(int x) {
					return x - 1;
				}
			});
		}
	}
}