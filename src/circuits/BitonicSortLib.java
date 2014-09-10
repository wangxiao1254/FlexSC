package circuits;

import flexsc.CompEnv;

public class BitonicSortLib<T> extends IntegerLib<T>
{
    public BitonicSortLib(CompEnv<T> e) {
		super(e);
	}

    public void sortWithPayload(T[][] a, T[][] data, T isAscending) throws Exception {
        bitonicSortWithPayload(a, data, 0, a.length, isAscending);
    }

    private void bitonicSortWithPayload(T[][]key, T[][] data, int lo, int n, T dir) throws Exception {
        if (n > 1) {
            int m=n/2;
            bitonicSortWithPayload(key, data, lo, m, not(dir));
            bitonicSortWithPayload(key, data, lo+m, n-m, dir);
            bitonicMergeWithPayload(key, data, lo, n, dir);
        }
    }

    private void bitonicMergeWithPayload(T[][] key, T[][] data, int lo, int n, T dir) throws Exception {
        if (n > 1) {
            int m=greatestPowerOfTwoLessThan(n);
            for (int i = lo; i < lo + n - m; i++)
                compareWithPayload(key, data, i, i+m, dir);
            bitonicMergeWithPayload(key, data, lo, m, dir);
            bitonicMergeWithPayload(key, data, lo + m, n - m, dir);
        }
    }

    private void compareWithPayload(T[][] key, T[][] data, int i, int j, T dir) throws Exception {
    	T greater = not(leq(key[i], key[j]));
    	T swap = eq(greater, dir);
    	T[] ki = mux(key[i], key[j], swap);
    	T[] kj = mux(key[j], key[i], swap);
    	key[i] = ki;
    	key[j] = kj;
    	
    	T[] di = mux(data[i], data[j], swap);
    	T[] dj = mux(data[j], data[i], swap);
    	data[i] = di;
    	data[j] = dj;
    }
 
    
    public void sort(T[][] a, T isAscending) throws Exception {
        bitonicSort(a, 0, a.length, isAscending);
    }

    private void bitonicSort(T[][]key, int lo, int n, T dir) throws Exception {
        if (n > 1) {
            int m=n/2;
            bitonicSort(key, lo, m, not(dir));
            bitonicSort(key, lo+m, n-m, dir);
            bitonicMerge(key, lo, n, dir);
        }
    }

    private void bitonicMerge(T[][] key, int lo, int n, T dir) throws Exception {
        if (n > 1) {
            int m=greatestPowerOfTwoLessThan(n);
            for (int i = lo; i < lo + n - m; i++)
                compare(key, i, i+m, dir);
            bitonicMerge(key, lo, m, dir);
            bitonicMerge(key, lo + m, n - m, dir);
        }
    }

    private void compare(T[][] key, int i, int j, T dir) throws Exception {
    	T greater = not(leq(key[i], key[j]));
    	T swap = eq(greater, dir);
    	T[] ki = mux(key[i], key[j], swap);
    	T[] kj = mux(key[j], key[i], swap);
    	key[i] = ki;
    	key[j] = kj;
    }
    
    private int greatestPowerOfTwoLessThan(int n) {
        int k=1;
        while (k<n)
            k=k<<1;
        return k>>1;
    }
}