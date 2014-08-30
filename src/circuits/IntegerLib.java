// Copyright (C) 2014 by Xiao Shaun Wang <wangxiao@cs.umd.edu>
package circuits;

import java.util.Arrays;

import flexsc.CompEnv;

public class IntegerLib<T> extends CircuitLib<T> {

	public IntegerLib(CompEnv<T> e) {
		super(e);
	}

	static final int S = 0;
	static final int COUT = 1;
	protected T[] add(T x, T y, T cin) throws Exception {
		T[] res = env.newTArray(2);

		T t1 = env.xor(x, cin);
		T t2 = env.xor(y, cin);
		res[S] = env.xor(x, t2);
		t1 = env.and(t1, t2);
		res[COUT] = env.xor(cin, t1);

		return res;
	}

	public T[] add(T[] x, T[] y, boolean cin) throws Exception {
		assert(x != null && y != null && x.length == y.length) : "add: bad inputs.";
		T[] res = env.newTArray(x.length);
		T[] t = add(x[0], y[0], env.newT(cin));
		res[0] = t[S];
		for (int i = 0; i < x.length-1; i++) {
			t = add(x[i+1], y[i+1], t[COUT]);
			res[i+1] = t[S];
		}
		return res;
	}

	public T[] add(T[] x, T[] y) throws Exception {

		return add(x, y, false);
	}

	public T[] sub(T x, T y) throws Exception{
		T[] ax = env.newTArray(2);
		ax[1] = SIGNAL_ZERO;
		ax[0] = x;
		T[] ay = env.newTArray(2);
		ay[1] = SIGNAL_ZERO;
		ay[0] = y;
		return sub(x, y);
	}

	public T[] sub(T[] x, T[] y) throws Exception {
		assert(x != null && y != null && x.length == y.length) : "sub: bad inputs.";

		return add(x, not(y), true);
	}

	public T[] incrementByOne(T[] x) throws Exception {
		T[] one = zeros(x.length);
		one[0] = SIGNAL_ONE;
		return add(x, one);
	}

	public T[] decrementByOne(T[] x) throws Exception {
		T[] one = zeros(x.length);
		one[0] = SIGNAL_ONE;
		return sub(x, one);
	}

	public T[] conditionalIncreament(T[] x, T flag) throws Exception {
		T[] one = zeros(x.length);
		one[0] = mux(SIGNAL_ZERO, SIGNAL_ONE, flag);
		return add(x, one);
	}

	public T[] conditionalDecrement(T[] x, T flag) throws Exception {
		T[] one = zeros(x.length);
		one[0] = mux(SIGNAL_ZERO, SIGNAL_ONE, flag);
		return sub(x, one);
	}

	public T geq(T[] x, T[] y) throws Exception {
		assert(x.length == y.length) : "bad input";

		T[] result = sub(x, y);
		return not(result[result.length-1]);
	}

	public T leq(T[] x, T[] y) throws Exception {
		return geq(y, x);
	}

	public T[] multiply(T[] x, T[] y) throws Exception {
		assert(x.length == y.length) : "bad input";
		return Arrays.copyOf(multiplyInternal(x, y), x.length);
	}

	// This multiplication does not truncate the length of x and y
	public T[] multiplyFull(T[] x, T[] y) throws Exception {
		return multiplyInternal(x, y);
	}

	private T[] multiplyInternal(T[] x, T[] y) throws Exception {
		assert(x != null && y!= null) : "multiply: bad inputs";	

		T[] res = zeros(x.length+y.length);
		T[] zero = zeros(res.length);
		T longerX[] = zeros(res.length);
		System.arraycopy(x, 0, longerX, 0, x.length);

		for(int i = 0; i < y.length; ++i) {
			res = add(res, mux(zero, longerX, y[i]));
			longerX = leftShift(longerX);
		}
		return res;
	}

	public T[] absolute(T[] x) throws Exception {
		T reachedOneSignal = SIGNAL_ZERO;
		T[] result = zeros(x.length);
		for(int i = 0; i < x.length; ++i) {
			T comp = eq(SIGNAL_ONE, x[i]);
			result[i] = xor(x[i], reachedOneSignal);
			reachedOneSignal = or(reachedOneSignal, comp);
		}
		return mux(x, result, x[x.length-1]);
	}

	public T[] divide(T[] x, T[] y) throws Exception {
		T[] absoluteX = absolute(x);
		T[] dividend = zeros(x.length + y.length);
		System.arraycopy(absoluteX, 0, dividend, 0, absoluteX.length);
		T[] absoluteY = absolute(y);
		T[] divisor = zeros(x.length + y.length);
		System.arraycopy(absoluteY, 0, divisor, x.length, absoluteY.length);

		T[] quotient = zeros(dividend.length);
		T[] zero = zeros(dividend.length);
		for(int i = 0; i < x.length+1; ++i) {
			quotient = leftShift(quotient);

			T divisorIsLEQ = leq(divisor, dividend);
			T[] temp = mux(zero, divisor, divisorIsLEQ);
			dividend = sub(dividend, temp);
			quotient[0] = divisorIsLEQ;

			divisor = rightShift(divisor);
		}

		//		return quotient;
		return addSign(Arrays.copyOf(quotient, x.length), xor(x[x.length-1], y[y.length-1]));
	}


