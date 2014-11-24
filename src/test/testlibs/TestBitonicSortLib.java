package test.testlibs;

import java.util.Arrays;
import java.util.Random;

import org.junit.Test;

import test.harness.TestHarness;
import test.harness.TestSortHarness;
import test.harness.TestSortHarness.Helper;
import circuits.BitonicSortLib;
import flexsc.CompEnv;

public class TestBitonicSortLib extends TestHarness {

	@Test
	public void testAllCases() throws Exception {
		Random rnd = new Random();
		for (int i = 0; i < 10; i++) {
			int[] a = new int[1000];
			for (int j = 0; j < a.length; ++j)
				a[j] = rnd.nextInt() % (1 << 30);

			TestSortHarness.runThreads(new Helper(a) {
				public <T>T[][] secureCompute(T[][] Signala,
						CompEnv<T> e) throws Exception {
					BitonicSortLib<T> lib = new BitonicSortLib<T>(e);
					lib.sort(Signala, lib.SIGNAL_ONE);
					return Signala;
				}

				@Override
				public int[] plainCompute(int[] intA) {
					Arrays.sort(intA);
					return intA;
				}
			});
		}
	}

}