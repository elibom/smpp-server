package net.gescobar.smppserver;

import ie.omk.smpp.message.SMPPPacket;
import ie.omk.smpp.util.DefaultSequenceScheme;
import ie.omk.smpp.util.SequenceNumberScheme;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collection;

import net.gescobar.smppserver.util.SocketLink;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>An SMPP Server that accepts client connections and process SMPP packets. Every time a connection is accepted,
 * a new {@link SmppSession} object is created to handle the packets from that connection.</p>
 * 
 * <p>Starting the SMPP Server is as simple as instantiating this class and calling the {@link #start()} method:</p>
 * 
 * <code>
 * 	SmppServer server = new SmppServer();
 * 	server.start();
 * 	...
 * 
 *  // somewhere else
 *  server.stop();
 * </code>
 * 
 * <p>To process the SMPP packets you will need to provide an implementation of the {@link PacketProcessor} interface
 * using the constructor {@link #SmppServer(int, PacketProcessor)} or the setter {@link #setPacketProcessor(PacketProcessor)}.
 * If no {@link PacketProcessor} is specified, a default implementation that always returns 0 (ESME_ROK in the SMPP 
 * specification) is used.</p>
 * 
 * @author German Escobar
 */
public class SmppServer {
	
	private Logger log = LoggerFactory.getLogger(SmppServer.class);
	
	/**
	 * Possible values for the status of the server.
	 * 
	 * @author German Escobar
	 */
	public enum Status {
		
		/**
		 * The server is stopped. This is the initial state by the way.
		 */
		STOPPED,
		
		/**
		 * The server is stopping.
		 */
		STOPPING,
		
		/**
		 * The server is starting.
		 */
		STARTING,
		
		/**
		 * The server has started.
		 */
		STARTED;
		
	}

	/**
	 * The port in which we are going to listen the connections.
	 */
	private int port;
	
	/**
	 * The status of the server
	 */
	private Status status = Status.STOPPED;
	
	/**
	 * The class that will process the SMPP messages.
	 */
	private PacketProcessor packetProcessor;
	
	/**
	 * The sequence number scheme used for delivering request to the client
	 */
	private SequenceNumberScheme sequenceNumberScheme;
	
	/**
	 * The created sessions.
	 */
	private Collection<SmppSession> sessions = new ArrayList<SmppSession>();
	
	/**
	 * Constructor. Creates an instance with the specified port and default {@link PacketProcessor} and 
	 * {@link SequenceNumberScheme} implementations.
	 * 
	 * @param port the server will accept connections in this port.
	 */
	public SmppServer(int port) {
		this(port, new PacketProcessor() {

			@Override
			public void processPacket(SMPPPacket packet, Response response) {
				response.setCommandStatus(CommandStatus.OK).send();
			}
			
		});
	}
	
	/**
	 * Constructor. Creates an instance with the specified port and {@link PacketProcessor} implementation. A
	 * default {@link SequenceNumberScheme} implementation is used.
	 * 
	 * @param port the server will accept connections in this port. 
	 * @param packetProcessor the {@link PacketProcessor} implementation that will process the SMPP messages.
	 */
	public SmppServer(int port, PacketProcessor packetProcessor) {
		this(port, packetProcessor, new DefaultSequenceScheme());
	}
	
	/**
	 * Constructor. Creates an instance with the specified port, {@link PacketProcessor} implementation and 
	 * {@link SequenceNumberScheme} implementation. 
	 * 
	 * @param port the server will accept connections in this port. 
	 * @param packetProcessor the {@link PacketProcessor} implementation that will process the SMPP messages.
	 * @param sequenceNumberScheme the {@link SequenceNumberScheme} implementation used to send requests to the 
	 * client.
	 */
	public SmppServer(int port, PacketProcessor packetProcessor, SequenceNumberScheme sequenceNumberScheme) {
		this.port = port;
		this.packetProcessor = packetProcessor;
		this.sequenceNumberScheme = sequenceNumberScheme;
	}

	/**
	 * Starts listening to client connections through the specified port.
	 * 
	 * @throws IOException if an I/O error occurs when opening the socket.
	 */
	public void start() throws IOException {
		
		log.debug("starting the SMPP Server ... ");
		this.status = Status.STARTING;
		
		// open the socket
		ServerSocket serverSocket = new ServerSocket(port);
		serverSocket.setSoTimeout(500);
		
		// start the thread that will accept the connections
		new Thread(new ConnectionAcceptor(serverSocket)).start();
		
		log.info("<< SMPP Server running on port " + port + " >>");
		this.status = Status.STARTED;
	}
	
	/**
	 * Stops the server gracefully.
	 */
	public void stop() {
		
		// this will signal the ConnectionThread to stop accepting connections
		log.debug("stopping the SMPP Server ... ");
		this.status = Status.STOPPING;
		
	}
	
	/**
	 * @return the status of the server.
	 */
	public Status getStatus() {
		return status;
	}
	
	/**
	 * Returns the opened sessions.
	 * 
	 * @return a collection of Session objects.
	 */
	public Collection<SmppSession> getSessions() {
		return sessions;
	}
	
	/**
	 * Sets the packet processor that will be used for new sessions. Old sessions will not be affected. 
	 * 
	 * @param packetProcessor the {@link PacketProcessor} implementation to be used.
	 */
	public void setPacketProcessor(PacketProcessor packetProcessor) {
		this.packetProcessor = packetProcessor;
	}
	
	/**
	 * The thread that will accept the connections and create the sessions.
	 * 
	 * @author German Escobar
	 */
	private class ConnectionAcceptor implements Runnable {
		
		private ServerSocket serverSocket;
		
		public ConnectionAcceptor(ServerSocket serverSocket) {
			this.serverSocket = serverSocket;
		}
		
		@Override
		public void run() {
			
			try {
				
				// keep running while the server is starting or started
				while (status.equals(Status.STARTING) || status.equals(Status.STARTED)) {
					
					Socket socket = null;

					try { socket = serverSocket.accept(); } catch (SocketTimeoutException e) {}

					// check if we have received a connection
					if (socket != null) {
						
						log.info("new connection accepted from " + socket.getRemoteSocketAddress());
						
						// create the session
						SocketLink link = new SocketLink(socket);
						SmppSession session = new SmppSession(link, packetProcessor, sequenceNumberScheme);
						
						// add the session to the collection
						sessions.add(session);
						
					}
				}
				
			} catch (IOException e) {
				log.error("IOException while acceping connections: " + e.getMessage(), e);
			}
			
			// close the socket
			try { serverSocket.close(); } catch (Exception e) {}
			
			// close all the sessions
			for (SmppSession session : sessions) {
				try { session.close(); } catch (Exception e) {}
			}
			
			// the server has stopped
			status = Status.STOPPED;
			log.info("<< SMPP Server stopped >>");
			
		}
		
	}
	
}
