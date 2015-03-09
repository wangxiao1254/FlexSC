package flexsc.scvm;

import circuits.arithmetic.FloatLib;
import circuits.arithmetic.IntegerLib;
import flexsc.CompEnv;
import flexsc.IWritable;

public class BoxedInt<T> implements IWritable<BoxedInt<T>, T> {

	public CompEnv<T> env;
	public IntegerLib<T> intLib;
	public FloatLib<T> floatLib;
	public T[] value;

	public BoxedInt(CompEnv<T> env, T[] value) throws Exception {
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
	public BoxedInt<T> newObj(T[] data) throws Exception {
		return new BoxedInt<T>(env, data);
	}

	@Override
	public BoxedInt<T> fake() throws Exception {
		return this;
	}

	@Override
	public BoxedInt<T> muxFake(T dummy) throws Exception {
		return this;
	}

}
