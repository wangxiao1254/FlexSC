package harness;

import org.junit.Test;


public class TestSend {

	
	public static class GenRunnable extends network.Server implements Runnable {

		public void run() {
			try {
				listen(54321);

				byte[] data = new byte[1024*1024];
				double t1 = System.nanoTime();
				for(int i = 1; i < 100000; ++i) {
					writeByte(data, 1024*1024);
					flush();
					System.out.println("a"+(i)/((System.nanoTime()-t1)/1000000000.0));
				}
				
				os.flush();
				disconnect();
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}

	public static class EvaRunnable extends network.Client implements Runnable {
		public EvaRunnable(String s) {
			this.s = s;
		}
		
		public EvaRunnable() {
			this.s = "localhost";
		}

		
		String s;
		public void run() {
			try {
				connect(s, 54321);				
			
				
				byte[] data = new byte[1024*1024];
				double t1 = System.nanoTime();
				for(int i = 1; i < 100000; ++i) {
					readBytes(1024*1024);
					System.out.println("a"+(i)/((System.nanoTime()-t1)/1000000000.0));
				}
				
				disconnect();
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}
	@Test
	public <T>void runThreads() throws Exception {
		GenRunnable gen = new GenRunnable();
		EvaRunnable env = new EvaRunnable();
		Thread tGen = new Thread(gen);
		Thread tEva = new Thread(env);
		tGen.start();
		Thread.sleep(5);
		tEva.start();
		tGen.join();
		tEva.join();
	}
	
	public static void main(String args[]) throws Exception {
		 if(args.length == 0){
			 	GenRunnable gen = new GenRunnable();
				EvaRunnable env = new EvaRunnable();
				Thread tGen = new Thread(gen);
				Thread tEva = new Thread(env);
				tGen.start();
				Thread.sleep(5);
				tEva.start();
				tGen.join();
				tEva.join();
		 }
		 if(new Integer(args[0]) == 0)
			 new GenRunnable().run();
		 else new EvaRunnable(args[1]).run();
	}
}
