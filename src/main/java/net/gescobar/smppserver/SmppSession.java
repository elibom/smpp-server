package net.gescobar.smppserver;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import net.gescobar.smppserver.packet.SmppPacket;
import net.gescobar.smppserver.packet.SmppRequest;
import net.gescobar.smppserver.packet.SmppResponse;
import net.gescobar.smppserver.packet.ch.PacketMapper;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cloudhopper.commons.util.windowing.Window;
import com.cloudhopper.commons.util.windowing.WindowFuture;
import com.cloudhopper.smpp.SmppConstants;
import com.cloudhopper.smpp.pdu.BaseBind;
import com.cloudhopper.smpp.pdu.Pdu;
import com.cloudhopper.smpp.pdu.PduRequest;
import com.cloudhopper.smpp.pdu.PduResponse;
import com.cloudhopper.smpp.pdu.SubmitSmResp;
import com.cloudhopper.smpp.tlv.Tlv;
import com.cloudhopper.smpp.transcoder.DefaultPduTranscoder;
import com.cloudhopper.smpp.transcoder.DefaultPduTranscoderContext;
import com.cloudhopper.smpp.transcoder.PduTranscoder;
import com.cloudhopper.smpp.type.SmppChannelException;

/**
 * 
 * 
 * @author German Escobar
 */
public class SmppSession extends SimpleChannelHandler {
	
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
		 * The connection is being closed
		 */
		CLOSING,

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
	
	private Channel channel;
	
	/**
	 * The class that will process the SMPP messages.
	 */
	private PacketProcessor packetProcessor;
	
	private PduTranscoder transcoder;
	
	/**
	 * Used to set the sequence number to packets sent to clients
	 */
	private AtomicInteger sequenceId = new AtomicInteger(0);
	
	/**
	 * Reusing the cloudhopper window mechanism to handle the response of packets sent through the 
	 * {@link #sendRequest(SmppRequest)} method.
	 */
	@SuppressWarnings("rawtypes")
	private final Window<Integer,PduRequest,PduResponse> sendWindow = 
			new Window<Integer,PduRequest,PduResponse>(10);
	
	/**
	 * Constructor.
	 * 
	 * @param channel
	 * @param packetProcessor
	 */
	public SmppSession(Channel channel, PacketProcessor packetProcessor) {
		
		if (channel == null) {
			throw new IllegalArgumentException("no channel specified");
		}
		
		if (packetProcessor == null) {
			throw new IllegalArgumentException("no packetProcessor specified");
		}
		
		this.channel = channel;
		this.packetProcessor = packetProcessor;
		this.transcoder = new DefaultPduTranscoder(new DefaultPduTranscoderContext());
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		
		Pdu pdu = (Pdu) e.getMessage();
		
		// handle responses
		if (pdu.isResponse()) {
			PduResponse pduResponse = (PduResponse) pdu;
			this.sendWindow.complete(pduResponse.getSequenceNumber(), pduResponse);
			
			return;
		}
		
		// if packet is a bind request and session is already bound, respond with error
		if (BaseBind.class.isInstance(pdu) && status.equals(Status.BOUND)) {
			
			log.warn("session with system id " + systemId + " is already bound");
			
			PduResponse response = createResponse((PduRequest) pdu, Response.ALREADY_BOUND);
			sendResponse(response);
			
			return;
		}
		
		// if not a bind packet and session is not bound, respond with error
		if (!BaseBind.class.isInstance(pdu) && !status.equals(Status.BOUND)) {
			
			PduResponse response = createResponse((PduRequest) pdu, Response.INVALID_BIND_STATUS);
			sendResponse(response);
			
			return;
		}
		
		log.debug("[" + systemId + "] received request PDU: " + pdu);
		
		ResponseSender responseSender = new OnlyOnceResponse( (PduRequest) pdu );

   	 	try {
   	 		packetProcessor.processPacket( (SmppRequest) PacketMapper.map((PduRequest) pdu), responseSender );
   	 	} catch (Exception f) {
   	 		log.error("Exception calling the packet processor: " + f.getMessage(), f);
   	 	}

		
	}
	
