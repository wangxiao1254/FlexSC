package util;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import flexsc.CompEnv;
import flexsc.Mode;
import flexsc.Party;

public abstract  class EvaRunnable<T> extends network.Client implements Runnable {
	public abstract void prepareInput(CompEnv<T> gen);
	public abstract void secureCompute(CompEnv<T> gen);
	public abstract void prepareOutput(CompEnv<T> gen);
	Mode m;
	int port;
	String host;

	public void setParameter(Mode m, String host, int port){
		this.m = m;
		this.port = port;
		this.host = host;
	}

	public void run() {
		try {
			connect(host, port);
			System.out.println("connected");

			@SuppressWarnings("unchecked")
			CompEnv<T> env = CompEnv.getEnv(m, Party.Bob, is, os);
			prepareInput(env);
			os.flush();
			secureCompute(env);
			os.flush();
			prepareOutput(env);
			os.flush();

			disconnect();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public static void printUtility() {
		  System.err
          .println("Usage: java -cp bin:lib/* util.EvaRunnable -p port -h host -m mode -c Path to class\n"
                  + "Mode: REAL|COUNT|VERIFY|OPT");
	}

	@SuppressWarnings("rawtypes")
	public static void main(String[] args) throws InstantiationException, IllegalAccessException, ParseException, ClassNotFoundException {
		Options opt = new Options();
		opt.addOption("m", true, "Mode");
		opt.addOption("p", true, "Port");
		opt.addOption("h", true, "Host");
		opt.addOption("c", true, "Evaluator Class");
		opt.addOption("h", "help", false, "Help");
		CommandLineParser parser = new PosixParser();     
		CommandLine cmd = parser.parse(opt, args);
		if(!cmd.hasOption("m") || !cmd.hasOption("p") || !cmd.hasOption("h")|| !cmd.hasOption("c")
				|| Mode.getMode(cmd.getOptionValue('m')) == null) {
			printUtility();
			System.exit(1);
		}
		Class<?> clazz = Class.forName(cmd.getOptionValue('c'));
		EvaRunnable run = (EvaRunnable) clazz.newInstance();
		run.setParameter(Mode.getMode(cmd.getOptionValue('m')), cmd.getOptionValue('h'), new Integer(cmd.getOptionValue('p')));
		run.run();
	}
}