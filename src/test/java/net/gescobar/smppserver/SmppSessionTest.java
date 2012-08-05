package net.gescobar.smppserver;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import net.gescobar.smppserver.packet.Bind;
import net.gescobar.smppserver.packet.SmppRequest;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.MessageEvent;
import org.testng.annotations.Test;

import com.cloudhopper.smpp.pdu.BindTransceiver;

public class SmppSessionTest {
	
	@Test(expectedExceptions=IllegalStateException.class)
	public void getBindTypeShouldFailIfNotBound() throws Exception {
		SmppSession session = new SmppSession( mock(Channel.class), new DefaultPacketProcessor() );
		session.getBindType();
	}
	
	@Test(expectedExceptions=IllegalStateException.class)
	public void getSystemIdShouldFailIfNotBound() throws Exception {
		SmppSession session = new SmppSession( mock(Channel.class), new DefaultPacketProcessor() );
		session.getSystemId();
	}
	
	@Test
	public void shouldCallCustomPacketProcessor() throws Exception {
		PacketProcessor packetProcessor = mock(PacketProcessor.class);
		SmppSession session = new SmppSession( mock(Channel.class), packetProcessor);
		
		MessageEvent event = mock(MessageEvent.class);
		when(event.getMessage()).thenReturn(new BindTransceiver());
		
		session.messageReceived(null, event);
		
		verify(packetProcessor).processPacket(any(SmppRequest.class), any(ResponseSender.class));
		
	}
	
	private class DefaultPacketProcessor implements PacketProcessor {

		@Override
		public void processPacket(SmppRequest packet, ResponseSender responseSender) {
			if (Bind.class.isInstance(packet)) {
				responseSender.send( Response.OK );
			}
		}
		
	}
	
}