	/**
	 * Closes the socket and stops the receiving thread.
	 * 
	 * @throws IOException if there is a problem closing the socket.
	 */
	public void close() {

		if (this.status == Status.BOUND) {

			// TODO maybe we should send an unbind request first?

			this.status = Status.CLOSING;
			

		}
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
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public SmppResponse sendRequest(SmppRequest packet) throws SmppException {
		
		if (packet == null) {
			throw new IllegalArgumentException("No packet specified");
		}
		
		// send requests only if already bound
		if (!status.equals(Status.BOUND)) {
			throw new IllegalStateException("The session is not bound.");
		}
		
		// only some packets can be sent to the client
		if (packet.getCommandId() != SmppPacket.DELIVER_SM &&
				packet.getCommandId() != SmppPacket.ENQUIRE_LINK) {
			throw new IllegalArgumentException("Not allowed to send this packet to the client. Possible packets: " +
					"deliver_sm, enquire_link");
		}
		
		// set the sequence number if not assigned
		if (packet.getSequenceNumber() == -1) {
			packet.setSequenceNumber( sequenceId.incrementAndGet() );
		}
		
		try {
			PduRequest pdu = (PduRequest) PacketMapper.map(packet);
			
			// encode the pdu into a buffer
	        ChannelBuffer buffer = transcoder.encode(pdu);
	        
	        WindowFuture<Integer,PduRequest,PduResponse> future = null;
	        try {
	            future = sendWindow.offer(pdu.getSequenceNumber(), pdu, 30000, 60000, true);
	        } catch (Exception e) {
	        	throw new SmppException(e);
	        }
	        
	        ChannelFuture channelFuture = this.channel.write(buffer).await();
	        
	        // check if the write was a success
	        if (!channelFuture.isSuccess()) {
	            // the write failed, make sure to throw an exception
	            throw new SmppChannelException(channelFuture.getCause().getMessage(), channelFuture.getCause());
	        }
	        
	        future.await(15000);
	        return (SmppResponse) PacketMapper.map( future.getResponse() );
	        
		} catch (Exception e) {
			throw new SmppException(e);
		}
		
	}
	
	@SuppressWarnings("rawtypes")
	private PduResponse createResponse(PduRequest request, Response response) {
		
		PduResponse pduResponse = request.createResponse();
		pduResponse.setCommandStatus( response.getCommandStatus() );
		return pduResponse;
		
	}
	
	private void sendResponse(PduResponse response) {
		
		try {
			
			// encode the pdu into a buffer
	        ChannelBuffer buffer = transcoder.encode(response);
	
	        // always log the PDU
	        log.info("[" + systemId + "] sending response PDU: {}", response);
	
	        // write the pdu out & wait till its written
	        ChannelFuture channelFuture = this.channel.write(buffer).await();
	
	        // check if the write was a success
	        if (!channelFuture.isSuccess()) {
	        	throw new SmppChannelException(channelFuture.getCause().getMessage(), channelFuture.getCause());
	        }
	        
		} catch (Exception e) {
			log.error("Fatal exception thrown while attempting to send response PDU: {}", e);
		}
	}
	
	/**
	 * This is the {@link ResponseSender} implementation that is passed to the 
	 * {@link PacketProcessor#processPacket(SMPPPacket, ResponseSender)} method. It checks that the response is sent 
	 * only once.
	 * 
	 * @author German Escobar
	 */
    private class OnlyOnceResponse implements ResponseSender {
    	
    	@SuppressWarnings("rawtypes")
		private PduRequest pduRequest;
    	
    	private boolean responseSent = false;
    	
    	@SuppressWarnings("rawtypes")
		public OnlyOnceResponse(PduRequest pduRequest) {
    		this.pduRequest = pduRequest;
    	}

		@SuppressWarnings("rawtypes")
		@Override
		public void send(Response response) {
			
			if (responseSent) {
				log.warn("response for this request was already sent to the client ... ignoring");
				return;
			}
			
			try {
				
				PduResponse pduResponse = createResponse(pduRequest, response);
				
				int commandId = pduRequest.getCommandId();
				int commandStatus = response.getCommandStatus();
				
				if (BaseBind.class.isInstance(pduRequest)) {
					
					if (commandStatus == Response.OK.getCommandStatus()) {
						
						status = Status.BOUND;

		   	 			if (commandId == SmppConstants.CMD_ID_BIND_RECEIVER) {
				   			bindType = BindType.RECEIVER;
				   		} else if (commandId == SmppConstants.CMD_ID_BIND_TRANSMITTER) {
				   			bindType = BindType.TRANSMITTER;
				   		} else if (commandId == SmppConstants.CMD_ID_BIND_TRANSCEIVER) {
				   			bindType = BindType.TRANSCIEVER;
				   		}

		   	 			BaseBind bind = (BaseBind) pduRequest;
		   	 			systemId = bind.getSystemId();
		   	 			
		   	 			// this is important to support tlv parameters
		   	 			pduResponse.addOptionalParameter( new Tlv(SmppConstants.TAG_SC_INTERFACE_VERSION, new byte[] { SmppConstants.VERSION_3_4 }) );
		   	 			
		   	 			log.info("[" + systemId + "] session created with bind type: " + bindType);
					}
					
				} else {
					
					if (commandId == SmppConstants.CMD_ID_SUBMIT_SM) {
						
						if (response.getMessageId() != null) {
							SubmitSmResp submitResp = (SubmitSmResp) pduResponse;
							submitResp.setMessageId( response.getMessageId() );
	   	 				}
		
					}
					
					// handle unbind request
					if (commandId == SmppConstants.CMD_ID_UNBIND) {
						status = Status.DEAD;
						/* TODO notify SmppSessionsManager that the connection is dead */
					}
					
				}
				
				SmppSession.this.sendResponse(pduResponse);
				
			} catch (Exception e) {
				log.error("Exception sending response: " + e.getMessage(), e);
			}
		}
    	
    }
	
}
