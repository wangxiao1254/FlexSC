package oram;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import flexsc.CompEnv;
import flexsc.Mode;
import flexsc.Party;

public class CircuitOram<T> extends TreeBasedOramParty<T> {
	public CircuitOramLib<T> lib;
	Block<T>[] scQueue;
	int cnt = 0;	
	public PlainBlock[] queue;
	public int queueCapacity;

	boolean[] nextPath()
	{
		boolean [] res = new boolean[logN];
		int temp = cnt;
		for(int i = res.length-1; i >= 0; --i) {
			res[i] = (temp&1)==1;
			temp>>=1;
		}
		cnt = (cnt+1)%N;
		return res;
	}


	public CircuitOram(InputStream is, OutputStream os, int N, int dataSize,
			Party p, int cap, Mode m, int sp) throws Exception {
		super(CompEnv.getEnv(m, p, is, os), N, dataSize, cap);
		lib = new CircuitOramLib<T>(lengthOfIden, lengthOfPos, lengthOfData, logN, capacity, env);
		queueCapacity = 30;
		queue = new PlainBlock[queueCapacity];

		for(int i = 0; i < queue.length; ++i) 
			queue[i] = getDummyBlock(p == Party.Alice);

		scQueue = prepareBlocks(queue, queue);		

	}
	
	public CircuitOram(CompEnv<T> env, int N, int dataSize,
			 int cap, int sp) throws Exception {
		super(env, N, dataSize, cap);
		lib = new CircuitOramLib<T>(lengthOfIden, lengthOfPos, lengthOfData, logN, capacity, env);
		queueCapacity = 30;
		queue = new PlainBlock[queueCapacity];

		for(int i = 0; i < queue.length; ++i) 
			queue[i] = getDummyBlock(p == Party.Alice);

		scQueue = prepareBlocks(queue, queue);		

	}

	protected void ControlEviction() throws Exception {
		flushOneTime(nextPath());
		flushOneTime(nextPath());
	}


	public void flushOneTime(boolean[] pos) throws Exception {
		PlainBlock[][] blocks = getPath(pos);
		Block<T>[][] scPath = preparePath(blocks, blocks);

		lib.flush(scPath, pos, scQueue);

		blocks = preparePlainPath(scPath);
		putPath(blocks, pos);
	}


	public T[] readAndRemove(T[] scIden, boolean[] pos, boolean RandomWhenNotFound) throws Exception {
		PlainBlock[][] blocks = getPath(pos);
		Block<T>[][] scPath = preparePath(blocks, blocks);


		Block<T> res = lib.readAndRemove(scPath, scIden);
		Block<T> res2 = lib.readAndRemove(scQueue, scIden);
		res = lib.mux(res, res2, res.isDummy);
		
		blocks = preparePlainPath(scPath);
		putPath(blocks, pos);

		if(RandomWhenNotFound) {
			PlainBlock b = randomBlock();	
			Block<T> scb = inputBlockOfClient(b);
			Block<T>finalRes = lib.mux(res, scb, res.isDummy);

			return finalRes.data;
		}
		else{
			return lib.mux(res.data, lib.zeros(res.data.length),res.isDummy);
		}
	}


	public void putBack(T[] scIden, T[] scNewPos, T[] scData) throws Exception {
		Block<T> b = new Block<T>(scIden, scNewPos, scData, lib.SIGNAL_ZERO);
		lib.add(scQueue, b);

		os.flush();
		ControlEviction();
	}

	public T[] read(T[] scIden, boolean[] pos, T[] scNewPos) throws Exception {
		scIden = Arrays.copyOf(scIden, lengthOfIden);
		T[] r = readAndRemove(scIden, pos, false);
		putBack(scIden, scNewPos, r);
		return r;
	}
	
	public void write(T[] scIden, boolean[] pos, T[] scNewPos, T[] scData) throws Exception {
		scIden = Arrays.copyOf(scIden, lengthOfIden);
		readAndRemove(scIden, pos, false);
		putBack(scIden, scNewPos, scData);
	}
	
	public T[] access(T[] scIden, boolean[] pos, T[] scNewPos, T[] scData, T op) throws Exception {
		scIden = Arrays.copyOf(scIden, lengthOfIden);
		T[] r = readAndRemove(scIden, pos, false);
		T[] toWrite = lib.mux(r, scData, op);
		putBack(scIden, scNewPos, toWrite);
		return toWrite;
	}
	
	public T[] conditionalReadAndRemove(T[] scIden, T[] pos, T condition) throws Exception {
		T[] posToUse = lib.mux(lib.randBools(pos.length), pos, condition);
		boolean[] path = lib.declassifyToBoth(posToUse);
		PlainBlock[][] blocks = getPath(path);
		Block<T>[][] scPath = preparePath(blocks, blocks);

		Block<T> res = lib.conditionalReadAndRemove(scPath, scIden, condition);
		Block<T> res2 = lib.conditionalReadAndRemove(scQueue, scIden, condition);
		res = lib.mux(res, res2, res.isDummy);
		
		blocks = preparePlainPath(scPath);
		putPath(blocks, path);

		return res.data;
	}


	public void conditionalPutBack(T[] scIden, T[] scNewPos, T[] scData, T condition) throws Exception {
		Block<T> b = new Block<T>(scIden, scNewPos, scData, lib.SIGNAL_ZERO);
		lib.conditionalAdd(scQueue, b, condition);
		os.flush();
		ControlEviction();
	}

}
