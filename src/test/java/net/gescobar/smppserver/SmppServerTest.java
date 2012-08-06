package net.gescobar.smppserver;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.DataInputStream;
import java.net.Socket;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.Executors;

import net.gescobar.smppserver.SmppSession.BindType;
import net.gescobar.smppserver.packet.DeliverSm;
import net.gescobar.smppserver.packet.EnquireLink;
import net.gescobar.smppserver.packet.SmppRequest;
import net.gescobar.smppserver.packet.SmppResponse;
import net.gescobar.smppserver.packet.SubmitSm;
import net.gescobar.smppserver.packet.Unbind;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.cloudhopper.commons.util.windowing.WindowFuture;
import com.cloudhopper.smpp.SmppBindType;
import com.cloudhopper.smpp.SmppConstants;
import com.cloudhopper.smpp.SmppSessionConfiguration;
import com.cloudhopper.smpp.SmppSessionHandler;
import com.cloudhopper.smpp.impl.DefaultSmppClient;
import com.cloudhopper.smpp.pdu.BindTransceiver;
import com.cloudhopper.smpp.pdu.BindTransceiverResp;
import com.cloudhopper.smpp.pdu.PduRequest;
import com.cloudhopper.smpp.pdu.PduResponse;
import com.cloudhopper.smpp.pdu.SubmitSmResp;
import com.cloudhopper.smpp.pdu.UnbindResp;
import com.cloudhopper.smpp.transcoder.DefaultPduTranscoder;
import com.cloudhopper.smpp.transcoder.DefaultPduTranscoderContext;
import com.cloudhopper.smpp.transcoder.PduTranscoder;

/**
 * 
 * @author German Escobar
 */
public class SmppServerTest {
	
	private final int PORT = 4444;
	
	private final long DEFAULT_TIMEOUT = 2000;
	
	@Test
	public void shouldCreateTranscieverSession() throws Exception {
		shouldCreateSession(BindType.TRANSCIEVER);
	}
	
	@Test
	public void shouldCreateReceiverSession() throws Exception {
		shouldCreateSession(BindType.RECEIVER);
	}
	
	@Test
	public void shoudlCreateTransmitterSession() throws Exception {
		shouldCreateSession(BindType.TRANSMITTER);
	}
	
	private void shouldCreateSession(BindType bindType) throws Exception {
		
		// start the SMPP Server
		SmppServer smppServer = new SmppServer(PORT);
		smppServer.start();
			
		try {
			SmppBindType chBindType = SmppBindType.TRANSCEIVER;
			if (BindType.RECEIVER.equals(bindType)) {
				chBindType = SmppBindType.RECEIVER;
			} else if (BindType.TRANSMITTER.equals(bindType)) {
				chBindType = SmppBindType.TRANSMITTER;
			}
				
			bind(chBindType);
					
			assertSessionsCreated(smppServer, 1, DEFAULT_TIMEOUT);
					
			Collection<SmppSession> sessions = smppServer.getSessions();
			Assert.assertNotNull(sessions);
			Assert.assertEquals(sessions.size(), 1);
					
			SmppSession session = sessions.iterator().next();
			Assert.assertNotNull(session);
			Assert.assertEquals(session.getBindType(), bindType);
			
		} finally {	
			stopServer(smppServer, 1000);
		}
		
	}
	
	@Test
	public void shouldAcceptMultipleConnections() throws Exception {
		
		// start the SMPP Server
		SmppServer smppServer = new SmppServer(PORT);
		smppServer.start();
		
		try {
			
			for (int i=0; i < 200; i++) {
				bind(SmppBindType.TRANSCEIVER);
			}
			
			assertSessionsCreated(smppServer, 200, 5000);
			
		} finally {
			stopServer(smppServer, 1000);
		}
		
	}
	
	@Test(dependsOnMethods="shouldCreateTranscieverSession")
	public void shouldSetCustomMessageId() throws Exception {
		
		SmppServer smppServer = new SmppServer(PORT, new PacketProcessor() {

			@Override
			public void processPacket(SmppRequest packet, ResponseSender responseSender) {
				
				if (SubmitSm.class.isInstance(packet)) {
					responseSender.send( Response.OK.withMessageId("12000") );
					return;
				}
				
				responseSender.send( Response.OK );
				
			}
			
		});
		
		smppServer.start();
		
		try {
			
			com.cloudhopper.smpp.SmppSession client = bind(SmppBindType.TRANSCEIVER);
			com.cloudhopper.smpp.pdu.SubmitSm submitSm = new com.cloudhopper.smpp.pdu.SubmitSm();
			SubmitSmResp submitSmResp = client.submit(submitSm, DEFAULT_TIMEOUT);
			
			Assert.assertNotNull( submitSmResp );
			Assert.assertEquals( submitSmResp.getMessageId(), "12000" );
			
		} finally {
			stopServer(smppServer, 1000);
		}
		
	}
	
