package oram;

import java.util.Arrays;

public class PlainBlock {
	public boolean[] iden;
	public boolean[] pos;
	public boolean[] data;
	public boolean isDummy;
	public PlainBlock(boolean[] iden, boolean[] pos, boolean[] data, boolean isDummy) {
		this.iden = iden;
		this.pos = pos;
		this.data = data;
		this.isDummy = isDummy;
	}
	
	public PlainBlock(boolean[] d, int lengthOfIden, int lengthOfPos, int lengthOfData) {
		this.iden = Arrays.copyOfRange(d, 0, lengthOfIden);
		this.pos = Arrays.copyOfRange(d, lengthOfIden, lengthOfIden+lengthOfPos);
		this.data = Arrays.copyOfRange(d, lengthOfIden+lengthOfPos, lengthOfIden+lengthOfPos+lengthOfData);
		this.isDummy = d[d.length-1];
	}

	public boolean[] toBooleanArray( ){
		boolean[] result = new boolean[iden.length + pos.length + data.length+1];
		System.arraycopy(iden, 0, result, 0, iden.length);
		System.arraycopy(pos, 0, result, iden.length, pos.length);
		System.arraycopy(data, 0, result, pos.length+iden.length, data.length);
		result[result.length-1] = isDummy;
		return result;
	}

	static public boolean[] toBooleanArray(PlainBlock[] blocks) {
		int blockSize = (blocks[0].iden.length + blocks[0].pos.length + 
				blocks[0].data.length+1);
		boolean[] result = new boolean[ blockSize* blocks.length];
		for(int i = 0; i < blocks.length; ++i) {
			boolean[] tmp = blocks[i].toBooleanArray();
			System.arraycopy(tmp, 0, result, i*blockSize, blockSize);
		}
		return result;
	}
}