	public T[] reminder(T[] x, T[] y) throws Exception {
		//can be better.
		T[] q = divide(x, y);
		return sub(x, multiply(y, q));
		/*
		Signal[] absoluteX = absolute(x);
		Signal[] dividend = zeros(x.length + y.length);
		System.arraycopy(absoluteX, 0, dividend, 0, absoluteX.length);
		Signal[] absoluteY = absolute(y);
		Signal[] divisor = zeros(x.length + y.length);
		System.arraycopy(absoluteY, 0, divisor, x.length, absoluteY.length);

		Signal[] zero = zeros(dividend.length);
		for(int i = 0; i < x.length+1; ++i) {
			Signal divisorIsLEQ = leq(divisor, dividend);
			Signal[] temp = mux(zero, divisor, divisorIsLEQ);
			dividend = sub(dividend, temp);	
			divisor = rightShift(divisor);
		}

		//return dividend;
		return addSign(dividend, xor(x[x.length-1], y[y.length-1]));*/
	}

	private T[] addSign(T[] x, T sign) throws Exception {

		T[] reachedOneSignal = zeros(x.length);
		T[] result = env.newTArray(x.length);
		for(int i = 0; i < x.length-1; ++i) {
			//Signal comp = x[i];
			reachedOneSignal[i+1] = or(reachedOneSignal[i], x[i]);
			result[i] = xor(x[i], reachedOneSignal[i]);
		}
		result[x.length-1] = xor(x[x.length-1], reachedOneSignal[x.length-1]);
		return mux(x, result, sign);


	}

	public T[] commonPrefix(T[] x, T[] y) throws Exception {
		assert(x != null && y!= null) : "multiply: bad inputs";
		T[] result = xor(x, y);

		for(int i = x.length-2; i>=0; --i) {
			result[i] = or(result[i], result[i+1]);
		}
		return result;
	}

	public T[] leadingZeros(T[] x) throws Exception {
		assert(x!= null) : "leading zeros: bad inputs";

		T[] result = Arrays.copyOf(x, x.length);
		for(int i = result.length-2; i>=0; --i) {
			result[i] = or(result[i], result[i+1]);
		}	

		return numberOfOnes(not(result));
	}

	public T[] lengthOfCommenPrefix(T[] x, T [] y) throws Exception {
		assert(x!= null) : "lengthOfCommenPrefix : bad inputs";

		return leadingZeros(xor(x, y));
	}


	/* Integer manipulation
	 * */
	public T[] leftShift(T[] x){
		assert(x!= null) : "leftShift: bad inputs";
		return leftPublicShift(x, 1);
	}

	public T[] rightShift(T[] x){
		assert(x!= null) : "rightShift: bad inputs";
		return rightPublicShift(x, 1);
	}

	public T[] leftPublicShift(T[] x, int s) {
		assert(x!= null && s < x.length) : "leftshift: bad inputs";

		T res[] = env.newTArray(x.length);
		System.arraycopy(zeros(s), 0, res, 0, s);
		System.arraycopy(x, 0, res, s, x.length-s);

		return res;
	}

	public T[] rightPublicShift(T[] x, int s) {
		assert(x!= null && s < x.length) : "leftshift: bad inputs";

		T[] res = env.newTArray(x.length);
		System.arraycopy(x, s, res, 0, x.length-s);
		System.arraycopy(zeros(s), 0, res, x.length-s, s);//assume that this function is operated on 32bit word

		return res;
	}

	public T[] conditionalLeftPublicShift(T[] x, int s, T sign) throws Exception {
		assert(x!= null && s < x.length) : "leftshift: bad inputs";

		T[] res = env.newTArray(x.length);
		System.arraycopy(mux(Arrays.copyOfRange(x, 0, s), zeros(s), sign), 0, res, 0, s);
		//System.arraycopy(sign, s, res, s, s);
		System.arraycopy(mux(Arrays.copyOfRange(x, s, x.length), Arrays.copyOfRange(x, 0, x.length)
				, sign), 0, res, s, x.length-s);
		return res;
	}

	public T[] conditionalRightPublicShift(T[] x, int s, T sign) throws Exception {
		assert(x!= null && s < x.length) : "rightshift: bad inputs";

		T res[] = env.newTArray(x.length);
		System.arraycopy(mux(Arrays.copyOfRange(x, 0, x.length-s), Arrays.copyOfRange(x, s, x.length), sign), 0, res, 0, x.length-s);
		System.arraycopy(mux(Arrays.copyOfRange(x, x.length-s, x.length), zeros(s), sign), 0, res, x.length-s, s);
		return res;
	}


