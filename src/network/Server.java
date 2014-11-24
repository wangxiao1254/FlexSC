package network;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

public class Server {
	static int bufferSize = 655360;
	private ServerSocket sock;

	public InputStream is;
	public OutputStream os;

	public void listen(int port) throws Exception {
		Socket clientSock;
		sock = new ServerSocket(port); // create socket and bind to port
		clientSock = sock.accept(); // wait for client to connect

		os = new BufferedOutputStream(clientSock.getOutputStream(), bufferSize);
		is = new BufferedInputStream(clientSock.getInputStream(), bufferSize);
	}

	public void disconnect() throws Exception {
		is.read();
		os.write(0);
		os.flush(); // dummy I/O to prevent dropping connection earlier than
					// protocol payloads are received.

		sock.close();
	}

	static public byte[] readBytes(InputStream is, int len) throws IOException {

		byte[] temp = new byte[len];
		int remain = len;
		while (0 < remain) {
			int readBytes = is.read(temp, len - remain, remain);
			if (readBytes != -1) {
				remain -= readBytes;
			}
		}
		return temp;
	}

	static public byte[] readBytes(InputStream is) throws IOException {
		byte[] lenBytes = readBytes(is, 4);
		int len = ByteBuffer.wrap(lenBytes).getInt();
		return readBytes(is, len);
	}

	static public void writeByte(OutputStream os, byte[] data)
			throws IOException {
		os.write(ByteBuffer.allocate(4).putInt(data.length).array());
		os.write(data);
	}
}
