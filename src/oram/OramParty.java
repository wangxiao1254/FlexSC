// Copyright (C) 2014 by Xiao Shaun Wang <wangxiao@cs.umd.edu>
package oram;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Arrays;

import rand.ISAACProvider;
import flexsc.CompEnv;
import flexsc.Mode;
import flexsc.Party;

public abstract class OramParty<T> {
	public int N;
	int dataSize;

	public int logN;
	public int lengthOfIden;
	public int lengthOfPos;
	public int lengthOfData;

	protected InputStream is;
	protected OutputStream os;
	public CompEnv<T> env;
	public Party p;
	public Mode mode;

	static protected SecureRandom rng;
	static{
		Security.addProvider(new ISAACProvider ());
		try {
			rng = SecureRandom.getInstance ("ISAACRandom");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public BucketLib<T> lib;
	boolean[] dummyArray;

	public OramParty(CompEnv<T> env, int N, int dataSize) throws Exception {
		this.env = env;
		this.is = env.is;
		this.os = env.os;

		this.dataSize = dataSize;
		long a = 1;logN=1;
		while(a < N) {
			a*=2;
			++logN;
		}

		--logN;

		this.N = 1<<logN;
		lengthOfData = dataSize;
		lengthOfIden = logN;
		lengthOfPos = logN-1;
		p = env.p;
		mode = env.m;
		init();

	}

	public OramParty(CompEnv<T> env, int N, int dataSize, int lengthOfPos) throws Exception {
		this.env = env;
		this.is = env.is;
		this.os = env.os;

		this.dataSize = dataSize;
		int a = 1;logN=1;
		while(a < N){
			a*=2;
			++logN;
		}
		--logN;
		this.N = 1<<logN;
		lengthOfData = dataSize;
		lengthOfIden = logN;
		this.lengthOfPos = lengthOfPos;
		p = env.p;
		mode = env.m;
		init();

	}
	
	public void init() throws Exception {
		dummyArray = new boolean[lengthOfIden+lengthOfPos+lengthOfData+1];
		for(int i = 0; i < dummyArray.length; ++i)
			dummyArray[i] = false;
		lib = new BucketLib<T>(lengthOfIden, lengthOfPos, lengthOfData, env);
		
		boolean[] iden = new boolean[lengthOfIden];
		boolean[] pos = new boolean[lengthOfPos];
		boolean[] data = new boolean[lengthOfData];
		for(int i = 0; i < lengthOfIden; ++i)
			iden[i] = true;
		for(int i = 0; i < lengthOfPos; ++i)
			pos[i] = true;
		for(int i = 0; i < lengthOfData; ++i)
			data[i] = true;
		pb_for_count_mode =  new PlainBlock(iden, pos, data, false);

	}

	public Block<T>[] prepareBlocks(PlainBlock[] clientBlock, PlainBlock[] serverBlock) throws Exception {
		Block<T>[] s = inputBucketOfServer(serverBlock);
		Block<T>[] c = inputBucketOfClient(clientBlock);
		return lib.xor(s, c);
	}

	public Block<T> prepareBlock(PlainBlock clientBlock, PlainBlock serverBlock/*, PlainBlock randomBlock*/) throws Exception {
		Block<T> s = inputBlockOfServer(serverBlock);
		Block<T> c = inputBlockOfClient(clientBlock);
		return lib.xor(s, c);
	}


	public PlainBlock preparePlainBlock(Block<T> blocks, Block<T> randomBlock) throws Exception {
		PlainBlock result = outputBlock(lib.xor(blocks, randomBlock));
		return result;	
	}

	public PlainBlock[] preparePlainBlocks(Block<T>[] blocks, Block<T>[] randomBlock) throws Exception {
		PlainBlock[] result = outputBucket(lib.xor(blocks, randomBlock));
		return result;
	}


	public Block<T> inputBlockOfServer(PlainBlock b) throws Exception {
		T[] TArray = env.inputOfBob(b.toBooleanArray());
		return new Block<T>(TArray, lengthOfIden,lengthOfPos,lengthOfData);

	}

	public Block<T> inputBlockOfClient(PlainBlock b) throws Exception {
		T[] TArray = env.inputOfAlice(b.toBooleanArray());		
		return new Block<T>(TArray, lengthOfIden,lengthOfPos,lengthOfData);
	}


	public Block<T>[] toBlocks(T[] Tarray, int lengthOfIden, int lengthOfPos, int lengthOfData, int capacity) {
		int blockSize = lengthOfIden + lengthOfPos + lengthOfData+1;
		Block<T>[] result = lib.newBlockArray(capacity);
		for(int i = 0; i < capacity; ++i) {
			result[i] = new Block<T>(Arrays.copyOfRange(Tarray, i*blockSize, (i+1)*blockSize),
					lengthOfIden, lengthOfPos, lengthOfData);
		}
		return result;
	}

	public Block<T>[] inputBucketOfServer(PlainBlock[] b) throws Exception {
//		System.out.println(b.length);
		T[] TArray = env.inputOfBob(PlainBlock.toBooleanArray(b));
		return toBlocks(TArray, lengthOfIden, lengthOfPos, lengthOfData, b.length);//new Block<T>(TArray, lengthOfIden,lengthOfPos,lengthOfData);
	}

	public Block<T>[] inputBucketOfClient(PlainBlock[] b) throws Exception {
		T[] TArray = env.inputOfAlice(PlainBlock.toBooleanArray(b));
		os.flush();
		return toBlocks(TArray, lengthOfIden, lengthOfPos, lengthOfData, b.length);
	}


	public PlainBlock outputBlock(Block<T> b) throws Exception {
		boolean[] iden = env.outputToAlice(b.iden);
		boolean[] pos = env.outputToAlice(b.pos);
		boolean[] data = env.outputToAlice(b.data);
		boolean isDummy = env.outputToAlice(b.isDummy);

		return new PlainBlock(iden, pos, data, isDummy);
	}

	public PlainBlock[] outputBucket(Block<T>[] b) throws Exception {
		PlainBlock[] result = new PlainBlock[b.length];
		for(int i = 0; i < b.length; ++i)
			result[i] = outputBlock(b[i]);
		return result;
	}

	public PlainBlock[][] outputBuckets(Block<T>[][] b) throws Exception {
		PlainBlock[][] result = new PlainBlock[b.length][];
		for(int i = 0; i < b.length; ++i)
			result[i] = outputBucket(b[i]);
		os.flush();
		return result;
	}

	PlainBlock pb_for_count_mode;
	public PlainBlock getDummyBlock(boolean b){
		if(mode == Mode.COUNT)
			return pb_for_count_mode;
		boolean[] iden = new boolean[lengthOfIden];
		boolean[] pos = new boolean[lengthOfPos];
		boolean[] data = new boolean[lengthOfData];
		for(int i = 0; i < lengthOfIden; ++i)
			iden[i] = true;
		for(int i = 0; i < lengthOfPos; ++i)
			pos[i] = true;
		for(int i = 0; i < lengthOfData; ++i)
			data[i] = true;
		return new PlainBlock(iden, pos, data, b);
	}

	PlainBlock r = getDummyBlock(true);
	public PlainBlock randomBlock() {
		if(mode == Mode.COUNT)
			return pb_for_count_mode;

		PlainBlock result = getDummyBlock(true);
		for(int i = 0; i < lengthOfIden; ++i)
			result.iden[i] = rng.nextBoolean();
		for(int i = 0; i < lengthOfPos; ++i)
			result.pos[i] = rng.nextBoolean();
		for(int i = 0; i < lengthOfData; ++i)
			result.data[i] = rng.nextBoolean();
		result.isDummy = rng.nextBoolean();
		return result;
	}

	public PlainBlock[] randomBucket(int length) {
		PlainBlock[] result = new PlainBlock[length];
		for(int i = 0; i < length; ++i)
			result[i] = randomBlock();
		return result;
	}
}