	public T[] leftPrivateShift(T[] x, T[] lengthToShift) throws Exception {
		T[] res = Arrays.copyOf(x, x.length);

		for(int i = 0; ((1<<i) < x.length) && i < lengthToShift.length; ++i)
			res = conditionalLeftPublicShift(res, (1<<i), lengthToShift[i]);
		T clear = SIGNAL_ZERO;
		for(int i = 0; i < lengthToShift.length; ++i) {
			if((1<<i) >= x.length)
				clear = or(clear, lengthToShift[i]);
		}

		return mux(res, zeros(x.length), clear);
	}

	public T[] rightPrivateShift(T[] x, T[] lengthToShift) throws Exception {
		T[] res = Arrays.copyOf(x, x.length);

		for(int i = 0; ((1<<i) < x.length) && i < lengthToShift.length; ++i)
			res = conditionalRightPublicShift(res, (1<<i), lengthToShift[i]);
		T clear = SIGNAL_ZERO;
		for(int i = 0; i < lengthToShift.length; ++i) {
			if((1<<i) >= x.length)
				clear = or(clear, lengthToShift[i]);
		}

		return mux(res, zeros(x.length), clear);
	}

	T compare(T x, T y, T cin) throws Exception {
		T t1 = xor(x, cin);
		T t2 = xor(y, cin);
		t1 = and(t1, t2);
		return xor(x, t1);
	}

	public T compare(T[] x, T[] y) throws Exception {
		assert(x != null && y != null && x.length == y.length) : "compare: bad inputs.";

		T t = env.newT(false);
		for (int i = 0; i < x.length; i++) {
			t = compare(x[i], y[i], t);
		}

		return t;
	}

	protected T eq(T x, T y) {
		assert(x != null && y!= null) : "CircuitLib.eq: bad inputs";

		return not(xor(x, y));
	}

	public T eq(T[] x, T[] y) throws Exception {
		assert(x != null && y != null && x.length == y.length) : "CircuitLib.eq[]: bad inputs.";

		T res = env.newT(true);
		for (int i = 0; i < x.length; i++) {
			T t = eq(x[i], y[i]);
			res = env.and(res, t);
		}

		return res;
	}

	public T[] twosComplement(T[] x) throws Exception {
		T reachOne = SIGNAL_ZERO;
		T[] result = env.newTArray(x.length);
		for(int i = 0; i < x.length; ++i) {
			result[i] = xor(x[i], reachOne);
			reachOne = or(reachOne, x[i]);
		}
		return result;
	}

	public T[] hammingDistance(T[] x, T[] y) throws Exception {
		T[] a = xor(x, y);
		return numberOfOnes(a);
		//return a;
	}

	public T[] numberOfOnes(T[] t) throws Exception {
		if(t.length == 0) {
			T[] res = env.newTArray(1);
			res[0] = SIGNAL_ZERO;
			return res;
		}
		if(t.length == 1) {
			return t;
		}
		else {
			int length = 1;
			int w = 1;
			while(length <= t.length){length<<=1;w++;}
			length>>=1;

			T[] res1 = numberOfOnesN(Arrays.copyOfRange(t, 0, length));
			T[] res2 = numberOfOnes(Arrays.copyOfRange(t, length, t.length));
			return add(padSignal(res1, w), padSignal(res2, w));
		}
	}
	public T[] numberOfOnesN(T[] t) throws Exception {
		assert(t!= null): "numberOfOnes : bad input";

		T[] x = Arrays.copyOf(t, t.length);
		for(int width = 1; width < x.length; width*=2)
			for(int i = 0; i < x.length; i+=(2*width)) {
				T[] re = padSignal(unSignedAdd(Arrays.copyOfRange(x, i, i+width), 
						Arrays.copyOfRange(x, i+width,i + 2*width)), 2*width);
				System.arraycopy(re, 0, x, i, 2*width);
			}

		return x; 

	}

	public T[] unSignedAdd(T[] x, T[] y) throws Exception {
		assert(x != null && y != null && x.length == y.length) : "add: bad inputs.";
		T[] res = env.newTArray(x.length+1);

		T[] t = add(x[0], y[0], env.newT(false));
		res[0] = t[S];
		for (int i = 0; i < x.length-1; i++) {
			t = add(x[i+1], y[i+1], t[COUT]);
			res[i+1] = t[S];
		}
		res[res.length-1] = t[COUT];
		return res;
	}

	public T[] min(T[] x, T[] y) throws Exception {
		T leq = leq(x, y);
		return mux(y, x, leq);
	}

	public T[] integerSqrt(T[] x) throws Exception {
		T[] rem = zeros(x.length);
		T[] root = zeros(x.length);
		for (int i = 0; i < x.length/2; i++) {
			root = leftShift(root);
			rem = add(leftPublicShift(rem, 2), rightPublicShift(x, x.length - 2));
			x = leftPublicShift(x, 2);
			T[] oldRoot = root;
			root = incrementByOne(root);
			T isRootSmaller = leq(root, rem);
			T[] remMinusRoot = sub(rem, root);
			rem = mux(rem, remMinusRoot, isRootSmaller);
			root = mux(oldRoot, incrementByOne(root), isRootSmaller);
		}
		return rightShift(root);
	}
}