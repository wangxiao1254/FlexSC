package flexsc.scvm;

import circuits.arithmetic.FloatLib;
import circuits.arithmetic.IntegerLib;
import flexsc.CompEnv;
import flexsc.IWritable;

public class BoxedFloat<T> implements IWritable<BoxedFloat<T>, T> {

	public CompEnv<T> env;
	public IntegerLib<T> intLib;
	public FloatLib<T> floatLib;
	public T[] value;

	public BoxedFloat(CompEnv<T> env, T[] value) throws Exception {
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
	public BoxedFloat<T> newObj(T[] data) throws Exception {
		return new BoxedFloat<T>(env, data);
	}

	@Override
	public BoxedFloat<T> fake() throws Exception {
		return this;
	}

	@Override
	public BoxedFloat<T> muxFake(T dummy) throws Exception {
		return this;
	}	
}
