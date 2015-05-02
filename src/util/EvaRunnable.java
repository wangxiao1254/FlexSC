package util;

import java.util.Arrays;

import org.apache.commons.cli.ParseException;

import flexsc.CompEnv;
import flexsc.Flag;
import flexsc.Mode;
import flexsc.Party;

public abstract  class EvaRunnable<T> extends network.Client implements Runnable {
	public abstract void prepareInput(CompEnv<T> gen) throws Exception;
	public abstract void secureCompute(CompEnv<T> gen) throws Exception;
	public abstract void prepareOutput(CompEnv<T> gen) throws Exception;
	Mode m;
	int port;
	String host;
	protected String[] args;
	public boolean verbose = true;
	public ConfigParser config;
	public void setParameter(ConfigParser config, String[] args) {
		this.m = Mode.getMode(config.getString("Mode"));
		this.port = config.getInt("Port");
		host = config.getString("Host");
		this.args = args;
		this.config = config;
	}

	public void setParameter(Mode m, String host, int port){
		this.m = m;
		this.port = port;
		this.host = host;
	}

	public void run() {
		try {
			if(verbose)
				System.out.println("connecting");
			connect(host, port);
			if(verbose)
				System.out.println("connected");

			@SuppressWarnings("unchecked")
			CompEnv<T> env = CompEnv.getEnv(m, Party.Bob, this);

			double s = System.nanoTime();
			Flag.sw.startTotal();
			prepareInput(env);
			os.flush();
			secureCompute(env);
			os.flush();
			prepareOutput(env);
			os.flush();
			Flag.sw.stopTotal();
			double e = System.nanoTime();
			disconnect();
			if(verbose){
				System.out.println("Eva running time:"+(e-s)/1e9);
				System.out.println("Number Of AND Gates:"+env.numOfAnds);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	@SuppressWarnings("rawtypes")
	public static void main(String[] args) throws InstantiationException, IllegalAccessException, ParseException, ClassNotFoundException {
		ConfigParser config = new ConfigParser("Config.conf");

		Class<?> clazz = Class.forName(args[0]+"$Evaluator");
		EvaRunnable run = (EvaRunnable) clazz.newInstance();
		run.setParameter(config, Arrays.copyOfRange(args, 1, args.length));
		run.run();
		if(Flag.CountTime)
			Flag.sw.print();
		if(Flag.countIO)
			run.printStatistic();
	}
}