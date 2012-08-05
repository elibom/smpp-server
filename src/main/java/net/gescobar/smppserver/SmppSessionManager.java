package net.gescobar.smppserver;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cloudhopper.smpp.channel.SmppChannelConstants;
import com.cloudhopper.smpp.channel.SmppSessionPduDecoder;
import com.cloudhopper.smpp.transcoder.DefaultPduTranscoder;
import com.cloudhopper.smpp.transcoder.DefaultPduTranscoderContext;

public class SmppSessionManager extends SimpleChannelUpstreamHandler {
	
	private Logger log = LoggerFactory.getLogger(SmppSessionManager.class);
	
	private Map<Channel,SmppSession> sessions = new HashMap<Channel,SmppSession>();
	
	private ChannelGroup channelGroup = new DefaultChannelGroup();
	
	private PacketProcessor packetProcessor;
	
	public SmppSessionManager(PacketProcessor packetProcessor) {
		this.packetProcessor = packetProcessor;
	}
	
	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		
		Channel channel = e.getChannel();
		SmppSession session = new SmppSession(channel, packetProcessor);
		
        channel.getPipeline().addLast(SmppChannelConstants.PIPELINE_SESSION_PDU_DECODER_NAME, 
        		new SmppSessionPduDecoder(new DefaultPduTranscoder(new DefaultPduTranscoderContext())));
		channel.getPipeline().addLast(SmppChannelConstants.PIPELINE_SESSION_WRAPPER_NAME, session);
		
		channelGroup.add(channel);
		
		sessions.put(channel, session);
	}

	@Override
	public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		channelGroup.remove(e.getChannel());
		SmppSession session = sessions.remove(e.getChannel());
		
		if (session != null) {
			log.info("[" + session.getSystemId() + "] disconnected");
		}
	}
	
	public void close() {
		channelGroup.close().awaitUninterruptibly();
	}

	public Collection<SmppSession> getSessions() {
		return Collections.unmodifiableCollection(sessions.values());
	}

	public void setPacketProcessor(PacketProcessor packetProcessor) {
		this.packetProcessor = packetProcessor;
	}
	
}
