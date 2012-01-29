package net.gescobar.smppserver;

import ie.omk.smpp.BadCommandIDException;
import ie.omk.smpp.Connection;
import ie.omk.smpp.message.Bind;
import ie.omk.smpp.message.BindResp;
import ie.omk.smpp.message.BindTransceiver;
import ie.omk.smpp.message.DeliverSM;
import ie.omk.smpp.message.SMPPPacket;
import ie.omk.smpp.message.SubmitSM;
import ie.omk.smpp.net.StreamLink;
import ie.omk.smpp.util.PacketFactory;
import ie.omk.smpp.util.SMPPIO;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import net.gescobar.smppserver.PacketProcessor.Response;
import net.gescobar.smppserver.SmppSession.BindType;
import net.gescobar.smppserver.SmppSession.Status;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * 
 * @author German Escobar
 */
public class SmppSessionTest {

	@Test
	public void shouldCreateTranscieverSession() throws Exception {
		shouldCreateSessionHelper(Connection.TRANSMITTER);
	}
	
	@Test
	public void shouldCreateReceiverSession() throws Exception {
		shouldCreateSessionHelper(Connection.RECEIVER);
	}
	
	@Test
	public void shouldCreateTransmitterSession() throws Exception {
		shouldCreateSessionHelper(Connection.TRANSMITTER);
	}
	
	private void shouldCreateSessionHelper(int bindType) throws Exception {
		
		// create the session and client
		PeerFactory peerFactory = new PeerFactory();
		SmppSession smppSession = peerFactory.getSmppSession();
		Connection smppClient = peerFactory.getSmppClient();
		
		// bind client
		BindResp bindResponse = smppClient.bind(bindType, "test", null, null);
		Assert.assertNotNull(bindResponse);
		Assert.assertEquals(bindResponse.getCommandStatus(), Response.OK.getCommandStatus());
		
		// check the session
		Assert.assertEquals(smppSession.getStatus(), Status.BOUND);
		if (bindType == Connection.TRANSCEIVER) {
			Assert.assertEquals(smppSession.getBindType(), BindType.TRANSCIEVER);
		} else if (bindType == Connection.TRANSMITTER) {
			Assert.assertEquals(smppSession.getBindType(), BindType.TRANSMITTER);
		} else if (bindType == Connection.RECEIVER) {
			Assert.assertEquals(smppSession.getBindType(), BindType.RECEIVER);
		} else {
			Assert.fail("Unknown bind type: " + bindType);
		}
		
		Assert.assertEquals(smppSession.getSystemId(), "test");
		
		// unbind
		smppClient.unbind();
		
		// check the session
		Assert.assertEquals(smppSession.getStatus(), Status.DEAD);
		
	}
	
	@Test(expectedExceptions=IllegalStateException.class)
	public void shouldFailCallingGetBindTypeIfNotBound() throws Exception {
		
		PeerFactory peerFactory = new PeerFactory();
		SmppSession smppSession = peerFactory.getSmppSession();
		
		smppSession.getBindType();
	}
	
	@Test(expectedExceptions=IllegalStateException.class)
	public void shouldFailCallingGetSystemIdIfNotBound() throws Exception {
		
		PeerFactory peerFactory = new PeerFactory();
		SmppSession smppSession = peerFactory.getSmppSession();
		
		smppSession.getSystemId();
	}
	
	@Test
	public void shouldCallCustomPacketProcessor() throws Exception {
		
		// create the session and client
		PeerFactory peerFactory = new PeerFactory();
		SmppSession smppSession = peerFactory.getSmppSession();
		Connection smppClient = peerFactory.getSmppClient();
		
		// set custom packet processor
		smppSession.setPacketProcessor(new PacketProcessor() {

			@Override
			public void processPacket(SMPPPacket packet, ResponseSender responseSender) {
				responseSender.sendResponse(Response.BIND_FAILED);
			}
			
		});
		
		// bind client
		BindResp bindResponse = smppClient.bind(Connection.TRANSCEIVER, "test", null, null);
		Assert.assertNotNull(bindResponse);
		Assert.assertEquals(bindResponse.getCommandStatus(), Response.BIND_FAILED.getCommandStatus());
		
		// check session
		Assert.assertEquals(smppSession.getStatus(), Status.IDLE);
		
	}
	
	@Test
	public void shouldSendRequestToClient() throws Exception {
		
		// create the session and client
		PeerFactory peerFactory = new PeerFactory();
		SmppSession smppSession = peerFactory.getSmppSession();
		Connection smppClient = peerFactory.getSmppClient();
		
		BindResp bindResp = smppClient.bind(Connection.TRANSCEIVER, "test", null, null);
		Assert.assertNotNull(bindResp);
		Assert.assertEquals(bindResp.getCommandStatus(), Response.OK.getCommandStatus());
		
		// send a DeliverSm
		DeliverSM ds1 = new DeliverSM();
		smppSession.sendRequest(ds1);
		
		SMPPPacket packet = smppClient.readNextPacket();
		Assert.assertNotNull(packet);
		Assert.assertEquals(packet.getCommandId(), SMPPPacket.DELIVER_SM);
		int sequenceNumber = packet.getSequenceNum();
		
		// send another DeliverSm
		DeliverSM ds2 = new DeliverSM();
		smppSession.sendRequest(ds2);
		
		packet = smppClient.readNextPacket();
		Assert.assertNotNull(packet);
		Assert.assertEquals(packet.getCommandId(), SMPPPacket.DELIVER_SM);
		Assert.assertEquals(packet.getSequenceNum(), sequenceNumber + 1);
		
		// unbind
		smppClient.unbind();
	}
	
