// Copyright (C) 2014 by Xiao Shaun Wang <wangxiao@cs.umd.edu>
package oram;

import java.io.IOException;

import flexsc.CompEnv;
import flexsc.Party;


public abstract class TreeBasedOramParty<T> extends OramParty<T> {
	public PlainBlock[][] tree;
	protected int capacity;
	public TreeBasedOramParty(CompEnv<T> env, int N, int dataSize,
			int capacity) throws Exception {
		super(env,  N, dataSize);
		this.capacity = capacity;

		tree  = new PlainBlock[this.N][capacity];

		PlainBlock b = getDummyBlock(p == Party.Alice);

		for(int i = 0; i < this.N; ++i)
			for(int j = 0; j < capacity; ++j)
				tree[i][j] = b;
	}


	protected PlainBlock[][] getPath(boolean[] path) {
		PlainBlock[][] result = new PlainBlock[logN][];
		int index = 1;
		result[0] = tree[index];
		for(int i = 1; i < logN; ++i) {
			index*=2;
			if(path[lengthOfPos-i])
				++index;
			result[i] = tree[index];
		}
		return result;
	}

	protected void putPath(PlainBlock[][] blocks, boolean[] path){
		int index = 1;
		tree[index] = blocks[0];
		for(int i = 1; i < logN; ++i) {
			index*=2;
			if(path[lengthOfPos-i])
				++index;
			tree[index] = blocks[i];
		}
	}

	public boolean[] getRandomPath() throws IOException{
		if(p == Party.Alice) {
			boolean[] pos = new boolean[lengthOfPos];
			for(int i = 0; i < lengthOfPos; ++i) {
				pos[i] = rng.nextBoolean();
				if(pos[i])
					os.write(1);
				else os.write(0);
			}
			os.flush();
			return pos;
		}
		else {
			boolean[] pos = new boolean[lengthOfPos];
			for(int i = 0; i < lengthOfPos; ++i) {
				pos[i] = (is.read()==1);
			}
			return pos;

		}
	}


	public Block<T>[][] preparePath(PlainBlock[][] clientBlock, PlainBlock[][] serverBlock

			) throws Exception {
		Block<T>[][] s = inputPathOfServer(serverBlock);
		Block<T>[][] c = inputPathOfClient(clientBlock);
		return  lib.xor(s, c);
	}

	public Block<T>[][] inputPathOfClient(PlainBlock[][] b) throws Exception {
		int length = 0;
		for(int i = 0; i < b.length; ++i)
			length += b[i].length;

		PlainBlock[] tmp = new PlainBlock[length];
		int cnt = 0;
		for(int i = 0; i < b.length; ++i)
			for(int j = 0; j < b[i].length; ++j)
				tmp[cnt++] = b[i][j];


		Block<T>[] tmpResult =  inputBucketOfClient(tmp);
		cnt = 0;
		Block<T>[][] result = lib.newBlockMatrix(b.length);
		for(int i = 0; i < b.length; ++i){
			result[i] = lib.newBlockArray(b[i].length);
			for(int j = 0; j < b[i].length; ++j)
				result[i][j] = tmpResult[cnt++];
		}
		return result;
	}

	public Block<T>[][] inputPathOfServer(PlainBlock[][] b) throws Exception{
		int length = 0;
		for(int i = 0; i < b.length; ++i)
			length += b[i].length;

		PlainBlock[] tmp = new PlainBlock[length];
		int cnt = 0;
		for(int i = 0; i < b.length; ++i)
			for(int j = 0; j < b[i].length; ++j)
				tmp[cnt++] = b[i][j];
		Block<T>[] tmpResult =  inputBucketOfServer(tmp);

		cnt = 0;
		Block<T>[][] result = lib.newBlockMatrix(b.length);
		for(int i = 0; i < b.length; ++i) {
			result[i] = lib.newBlockArray(b[i].length);
			for(int j = 0; j < b[i].length; ++j)
				result[i][j] = tmpResult[cnt++];
		}
		return result;
	}


	public PlainBlock[][] preparePlainPath(Block<T>[][] blocks) throws Exception {
		Block<T> [][] randomSCPath = lib.newBlockMatrix(blocks.length);

		// this part can potentially be much better :)!!! io!
		PlainBlock[][] randomPath = new PlainBlock[blocks.length][];
		for(int i = 0; i < randomPath.length; ++i){
			randomPath[i] = randomBucket(blocks[i].length);
		}
		os.flush();
		randomSCPath = inputPathOfServer(randomPath);			
		os.flush();
		PlainBlock[][] result = outputBuckets(lib.xor(blocks, randomSCPath));
		os.flush();
		if(p == Party.Alice)
			return result;
		else return randomPath;
	}

	public PlainBlock[][] randomPath(PlainBlock[][] path) {
		PlainBlock[][] result = new PlainBlock[path.length][];
		for(int i = 0; i < path.length; ++i)
			result[i] = randomBucket(path[i].length);
		return result;
	}
}
