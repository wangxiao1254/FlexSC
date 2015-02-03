package flexsc;

// for compiler generated code
public interface IWritable<T1, T2> {
	public int numBits();

	public T2[] getBits();

	public T1 newObj(T2[] data) throws Exception;
}