	@Test
	public void shouldFailSecondBind() throws Exception {
        
		// first bind request
        Bind br1 = new BindTransceiver();
        br1.setSequenceNum(1);
        br1.setSystemId("test");
        
        // second byte request
        Bind br2 = new BindTransceiver();
        br2.setSequenceNum(2);
        br2.setSystemId("test");
        
        Collection<SMPPPacket> packets = new ArrayList<SMPPPacket>();
        packets.add(br1);
        packets.add(br2);
        
        ByteArrayInputStream bis = createInputStream(packets);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        
        // create the session
        StreamLink link = new StreamLink(bis, bos);
        SmppSession session = new SmppSession(link);
        
        // wait until the packages are read
        waitForStatus(session, Status.DEAD, 1000);
        Assert.assertEquals(session.getStatus(), Status.DEAD);
        
        // read the first bind response
        byte[] packetsBytes = bos.toByteArray();
        SMPPPacket packet = getFirstPacket(packetsBytes);
    
        // check the first bind response
        Assert.assertNotNull(packet);
        Assert.assertEquals(packet.getCommandId(), SMPPPacket.BIND_TRANSCEIVER_RESP);
        Assert.assertEquals(packet.getCommandStatus(), Response.OK.getCommandStatus());
        
        // read the second bind response
        packetsBytes = removeFirstPacket(packetsBytes);
        packet = getFirstPacket(packetsBytes);
        
        // check the second bind response
        Assert.assertNotNull(packet);
        Assert.assertEquals(packet.getCommandId(), SMPPPacket.BIND_TRANSCEIVER_RESP);
        Assert.assertEquals(packet.getCommandStatus(), Response.ALREADY_BOUND.getCommandStatus());
	}
	
	@Test
	public void shouldFailCommandBeforeBind() throws Exception {
		
		// create a packet that is not a bind
		SMPPPacket notBindPacket = new SubmitSM();
		notBindPacket.setSequenceNum(1);
		
		Collection<SMPPPacket> packets = new ArrayList<SMPPPacket>();
		packets.add(notBindPacket);
		
		ByteArrayInputStream bis = createInputStream(packets);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        
        // create the session
        StreamLink link = new StreamLink(bis, bos);
        SmppSession session = new SmppSession(link);
        
        // wait until the packages are read
        waitForStatus(session, Status.DEAD, 1000);
        Assert.assertEquals(session.getStatus(), Status.DEAD);
        
        // read the first bind response
        byte[] packetsBytes = bos.toByteArray();
        SMPPPacket packet = getFirstPacket(packetsBytes);
    
        // check the first bind response
        Assert.assertNotNull(packet);
        Assert.assertEquals(packet.getCommandId(), SMPPPacket.SUBMIT_SM_RESP);
        Assert.assertEquals(packet.getCommandStatus(), Response.INVALID_BIND_STATUS.getCommandStatus());
		
	}
	
	private ByteArrayInputStream createInputStream(Collection<SMPPPacket> packets) throws IOException {
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		for (SMPPPacket packet : packets) {
			packet.writeTo(bos);
		}
		
		// create the input stream and return it
		ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
		return bis;
		
	}
	
	private void waitForStatus(SmppSession session, Status status, long timeout) {
		
		long startTime = new Date().getTime();
		
		while (session.getStatus() != Status.DEAD && (new Date().getTime() - startTime) < timeout) {
        	try { Thread.sleep(200); } catch (Exception e) {}
        }
		
	}
	
	private byte[] removeFirstPacket(byte[] buffer) {
		int commandLength = SMPPIO.bytesToInt(buffer, 0, 4);
		
		int leftBytes = buffer.length - commandLength;
        byte[] leftBuffer = new byte[leftBytes];
        System.arraycopy(buffer, commandLength, leftBuffer, 0, leftBytes);
        
        return leftBuffer;
	}
	
	private SMPPPacket getFirstPacket(byte[] buffer) throws IOException, BadCommandIDException {
		
		int commandId = SMPPIO.bytesToInt(buffer, 4, 4); 
        SMPPPacket packet = PacketFactory.newInstance(commandId);
        
        if (packet != null) {
            packet.readFrom(buffer, 0);
        
        }
        
        return packet;
	}
	
	private class PeerFactory {
		
		private SmppSession smppSession;
		
		private Connection smppClient;
		
		public PeerFactory() throws IOException {
			
			// the output from the client is the session input
			PipedOutputStream clientOutput = new PipedOutputStream();
			PipedInputStream sessionInput = new PipedInputStream(clientOutput);
			
			// the output from the session is the client input
			PipedOutputStream sessionOutput = new PipedOutputStream();
			PipedInputStream clientInput = new PipedInputStream(sessionOutput);
			
			StreamLink sessionLink = new StreamLink(sessionInput, sessionOutput);
			sessionLink.open();
			
			StreamLink clientLink = new StreamLink(clientInput, clientOutput);
			clientLink.open();
			
			smppSession = new SmppSession(sessionLink);
			Assert.assertEquals(smppSession.getStatus(), Status.IDLE);
			
			smppClient = new Connection(clientLink);
		}

		public SmppSession getSmppSession() {
			return smppSession;
		}

		public Connection getSmppClient() {
			return smppClient;
		}
		
	}
	
}
