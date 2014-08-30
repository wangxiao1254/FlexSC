// Copyright (C) 2013 by Yan Huang <yhuang@cs.umd.edu>
// 					 and Xiao Shaun Wang <wangxiao@cs.umd.edu>

package flexsc;

public abstract class Signal {
	public Signal() {}
	abstract public Signal ONE();
	abstract public Signal ZERO();
}
