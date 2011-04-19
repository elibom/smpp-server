package net.gescobar.smppserver;

import ie.omk.smpp.Connection;
import ie.omk.smpp.event.ConnectionObserver;
import ie.omk.smpp.event.SMPPEvent;
import ie.omk.smpp.message.BindResp;
import ie.omk.smpp.message.SMPPPacket;
import ie.omk.smpp.net.TcpLink;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import net.gescobar.smppserver.SmppSession.BindType;
import net.gescobar.smppserver.SmppSession.Status;

import org.testng.Assert;
import org.testng.annotations.Test;

public class SmppServerTest {
	
	private final int PORT = 4444;
	private final long RECEIVE_TIMEOUT = 1000;

	@Test
	public void shouldCreateTransceiverSession() throws Exception {
		
		// start the SMPP Server
		SmppServer smppServer = new SmppServer(PORT);
		smppServer.start();
		
		Assert.assertNotNull(smppServer.getSessions());
		Assert.assertTrue(smppServer.getSessions().isEmpty());
		
		// open a connection
		TcpLink link = new TcpLink("localhost", PORT);
		link.open();
		Connection connection = new Connection(link, true);
		
		MessageListener messageListener = new MessageListener();
		connection.addObserver(messageListener);
		
		assertSessionCreated(smppServer, 500);
		
		SmppSession session = smppServer.getSessions().iterator().next();
		Assert.assertNotNull(session);
		Assert.assertEquals(session.getStatus(), Status.BOUND);
		
		// bind
		connection.bind(Connection.TRANSCEIVER, "test", null, null);
		BindResp bindResponse = messageListener.getBindResponse(RECEIVE_TIMEOUT);
		
		Assert.assertNotNull(bindResponse);
		Assert.assertEquals(bindResponse.getCommandStatus(), 0);
		
		session = smppServer.getSessions().iterator().next();
		Assert.assertNotNull(session);
		Assert.assertEquals(session.getStatus(), Status.BOUND);
		Assert.assertEquals(session.getBindType(), BindType.TRANSCIEVER);
		Assert.assertEquals(session.getSystemId(), "test");
		
		// unbind
		connection.unbind();
		
		
		
		smppServer.stop();
	}
	
	private void assertSessionCreated(SmppServer smppServer, long timeout) {
		
		boolean asserted = false;
		long startTime = new Date().getTime();
		
		while (!asserted && (new Date().getTime() - startTime) < timeout) {
			if (!smppServer.getSessions().isEmpty()) {
				Assert.assertEquals(smppServer.getSessions().size(), 1);
				asserted = true;
			}
			
			try { Thread.sleep(200); } catch (InterruptedException e) {}
		}
	}
	
	private class MessageListener implements ConnectionObserver {
		
		private Collection<SMPPPacket> packets = Collections.synchronizedList(new ArrayList<SMPPPacket>());

		@Override
		public void packetReceived(Connection source, SMPPPacket packet) {
			packets.add(packet);
		}

		@Override
		public void update(Connection source, SMPPEvent event) {}
		
		public BindResp getBindResponse(long timeout) {
			
			long startTime = new Date().getTime();
			
			while ((new Date().getTime() - startTime) < timeout) {
				
				for (SMPPPacket p : packets) {
					if (p.getCommandId() == SMPPPacket.BIND_RECEIVER_RESP 
							|| p.getCommandId() == SMPPPacket.BIND_TRANSMITTER_RESP
							|| p.getCommandId() == SMPPPacket.BIND_TRANSCEIVER_RESP)
						return (BindResp) p;
				}
				
				try { Thread.sleep(200); } catch (InterruptedException e) {}
			}
			
			return null;
		}
		
	}
	
}
