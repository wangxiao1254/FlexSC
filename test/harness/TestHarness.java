package harness;

import flexsc.Mode;

public class TestHarness {
	static public int testCases;
	static public Mode m = Mode.COUNT;
	public TestHarness() {
		if (m == Mode.COUNT) {
			testCases = 1;
		}
		else if (m == Mode.REAL || m == Mode.OPT) {
			testCases = 100;
		}
		else if (m == Mode.VERIFY)
			testCases = 1000;
	}
}