	@Test
	public void shouldSendRequestToClient() throws Exception {
		
		SmppServer smppServer = new SmppServer(PORT);
		smppServer.start();
		
		// we need to set the same sequence number for the request/response packet
		int sequenceNumber = 234;
		
		try {
			
			// this is the response packet
			com.cloudhopper.smpp.pdu.DeliverSmResp deliverSmResp = new com.cloudhopper.smpp.pdu.DeliverSmResp();
			deliverSmResp.setSequenceNumber(sequenceNumber);
			deliverSmResp.setCommandStatus( SmppConstants.STATUS_OK );
			
			// mock the SmppSessionHandler and configure the return packet
			SmppSessionHandler sessionHandler = mock(SmppSessionHandler.class);
			when(sessionHandler.firePduRequestReceived(any(PduRequest.class))).thenReturn(deliverSmResp);
			
			// bind and wait until the session is created
			bind(SmppBindType.TRANSCEIVER, sessionHandler);
			assertSessionsCreated(smppServer, 1, DEFAULT_TIMEOUT);
			
			// retrieve the session
			SmppSession smppSession = smppServer.getSessions().iterator().next();
			
			// send the request
			DeliverSm deliverSm = new DeliverSm();
			deliverSm.setSequenceNumber(sequenceNumber);
			SmppResponse response = smppSession.sendRequest(deliverSm, DEFAULT_TIMEOUT);
			
			// validate
			verify(sessionHandler, timeout(1000)).firePduRequestReceived(any(PduRequest.class));
			
			Assert.assertNotNull(response);
			Assert.assertEquals( response.getCommandStatus(), Response.OK.getCommandStatus() );
			
			
		} finally {
			stopServer(smppServer, 1000);
		}
		
	}
	
	@Test(expectedExceptions=IllegalArgumentException.class)
	public void shouldFailSendingNullPacketToClient() throws Exception {
		
		SmppServer smppServer = new SmppServer(PORT);
		smppServer.start();
		
		try {
			// bind and wait until the session is created
			bind(SmppBindType.TRANSCEIVER);
			assertSessionsCreated(smppServer, 1, 500);
						
			// retrieve the session
			SmppSession smppSession = smppServer.getSessions().iterator().next();
			smppSession.sendRequest(null, 500);
			
		} finally {
			stopServer(smppServer, 1000);
		}
		
	}
	
	@Test(expectedExceptions=IllegalStateException.class)
	public void shouldFailSendingPacketToClientWhileNotBound() throws Exception {
		
		SmppServer smppServer = new SmppServer(PORT);
		smppServer.start();
		
		try {
			new Socket("localhost", PORT);
			
			assertSessionsCreated(smppServer, 1, DEFAULT_TIMEOUT);
			
			// retrieve the session
			SmppSession smppSession = smppServer.getSessions().iterator().next();
			smppSession.sendRequest(new EnquireLink(), DEFAULT_TIMEOUT);
			
		} finally {
			stopServer(smppServer, 1000);
		}
		
	}
	
	@SuppressWarnings("rawtypes")
	@Test
	public void shouldFailSecondBind() throws Exception {
		
		SmppServer smppServer = new SmppServer(PORT);
		smppServer.start();
		
		try {
			// bind and wait until the session is created
			com.cloudhopper.smpp.SmppSession client = bind(SmppBindType.TRANSCEIVER);
			assertSessionsCreated(smppServer, 1, DEFAULT_TIMEOUT);
						
			WindowFuture<Integer,PduRequest,PduResponse> future = client.sendRequestPdu(new BindTransceiver(), 1000, true);
			
			future.await(1000);
			BindTransceiverResp response = (BindTransceiverResp) future.getResponse();
			
			Assert.assertNotNull( response );
			Assert.assertEquals( response.getCommandStatus(), SmppConstants.STATUS_ALYBND );
			
		} finally {
			stopServer(smppServer, 1000);
		}
		
	}
	
	@Test
	public void shouldCloseConnectionOnClientUnbind() throws Exception {
		
		SmppServer smppServer = new SmppServer(PORT);
		smppServer.start();
		
		try {
			// bind and check that a session was created
			com.cloudhopper.smpp.SmppSession client = bind(SmppBindType.TRANSCEIVER);
			assertSessionsCreated(smppServer, 1, DEFAULT_TIMEOUT);
			
			SmppSession session = smppServer.getSessions().iterator().next();
			
			client.unbind(1000);
			Assert.assertEquals( smppServer.getSessions().size(), 0 );
			Assert.assertFalse( session.isBound() );
			
		} finally {
			smppServer.stop();
		}
	}
	
