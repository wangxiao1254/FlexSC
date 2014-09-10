package test.ints;

import flexsc.CompEnv;
import gc.GCSignal;

import org.junit.Test;

import test.harness.Test_2Input1Output;
import circuits.CircuitLib;

public class TestMux extends Test_2Input1Output<GCSignal>{

	@Test
	public void testAllCases() throws Exception {

		for (int i = 0; i < testCases; i++) {
			runThreads(new Helper(0b1100, 0b1010) { // This particular pair of inputs exhausts 4 possible inputs, excluding selection signal 
				public GCSignal[] secureCompute(GCSignal[] a, GCSignal[] b, CompEnv<GCSignal> e) throws Exception {
					CircuitLib<GCSignal> lib = new CircuitLib<GCSignal>(e);
					return lib.mux(a, b, lib.SIGNAL_ONE);}

				public int plainCompute(int x, int y) {
					return y;}
			});
		}
		
		for (int i = 0; i < testCases; i++) {
			runThreads(new Helper(0b1100, 0b1010) { // This particular pair of inputs exhausts 4 possible inputs, excluding selection signal
				public GCSignal[] secureCompute(GCSignal[] a, GCSignal[] b, CompEnv<GCSignal> e) throws Exception {
					CircuitLib<GCSignal> lib = new CircuitLib<GCSignal>(e);
					return lib.mux(a, b, lib.SIGNAL_ZERO);}

				public int plainCompute(int x, int y) {
					return x;}
			});
		}
	}
}