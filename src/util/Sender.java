package util;

import network.Server;

import org.apache.commons.cli.ParseException;

import flexsc.Flag;

public class Sender<T> extends network.Client implements Runnable {

	public void run() {
		try {
			connect("localhost", 54321);
			System.out.println("connected");

			double t = System.nanoTime();
			while(true) {
				Server.readBytes(is, 1024*128*1024);
				double t2 = System.nanoTime();
				System.out.println(1024*1024/(t2-t)*1000000000);
				t = t2;
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}



	@SuppressWarnings("rawtypes")
	public static void main(String[] args) throws InstantiationException, IllegalAccessException, ParseException, ClassNotFoundException {
		Sender r = new Sender();
		r.run();
		Flag.sw.print();

	}
}