package util;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import flexsc.CompEnv;
import flexsc.Flag;
import flexsc.Mode;
import flexsc.Party;

public abstract class GenRunnable<T> extends network.Server implements Runnable {

	Mode m;
	int port;
	public void setParameter(Mode m, int port) {
		this.m = m;
		this.port = port;
	}
	public abstract void prepareInput(CompEnv<T> gen);
	public abstract void secureCompute(CompEnv<T> gen);
	public abstract void prepareOutput(CompEnv<T> gen);

	public void run() {
		try {
			listen(port);
			@SuppressWarnings("unchecked")
			CompEnv<T> env = CompEnv.getEnv(m, Party.Alice, is, os);
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
        .println("Usage: java -cp bin:lib/* util.EvaRunnable -p port -m mode -c Path to class"
                + "Mode: REAL|COUNT|VERIFY|OPT");
	}
	
	@SuppressWarnings("rawtypes")
	public static void main(String[] args) throws ParseException, ClassNotFoundException, InstantiationException, IllegalAccessException { 
		Options opt = new Options();
		opt.addOption("m", true, "Mode");
		opt.addOption("p", true, "Port");
		opt.addOption("c", true, "Generator Class");
		CommandLineParser parser = new PosixParser();     
		CommandLine cmd = parser.parse(opt, args);
		if(!cmd.hasOption("m") || !cmd.hasOption("p") || !cmd.hasOption("c")
				|| Mode.getMode(cmd.getOptionValue('m')) == null) {
			printUtility();
			System.exit(1);
		}
		Class<?> clazz = Class.forName(cmd.getOptionValue('c')+"$Generator");
		GenRunnable run = (GenRunnable) clazz.newInstance();
		run.setParameter(Mode.getMode(cmd.getOptionValue('m')), new Integer(cmd.getOptionValue('p')));
		run.run();
		Flag.sw.print();
	}
}
