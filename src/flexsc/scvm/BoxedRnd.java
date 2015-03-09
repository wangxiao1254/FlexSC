package flexsc.scvm;

import circuits.arithmetic.FloatLib;
import circuits.arithmetic.IntegerLib;
import flexsc.CompEnv;
import flexsc.IWritable;

public class BoxedRnd<T> implements IWritable<BoxedRnd<T>, T> {

	public CompEnv<T> env;
	public IntegerLib<T> intLib;
	public FloatLib<T> floatLib;
	public T[] value;

	public BoxedRnd(CompEnv<T> env, T[] value) throws Exception {
		this.env = env;
		intLib = new IntegerLib<T>(env);
		floatLib = new FloatLib<T>(env, 24, 8);
		this.value = value;
	}


	@Override
	public int numBits() {
		return value.length;
	}

	@Override
	public T[] getBits() {
		return value;
	}

	@Override
	public BoxedRnd<T> newObj(T[] data) throws Exception {
		return new BoxedRnd<T>(env, data);
	}

	@Override
	public BoxedRnd<T> fake() throws Exception {
		return new BoxedRnd<T>(env, intLib.randBools(value.length));
	}

	@Override
	public BoxedRnd<T> muxFake(T dummy) throws Exception {
		return new BoxedRnd<T>(env,
				intLib.mux(
						value,
						intLib.randBools(value.length),
						dummy));
	}

}
