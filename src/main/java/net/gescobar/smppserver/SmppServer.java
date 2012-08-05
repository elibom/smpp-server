package net.gescobar.smppserver;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.concurrent.Executors;

import net.gescobar.smppserver.packet.SmppRequest;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelException;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cloudhopper.smpp.channel.SmppChannelConstants;
import com.cloudhopper.smpp.type.SmppChannelException;

/**
 * <p>An SMPP Server that accepts client connections and process SMPP packets. Every time a connection is accepted,
 * a new {@link SmppSession} object is created to handle the packets from that connection.</p>
 * 
 * <p>Starting the SMPP Server is as simple as instantiating this class and calling the {@link #start()} method:</p>
 * 
 * <pre>
 * 	SmppServer server = new SmppServer();
 * 	server.start();
 * 	...
 * 
 *  // somewhere else
 *  server.stop();
 * </pre>
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
	
	private ServerBootstrap serverBootstrap;
	
	private Channel serverChannel;
	
	private SmppSessionManager sessionsManager;
	
	/**
	 * Constructor. Creates an instance with the specified port and default {@link PacketProcessor} and 
	 * {@link SequenceNumberScheme} implementations.
	 * 
	 * @param port the server will accept connections in this port.
	 */
	public SmppServer(int port) {
		this(port, new PacketProcessor() {
			
			@Override
			public void processPacket(SmppRequest packet, ResponseSender responseSender) {
				responseSender.send( Response.OK );
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
		
		this.port = port;
		this.sessionsManager = new SmppSessionManager(packetProcessor);
		
		ChannelFactory channelFactory = new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), 
				Executors.newCachedThreadPool(), Runtime.getRuntime().availableProcessors() * 3);
		this.serverBootstrap = new ServerBootstrap(channelFactory);
		
		ChannelPipeline pipeline = serverBootstrap.getPipeline();
		pipeline.addLast(SmppChannelConstants.PIPELINE_SERVER_CONNECTOR_NAME, sessionsManager);
		
	}

	/**
	 * Starts listening to client connections through the specified port.
	 * 
	 * @throws IOException if an I/O error occurs when opening the socket.
	 */
	public void start() throws SmppChannelException {
		
		if (this.status != Status.STOPPED) {
			log.warn("can't start SMPP Server, current status is " + this.status);
			return;
		}
		
		log.debug("starting the SMPP Server ... ");
		this.status = Status.STARTING;
		
		try {
            this.serverChannel = this.serverBootstrap.bind( new InetSocketAddress(port) );
            log.info("SMPP Server started on SMPP port [{}]", port);
        } catch (ChannelException e) {
            throw new SmppChannelException(e.getMessage(), e);
        }
		
		log.info("<< SMPP Server running on port " + port + " >>");
		this.status = Status.STARTED;
	}
	
	/**
	 * Stops the server gracefully.
	 */
	public void stop() {
		
		if (this.status != Status.STARTED) {
			log.warn("can't stop SMPP Server, current status is " + this.status);
			return;
		}
		
		// this will signal the ConnectionThread to stop accepting connections
		log.debug("stopping the SMPP Server ... ");
		this.status = Status.STOPPING;
		
		sessionsManager.close();
		
        // clean up all external resources
        if (this.serverChannel != null) {
            this.serverChannel.close().awaitUninterruptibly();
            this.serverChannel = null;
        }
		
		// the server has stopped
		status = Status.STOPPED;
		log.info("<< SMPP Server stopped >>");
		
	}
	
	/**
	 * Returns the opened sessions.
	 * 
	 * @return a collection of Session objects.
	 */
	public Collection<SmppSession> getSessions() {
		return sessionsManager.getSessions();
	}
	
	/**
	 * @return the status of the server.
	 */
	public Status getStatus() {
		return status;
	}
	
	/**
	 * Sets the packet processor that will be used for new sessions. Old sessions will not be affected. 
	 * 
	 * @param packetProcessor the {@link PacketProcessor} implementation to be used.
	 */
	public void setPacketProcessor(PacketProcessor packetProcessor) {
		sessionsManager.setPacketProcessor(packetProcessor);
	}
	
}
