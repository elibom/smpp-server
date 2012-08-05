package net.gescobar.smppserver.packet.ch;

import net.gescobar.smppserver.packet.Address;
import net.gescobar.smppserver.packet.Bind;
import net.gescobar.smppserver.packet.DeliverSm;
import net.gescobar.smppserver.packet.DeliverSmResp;
import net.gescobar.smppserver.packet.EnquireLink;
import net.gescobar.smppserver.packet.EnquireLinkResp;
import net.gescobar.smppserver.packet.GenericNack;
import net.gescobar.smppserver.packet.SmppPacket;
import net.gescobar.smppserver.packet.SubmitSm;
import net.gescobar.smppserver.packet.Tlv;

import com.cloudhopper.smpp.pdu.BaseBind;
import com.cloudhopper.smpp.pdu.Pdu;
import com.cloudhopper.smpp.type.SmppInvalidArgumentException;

/**
 * Maps packets from the cloudhopper representation to our representation and vice versa.
 * 
 * @author German Escobar
 */
public class PacketMapper {

	@SuppressWarnings("rawtypes")
	public static SmppPacket map(Pdu pdu) {
		
		if (pdu == null) {
			return null;
		}

		SmppPacket packet = null;
		
		if (BaseBind.class.isInstance(pdu)) {
			packet = map( (BaseBind) pdu );
		} else if (pdu.getCommandId() == SmppPacket.SUBMIT_SM) {
			packet = map( (com.cloudhopper.smpp.pdu.SubmitSm) pdu );
		} else if (pdu.getCommandId() == SmppPacket.DELIVER_SM_RESP) {
			packet = new DeliverSmResp();
		} else if (pdu.getCommandId() == SmppPacket.ENQUIRE_LINK) {
			packet = new EnquireLink();
		} else if (pdu.getCommandId() == SmppPacket.ENQUIRE_LINK_RESP) {
			packet = new EnquireLinkResp();
		} else if (pdu.getCommandId() == SmppPacket.GENERIC_NACK) {
			packet = new GenericNack();
		}
		
		// it was a unknown packet, return null
		if (packet == null) {
			return null;
		}
		
		packet.setCommandStatus( pdu.getCommandStatus() );
		packet.setSequenceNumber( pdu.getSequenceNumber() );
		
		if (pdu.getOptionalParameters() != null) {
			for (com.cloudhopper.smpp.tlv.Tlv op : pdu.getOptionalParameters()) {
				packet.addOptionalParameter( map(op) );
			}
		}
		
		return packet;
		
	}
	
	@SuppressWarnings("rawtypes")
	private static Bind map(BaseBind chBind) {
		
		Bind bind = new Bind( chBind.getCommandId() );
		bind.setSystemId( chBind.getSystemId() );
		bind.setPassword( chBind.getPassword() );
		bind.setSystemType( chBind.getSystemType() );
		bind.setAddressRange( map(chBind.getAddressRange()) );
		
		return bind;
		
	}
	
	private static SubmitSm map(com.cloudhopper.smpp.pdu.SubmitSm chSubmitSm) {
		
		SubmitSm submitSm = new SubmitSm();
		submitSm.setServiceType( chSubmitSm.getServiceType() );
		submitSm.setSourceAddress( map(chSubmitSm.getSourceAddress()) );
		submitSm.setDestAddress( map(chSubmitSm.getDestAddress()) );
		submitSm.setEsmClass( chSubmitSm.getEsmClass() );
		submitSm.setProtocolId( chSubmitSm.getProtocolId() );
		submitSm.setPriority( chSubmitSm.getPriority() );
		submitSm.setScheduleDeliveryTime( chSubmitSm.getScheduleDeliveryTime() );
		submitSm.setValidityPeriod( chSubmitSm.getValidityPeriod() );
		submitSm.setRegisteredDelivery( chSubmitSm.getRegisteredDelivery() );
		submitSm.setReplaceIfPresent( chSubmitSm.getReplaceIfPresent() );
		submitSm.setDataCoding( chSubmitSm.getDataCoding() );
		submitSm.setDefaultMsgId( chSubmitSm.getDefaultMsgId() );
		submitSm.setShortMessage( chSubmitSm.getShortMessage() );
		
		return submitSm;
		
	}
	
	private static Address map(com.cloudhopper.smpp.type.Address chAddress) {
		
		if (chAddress == null) { 
			return null;
		}
		
		return new Address()
			.withTon( chAddress.getTon() )
			.withNpi( chAddress.getNpi() )
			.withAddress( chAddress.getAddress() );
		
	}
	
	private static Tlv map(com.cloudhopper.smpp.tlv.Tlv tlv) {
		return new Tlv( tlv.getTag(), tlv.getValue(), tlv.getTagName() );
	}
	
	public static Pdu map(SmppPacket packet) throws SmppInvalidArgumentException {
		
		if (packet == null) { 
			return null;
		}
		
		Pdu pdu = null;
		
		if (packet.getCommandId() == SmppPacket.DELIVER_SM) {
			pdu = map( (DeliverSm) packet );
		} else if (packet.getCommandId() == SmppPacket.ENQUIRE_LINK) {
			pdu = new com.cloudhopper.smpp.pdu.EnquireLink();
		}
		
		if (pdu == null) {
			return null;
		}
		
		pdu.setCommandStatus( packet.getCommandStatus() );
		pdu.setSequenceNumber( packet.getSequenceNumber() );
		
		return pdu;
		
	}
	
	private static Pdu map(DeliverSm deliverSm) throws SmppInvalidArgumentException {
		
		com.cloudhopper.smpp.pdu.DeliverSm chDeliverSm = new com.cloudhopper.smpp.pdu.DeliverSm();
		chDeliverSm.setServiceType( deliverSm.getServiceType() );
		chDeliverSm.setSourceAddress( map(deliverSm.getSourceAddress()) );
		chDeliverSm.setDestAddress( map(deliverSm.getDestAddress()) );
		chDeliverSm.setEsmClass( deliverSm.getEsmClass() );
		chDeliverSm.setProtocolId( deliverSm.getProtocolId() );
		chDeliverSm.setPriority( deliverSm.getPriority() );
		chDeliverSm.setScheduleDeliveryTime( deliverSm.getScheduleDeliveryTime() );
		chDeliverSm.setValidityPeriod( deliverSm.getValidityPeriod() );
		chDeliverSm.setRegisteredDelivery( deliverSm.getRegisteredDelivery() );
		chDeliverSm.setReplaceIfPresent( deliverSm.getReplaceIfPresent() );
		chDeliverSm.setDataCoding( deliverSm.getDataCoding() );
		chDeliverSm.setShortMessage( deliverSm.getShortMessage() );
		
		return chDeliverSm;
		
	}
	
	private static com.cloudhopper.smpp.type.Address map(Address address) {
		
		if (address == null) {
			return null;
		}
		
		com.cloudhopper.smpp.type.Address chAddress = new com.cloudhopper.smpp.type.Address();
		chAddress.setTon( address.getTon() );
		chAddress.setNpi( address.getNpi() );
		chAddress.setAddress( address.getAddress() );
		
		return chAddress;
		
	}
	
}
