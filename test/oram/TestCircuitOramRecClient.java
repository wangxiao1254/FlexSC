package oram;

import oram.TestCircuitOramRec.EvaRunnable;
import flexsc.Flag;

public class TestCircuitOramRecClient {

	public  static void main(String args[]) throws Exception {
		for(int i = 8; i <=24 ; i+=4) {
			Flag.sw.flush();
			EvaRunnable eva = new EvaRunnable("localhost", 12345);
			eva.run();
			Flag.sw.print();
			System.out.print("\n");
		}
	}
}