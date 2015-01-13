// Copyright (C) 2013 by Yan Huang <yhuang@cs.umd.edu>
// 					 and Xiao Shaun Wang <wangxiao@cs.umd.edu>

package flexsc;

import util.StopWatch;

public class Flag {
	public static boolean CountTime = true;
	public static StopWatch sw = new StopWatch(CountTime);
	public static boolean countIO = true;
	public static boolean FakeOT = false;
}
