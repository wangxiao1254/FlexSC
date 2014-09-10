package test.ints;

import flexsc.CompEnv;
import gc.GCSignal;

import java.util.Random;

import org.junit.Test;

import test.harness.Test_2Input1Output;
import circuits.IntegerLib;


public class TestRightPrivaeShift extends Test_2Input1Output<GCSignal>{

	@Test
	public void testAllCases() throws Exception {
		Random rnd = new Random();

		for (int i = 0; i < testCases; i++) {
			int shift = rnd.nextInt(1<<5);
			runThreads(new Helper(rnd.nextInt(1<<30), shift) {
				public GCSignal[] secureCompute(GCSignal[] Signala, GCSignal[] Signalb, CompEnv<GCSignal> e) throws Exception {
					return new IntegerLib<GCSignal>(e).rightPrivateShift(Signala ,Signalb);}

				public int plainCompute(int x, int y) {
					return x>>y;}
			});
		}		
	}
}