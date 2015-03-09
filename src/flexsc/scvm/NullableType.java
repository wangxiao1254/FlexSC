package flexsc.scvm;

import java.util.Arrays;

import circuits.arithmetic.FloatLib;
import circuits.arithmetic.IntegerLib;
import flexsc.CompEnv;
import flexsc.IWritable;

public class NullableType<T1, T extends IWritable<T, T1>> implements IWritable<NullableType<T1, T>, T1> {

	public CompEnv<T1> env;
	public IntegerLib<T1> intLib;
	public FloatLib<T1> floatLib;
	public T value;
	public T1 isNull;
	
	public NullableType(CompEnv<T1> env, T value, T1 isNull) {
		this.env = env;
		intLib = new IntegerLib<T1>(env);
		floatLib = new FloatLib<T1>(env, 24, 8);
		this.value = value;
		this.isNull = isNull;
	}
	
	@Override
	public int numBits() {
		return value.numBits() + 1;
	}

	@Override
	public T1[] getBits() {
		T1[] bits = value.getBits();
		T1[] newBits = env.newTArray(bits.length + 1);
		for(int i=0; i<bits.length; ++i)
			newBits[i+1] = bits[i];
		newBits[0] = isNull;
		return newBits;
	}

	@Override
	public NullableType<T1, T> newObj(T1[] data) throws Exception {
		T1 isNull = data[0];
		T value = this.value.newObj(Arrays.copyOfRange(data, 1, data.length - 1));
		return new NullableType<T1, T>(env, value, isNull);
	}

	@Override
	public NullableType<T1, T> fake() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public T getFake() throws Exception {
		return value.fake();
	}
	
	public T muxFake() throws Exception {
		// TODO
		return null;
	}

	@Override
	public NullableType<T1, T> muxFake(T1 dummy) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	public NullableType<T1, T> mux(T1 dummy, NullableType<T1, T> b) throws Exception {
		T1[] bits = intLib.mux(value.getBits(), b.value.getBits(), dummy);
		T1 isNull = intLib.mux(this.isNull, b.isNull, dummy);
		return new NullableType<T1, T>(env, value.newObj(bits), isNull);
	}

}
