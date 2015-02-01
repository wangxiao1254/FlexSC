package util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Scanner;

import org.apache.commons.cli.ParseException;

import flexsc.CompEnv;
import flexsc.Flag;
import flexsc.Mode;
import flexsc.Party;

public abstract  class EvaRunnable<T> extends network.Client implements Runnable {
	public abstract void prepareInput(CompEnv<T> gen);
	public abstract void secureCompute(CompEnv<T> gen);
	public abstract void prepareOutput(CompEnv<T> gen);
	Mode m;
	int port;
	String host;
	protected String[] args;

	public void setParameter(Mode m, String host, int port, String[] args){
		this.m = m;
		this.port = port;
		this.host = host;
		this.args = args;
	}

	public void setParameter(Mode m, String host, int port){
		this.m = m;
		this.port = port;
		this.host = host;
	}
	
	public void run() {
		try {
			System.out.println("connecting");
			connect(host, port);
			System.out.println("connected");

			double s = System.nanoTime();
			@SuppressWarnings("unchecked")
			CompEnv<T> env = CompEnv.getEnv(m, Party.Bob, is, os);
			
			Flag.sw.startTotal();
			prepareInput(env);
			os.flush();
			secureCompute(env);
			os.flush();
			prepareOutput(env);
			os.flush();
			Flag.sw.stopTotal();
			System.out.println("Gen running time:"+(System.nanoTime()-s)/1e9);
			System.out.println("Number Of Gates:"+Flag.sw.ands);
			disconnect();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	@SuppressWarnings("rawtypes")
	public static void main(String[] args) throws InstantiationException, IllegalAccessException, ParseException, ClassNotFoundException {
		File file = new File("Config.conf");
		Scanner scanner;
		String host = null;
		int port = 0;
		Mode mode = null;
		
		try {
			scanner = new Scanner(file);
			while(scanner.hasNextLine()) {
				String a = scanner.nextLine();
				String[] content = a.split(":");
				if(content.length == 2) {
					if(content[0].equals("Host"))
						host = content[1].replace(" ", "");
					else if(content[0].equals("Port"))
						port = new Integer(content[1].replace(" ", ""));
					else if(content[0].equals("Mode"))
						mode = Mode.getMode(content[1].replace(" ", ""));
					else{}
				}
			}
			scanner.close();			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Class<?> clazz = Class.forName(args[0]+"$Evaluator");
		EvaRunnable run = (EvaRunnable) clazz.newInstance();
		run.setParameter(mode, host, port, Arrays.copyOfRange(args, 1, args.length));
		run.run();
//		Flag.sw.print();
		if(Flag.countIO)
			run.printStatistic();
	}
}