package util;

import org.apache.commons.cli.ParseException;

public class Reciever<T> extends network.Server implements Runnable {

	public void run() {
		try {
			listen(54321);

			while(true) {
				byte[] res = new byte[1024*128];//1024*1024bits
				os.write(res);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	
	@SuppressWarnings("rawtypes")
	public static void main(String[] args) throws ParseException, ClassNotFoundException, InstantiationException, IllegalAccessException { 
		Reciever r = new Reciever();
		r.run();
	}
}
