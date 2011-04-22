package net.gescobar.smppserver;

import ie.omk.smpp.BadCommandIDException;
import ie.omk.smpp.message.Bind;
import ie.omk.smpp.message.SMPPPacket;
import ie.omk.smpp.message.SMPPProtocolException;
import ie.omk.smpp.message.SMPPRequest;
import ie.omk.smpp.message.SMPPResponse;
import ie.omk.smpp.net.StreamLink;
import ie.omk.smpp.util.APIConfig;
import ie.omk.smpp.util.SMPPIO;

import java.io.IOException;

import net.gescobar.smppserver.PacketProcessor.ResponseStatus;
import net.gescobar.smppserver.util.PacketFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Represents an SMPP session with an SMPP client. When it receives an SMPP packet, it calls the 
 * {@link PacketProcessor#processPacket(SMPPPacket)} and responds with the returned value.</p>
 * 
 * <p>You can also send SMPP packets to the client using the ... </p>
 * 
 * @author German Escobar
 */
public class SmppSession {
	
	private Logger log = LoggerFactory.getLogger(SmppSession.class);
	
	/**
	 * Possible values for the status of the session.
	 * 
	 * @author German Escobar
	 */
	public enum Status {
		
		/**
		 * The connection is opened but the client hasn't tried to bind or has tried but unsuccessfully.
		 */
		IDLE,
		
		/**
		 * The connection is opened and the client is bound.
		 */
		BOUND,

		/**
		 * The connection is closed.
		 */
		DEAD;
	}
	
	/**
	 * Possible values for the bind type of the session.
	 * 
	 * @author German Escobar
	 */
	public enum BindType {

		TRANSMITTER,
		
		RECEIVER,
		
		TRANSCIEVER;
		
	}
	
	/**
	 * The status of the session.
	 */
	private Status status = Status.IDLE;
	
	/**
	 * The bind type of the session. Null if not bound.
	 */
	private BindType bindType;
	
	/**
	 * The systemId that was used to bind.
	 */
	private String systemId;

	/**
	 * The underlying link used to receive and send packets from and to the client.
	 */
	private StreamLink link;
	
	/**
	 * The class that will process the SMPP messages.
	 */
	private PacketProcessor packetProcessor;
	
	/**
	 * Constructor. Creates an instance with the specified link and the default {@link PacketProcessor} implementation.
	 * The link must be opened.
	 * 
	 * @param link the link used to to receive and send packets from and to the client.
	 */
	public SmppSession(StreamLink link) {
		this(link, new PacketProcessor() {

			@Override
			public ResponseStatus processPacket(SMPPPacket packet) {
				return ResponseStatus.OK;
			}
			
		});
	}
	
	/**
	 * Constructor. Creates an instance with the specified link and {@link PacketProcessor} implementation.
	 * The link must be opened.
	 * 
	 * @param link the link used to to receive and send packets from and to the client.
	 * @param packetProcessor the {@link PacketProcessor} implementation that will process the SMPP messages.
	 */
	public SmppSession(StreamLink link, PacketProcessor packetProcessor) {
		this.link = link;
		this.packetProcessor = packetProcessor;
		
		try {
			link.open();
		} catch (IOException e) {
			throw new RuntimeException("Should not have happened as the streams are already open!", e);
		}
		
		// start the thread that will receive the packets
		new ReceiverThread().start();
	}
	
	/**
	 * Sends an SMPP request to the client.
	 * 
	 * @param request the request packet to send to the client.
	 * @throws IllegalStateException if the session is not bound.
	 * @throws IOException if an I/O error occurs while writing the request packet to the network connection.
	 */
	public void sendRequest(SMPPRequest request) throws IllegalStateException, IOException {
		
		if (!status.equals(Status.BOUND)) {
			throw new IllegalStateException("The session is not bound.");
		}
		
		link.write(request, true);
	}

	/**
	 * @return the status of the session.
	 */
	public Status getStatus() {
		return status;
	}

	/**
	 * Tells if the session was bound in transceiver, receiver or transmitter mode. 
	 * 
	 * @return the bind type of the session.
	 * @throws IllegalStateException if the session is not bound.
	 */
	public BindType getBindType() throws IllegalStateException {
		
		if (!status.equals(Status.BOUND)) {
			throw new IllegalStateException("The session is not bound.");
		}
		
		return bindType;
	}

	/**
	 * @return the system id which was used by the client to bind the session.
	 * @throws IllegalStateException if the session is not bound.
	 */
	public String getSystemId() throws IllegalStateException {
		
		if (!status.equals(Status.BOUND)) {
			throw new IllegalStateException("The session is not bound.");
		}
		
		return systemId;
	}
	
	/**
	 * Sets the packet processor that will be used to process the packets.
	 * 
	 * @param packetProcessor the {@link PacketProcessor} implementation to be used.
	 */
	public void setPacketProcessor(PacketProcessor packetProcessor) {
		this.packetProcessor = packetProcessor;
	}
	
	/**
	 * @return the {@link PacketProcessor} implementation that is being used in this session.
	 */
	public PacketProcessor getPacketProcessor() {
		return packetProcessor;
	}
    
	/**
	 * Thread that receives and process SMPP packets from the client.
	 * 
	 * @author German Escobar
	 */
    private class ReceiverThread extends Thread {
    	
    	/**
    	 * Helper class to create SMPP packets.
    	 */
    	private PacketFactory packetFactory = new PacketFactory();

		@Override
		public void run() {
			
	        try {
	            receiveAndProcessPackets();
	        } catch (Exception x) {
	            log.error("Exception while receiving packets: " + x.getMessage(), x);
	        }
	        
	        // TODO don't we need to close the link here? or somewhere else?
	        
	        status = Status.DEAD;
	        
		}
		
		/**
		 * Helper method that will read the incoming packets. 
		 * 
		 * @throws IOException if there is a problem receiving the packets. 
		 */
		private void receiveAndProcessPackets() throws IOException {
	       
			int ioExceptions = 0;
	        final int ioExceptionLimit = APIConfig.getInstance().getInt(APIConfig.TOO_MANY_IO_EXCEPTIONS, 5);
	        
	        // read packets!
	        while (true) {
	            
	        	try {
	        		// read a packet
	                SMPPPacket packet = readNextPacket();
	                if (packet == null) {
	                    continue;
	                }
	                
	                // process it
	                processPacket(packet);
	                
	                ioExceptions = 0;
	                
	            } catch (IOException x) {
	            	
	            	// increase the exceptions and check if we have exceeded the limit
	                ioExceptions++;
	                if (ioExceptions >= ioExceptionLimit) {
	                    throw x;
	                }
	                
	                log.error("Exception receiving packet: " + x.getMessage(), x);
	            }
	        }

	    }
	    
		/**
		 * Helper method that will try to read one packet.
		 * 
		 * @return the SMPP packet that was received.
		 * @throws IOException if there is a problem reading the packet.
		 */
	    private SMPPPacket readNextPacket() throws IOException {
	    	try {
	            SMPPPacket pak = null;
	            int id = -1;

	            byte[] buff = new byte[300];
	            buff = link.read(buff);
	            id = SMPPIO.bytesToInt(buff, 4, 4);
	            pak = packetFactory.newInstance(id);

	            if (pak != null) {
	                pak.readFrom(buff, 0);
	            }
	            return pak;
	        } catch (BadCommandIDException x) {
	            throw new SMPPProtocolException("Unrecognised command received", x);
	        }
	    }
	    
	    /**
	     * Helper method that will process the packet.
	     * 
	     * @param packet the packet to the processed.
	     * @throws IOException if there is a problem writing the response
	     */
	    private void processPacket(SMPPPacket packet) throws IOException {
	   	 	log.debug("received packet: " + packet);
	   	 
	   	 	if (packet.isRequest()) {
	   		 
	   	 		// call the handler
	   	 		ResponseStatus responseStatus = null;
	   	 		try {
	   	 			responseStatus = packetProcessor.processPacket(packet);
	   	 			log.debug("packet processor returned: " + responseStatus);
	   	 		} catch (Exception e) {
	   	 			log.error("Exception calling the packet processor: " + e.getMessage(), e);
	   	 			responseStatus = ResponseStatus.SYSTEM_ERROR;
	   	 		}
	   	 		
	   	 		// create the response
	   	 		SMPPResponse response = null;
	   	 		try {
	   	 			response = (SMPPResponse) packetFactory.newResponse(packet);
	   	 		} catch (BadCommandIDException e) {
	   	 			throw new SMPPProtocolException("Unrecognised command received", e);
	   	 		}
	   	 		
	   	 		// set the command status
	   	 		response.setCommandStatus(responseStatus.getCommandStatus());
	   		 
	   	 		// bind is a special request
	   	 		if (packet.getCommandId() == SMPPPacket.BIND_RECEIVER
	   	 				|| packet.getCommandId() == SMPPPacket.BIND_TRANSCEIVER
	   	 				|| packet.getCommandId() == SMPPPacket.BIND_TRANSMITTER) {
	   	 			
	   	 			if (status.equals(Status.BOUND)) {
	   	 				
	   	 				// can't bind more than once
	   	 				log.warn("session with system id " + systemId + " is already bound");
	   	 				response.setCommandStatus(ResponseStatus.ALREADY_BOUND.getCommandStatus());
	   	 				
	   	 			} else {
	   			 
		   	 			if (responseStatus.equals(ResponseStatus.OK)) {
		   	 				Bind bind = (Bind) packet;
			   			 
		   	 				status = Status.BOUND;
			   			 
		   	 				if (packet.getCommandId() == SMPPPacket.BIND_RECEIVER) {
				   				bindType = BindType.RECEIVER;
				   			} else if (packet.getCommandId() == SMPPPacket.BIND_TRANSMITTER) {
				   				bindType = BindType.TRANSMITTER;
				   			} else if (packet.getCommandId() == SMPPPacket.BIND_TRANSCEIVER) {
				   				bindType = BindType.TRANSCIEVER;
				   			}
			   			 
		   	 				systemId = bind.getSystemId();
		   	 			}
		   	 			
	   	 			}
	   	 		} else {
	   	 			
	   	 			// for every other packet, we need to be bound
		   	 		if (!status.equals(Status.BOUND)) {
	   	 				response.setCommandStatus(ResponseStatus.INVALID_BIND_STATUS.getCommandStatus());
	   	 			} else {
		   	 		
	   	 				// handle unbind request
		   	 			if (packet.getCommandId() == SMPPPacket.UNBIND) {
		   	 				status = Status.DEAD;
		   	 			}
		   	 			
	   	 			}
	   	 		}
	   	 			
	   	 		// send the response
	   	 		link.write(response, false);
	   	 	}
	    }
    	
    }

}
