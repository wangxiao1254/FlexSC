package oram;

import oram.TestCircuitOramRec.GenRunnable;
import flexsc.Flag;

public class TestCircuitOramRecServer {

	public  static void main(String args[]) throws Exception {
		for(int i = 26; i <=26 ; i+=2) {
			Flag.sw.flush();
			GenRunnable gen = new GenRunnable(12345, i, 3, 32, 8, 6);
			gen.run();
			Flag.sw.print();
			System.out.print("\n");
			//asdasdasdas
		}
	}
}