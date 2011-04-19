package net.gescobar.smppserver.util;

import ie.omk.smpp.net.StreamLink;

import java.io.IOException;
import java.net.Socket;

public class SocketLink extends StreamLink {

	public SocketLink(Socket socket) throws IOException {
		super(socket.getInputStream(), socket.getOutputStream());
	}

}
