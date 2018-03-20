package oram;

import oram.TestCircuitOramRec.GenRunnable;
import flexsc.Flag;

public class TestCircuitOramRecServer {

	public  static void main(String args[]) throws Exception {
		for(int i = 12; i <=20 ; i+=2) {
			Flag.sw.flush();
			GenRunnable gen = new GenRunnable(12345, i, 3, 32, 8, 6);
			gen.run();
			Flag.sw.print();
			System.out.print("\n");
		}
	}
}