	@Test
	public void shouldCloseConnectionOnServerUnbind() throws Exception {
		
		SmppServer smppServer = new SmppServer(PORT);
		smppServer.start();
		
		try {
			
			int sequenceNumber = 482;
			
			// mock the SmppSessionHandler and configure the return packet
			SmppSessionHandler sessionHandler = mock(SmppSessionHandler.class);
			UnbindResp unbindResp = new UnbindResp();
			unbindResp.setSequenceNumber(sequenceNumber);
			when(sessionHandler.firePduRequestReceived(any(PduRequest.class))).thenReturn(unbindResp);
			
			// bind and check that a session was created
			bind(SmppBindType.TRANSCEIVER, sessionHandler);
			assertSessionsCreated(smppServer, 1, DEFAULT_TIMEOUT);
			
			SmppSession session = smppServer.getSessions().iterator().next();
			Unbind unbind = new Unbind();
			unbind.setSequenceNumber(sequenceNumber);
			session.sendRequest(unbind, 500);
			
			assertSessionsCreated(smppServer, 0, DEFAULT_TIMEOUT);
			Assert.assertEquals(smppServer.getSessions().size(), 0);
			Assert.assertFalse( session.isBound() );
			
		} finally {
			stopServer(smppServer, 1000);
		}
		
	}
	
	@Test
	public void shouldFailCommandBeforeBind() throws Exception {
		
		SmppServer smppServer = new SmppServer(PORT, new PacketProcessor() {

			@Override
			public void processPacket(SmppRequest packet, ResponseSender responseSender) {
				if (packet.isBind()) {
					responseSender.send( Response.INVALID_PASSWORD );
					return;
				}
				
				responseSender.send( Response.OK );
				
			}
			
		});
		smppServer.start();
		
		try {
			
			// open the socket
			Socket socket = new Socket("localhost", PORT);
			
			// write the submit_sm using the built in PduTranscoder of cloudhopper
			PduTranscoder transcoder = new DefaultPduTranscoder(new DefaultPduTranscoderContext());
			socket.getOutputStream().write(transcoder.encode(new com.cloudhopper.smpp.pdu.SubmitSm()).array());
			
			// read the packet ... this is as low level as it can get
			DataInputStream input = new DataInputStream(socket.getInputStream());
			input.readInt(); // command length
			int commandId = input.readInt();
			int commandStatus = input.readInt();
			
			Assert.assertEquals( commandId, SmppConstants.CMD_ID_SUBMIT_SM_RESP );
			Assert.assertEquals( commandStatus, SmppConstants.STATUS_INVBNDSTS );
			
			socket.close();
			
		} finally {
			stopServer(smppServer, 1000);
		}
		
	}
	
	private com.cloudhopper.smpp.SmppSession bind(SmppBindType bindType) throws Exception {
		return bind(bindType, null);
	}
	
	private com.cloudhopper.smpp.SmppSession bind(SmppBindType bindType, SmppSessionHandler sessionHandler) 
			throws Exception {
		
		DefaultSmppClient clientBootstrap = new DefaultSmppClient(Executors.newCachedThreadPool(), 1, null);
		
		SmppSessionConfiguration config = new SmppSessionConfiguration();
		config.setHost("localhost");
		config.setPort(PORT);
		config.setType(bindType);
		
		return clientBootstrap.bind(config, sessionHandler);
		
	}
	
	private void assertSessionsCreated(SmppServer smppServer, int numSessions, long timeout) {
		
		boolean asserted = false;
		long startTime = new Date().getTime();
		
		while (!asserted && (new Date().getTime() - startTime) < timeout) {
			if (smppServer.getSessions().size() == numSessions) {
				asserted = true;
			} else {
				try { Thread.sleep(200); } catch (InterruptedException e) {}
			}
			
		}
		
		Assert.assertEquals(smppServer.getSessions().size(), numSessions);
	}
	
	private void stopServer(SmppServer server, long timeout) {
		
		server.stop();
		
		boolean stopped = false;
		long startTime = new Date().getTime();
		
		while (!stopped && (new Date().getTime() - startTime) < timeout) {
			if (server.getStatus().equals(SmppServer.Status.STOPPED)) {
				stopped = true;
			} else {
				try { Thread.sleep(200); } catch (InterruptedException e) {}
			}
		
		}
		
		Assert.assertEquals(server.getStatus(), SmppServer.Status.STOPPED);
	}
	
}
