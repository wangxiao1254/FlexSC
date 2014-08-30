// Copyright (C) 2013 by Yan Huang <yhuang@cs.umd.edu>
// Improved by Xiao Shaun Wang <wangxiao@cs.umd.edu> and Kartik Nayak <kartik@cs.umd.edu>

package network;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Client {
	private Socket sock = null;
	public InputStream is;
	public OutputStream os;

	public void connect(String server, int port) throws Exception {
		while(true){
			try{
				sock = new java.net.Socket(server, port);          // create socket and connect
				if(sock != null)
					break;
			}
			catch(IOException e){
				Thread.sleep(100);
			}
		}

			os = new BufferedOutputStream(sock.getOutputStream(), Server.bufferSize);  
			is = new BufferedInputStream( sock.getInputStream(), Server.bufferSize);
	}

	public void disconnect() throws Exception {
		os.write(0);
		os.flush();
		is.read(); // dummy write to prevent dropping connection earlier than
		// protocol payloads are received.
		sock.close(); 
	}
}
