// Copyright (C) 2013 by Yan Huang <yhuang@cs.umd.edu>
// 					 and Xiao Shaun Wang <wangxiao@cs.umd.edu>

package flexsc;

public enum Mode {
	// verify the correctness of the circuit without running the protocol
	VERIFY, 
	//GRR3 + Free XOR
	REAL,
	//Simulating the protocol and count number of gates/encs
	COUNT,
	//Half Gates
	OPT;
}
