package test.ints;

import flexsc.CompEnv;
import gc.GCSignal;

import java.util.Random;

import org.junit.Test;

import test.Utils;
import test.harness.Test_2Input1Output;
import circuits.IntegerLib;


public class TestLeq extends Test_2Input1Output<GCSignal>{

	@Test
	public void testAllCases() throws Exception {
		Random rnd = new Random();

		for (int i = 0; i < testCases; i++) {
			runThreads(new Helper(rnd.nextInt()%(1<<30), rnd.nextInt()%(1<<30)) {
				public GCSignal[] secureCompute(GCSignal[] Signala, GCSignal[] Signalb, CompEnv<GCSignal> e) throws Exception {
					return new GCSignal[]{new IntegerLib<GCSignal>(e).leq(Signala ,Signalb)}; }

				public int plainCompute(int x, int y) {
					return (int) Utils.toSignedInt(new boolean[]{(x<=y)});}
			});
		}
		
		for (int i = 0; i < testCases; i++) {
			int a = rnd.nextInt(1<<30);
			runThreads(new Helper(a, a) {
				public GCSignal[] secureCompute(GCSignal[] Signala, GCSignal[] Signalb, CompEnv<GCSignal> e) throws Exception {
					return new GCSignal[]{new IntegerLib<GCSignal>(e).leq(Signala ,Signalb)}; }

				public int plainCompute(int x, int y) {
					return (int) Utils.toSignedInt(new boolean[]{(x<=y)});}
			});
		}
	}
}