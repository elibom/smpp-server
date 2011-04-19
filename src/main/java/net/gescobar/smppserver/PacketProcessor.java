package net.gescobar.smppserver;

import ie.omk.smpp.message.SMPPPacket;

/**
 * This interface is implemented by those who want to process incoming SMPP packets received in a 
 * {@link SmppSession}.
 * 
 * @author German Escobar
 */
public interface PacketProcessor {
	
	/**
	 * The possible values that can be returned in the {@link PacketProcessor#processPacket(SMPPPacket)} method.
	 * Actually, they correspond to the values defined in the command status of the SMPP specification.
	 * 
	 * @author German Escobar
	 */
	public enum ResponseStatus {
		
		OK(0),
		INVALID_MESSAGE_LEN(1),
		INVALID_COMMAND_LEN(2),
		INVALID_COMMAND_ID(3),
		INVALID_BIND_STATUS(4),
		ALREADY_BOUND(5),
		INVALID_PRIORITY_FLAG(6),
		INVALID_REGISTERED_DELIVERY_FLAG(7),
		SYSTEM_ERROR(8),
		INVALID_SOURCE_ADDRESS(0xa),
		INVALID_DEST_ADDRESS(0xb),
		INVALID_MESSAGE_ID(0xc),
		BIND_FAILED(0xd),
		INVALID_PASSWORD(0xe),
		INVALID_SYSTEM_ID(0xf),
		CANCEL_SM_FAILED(0x11),
		REPLACE_SM_FAILED(0x13),
		MESSAGE_QUEUE_FULL(0x14),
		INVALID_SERVICE_TYPE(0x15),
		INVALID_NUMBER_OF_DESTINATIONS(0x33),
		INVALID_DISTRIBUTION_LIST(0x34),
		INVALID_DESTINATION_FLAG(0x40),
		INVALID_SUBMIT_WITH_REPLACE(0x42),
		INVALID_ESM_CLASS(0x43),
		SUBMIT_TO_DISTRIBUTION_LIST_FAILED(0x44),
		SUBMIT_FAILED(0x45),
		INVALID_SOURCE_TON(0x48),
		INVALID_SOURCE_NPI(0x49),
		INVALID_DESTINATION_TON(0x50),
		INVALID_DESTINATION_NPI(0x51),
		INVALID_SYSTEM_TYPE(0x53),
		INVALID_REPLACE_IF_PRESENT_FLAG(0x54),
		INVALID_NUMBER_OF_MESSAGES(0x55),
		THROTTLING_ERROR(0x58),
		INVALID_SCHEDULED_DELIVERY_TIME(0x61),
		INVALID_EXPIRY_TIME(0x62),
		INVALID_PREDEFINED_MESSAGE(0x63),
		RECEIVER_TEMPORARY_ERROR(0x64),
		RECEIVER_PERMANENT_ERROR(0x65),
		RECEIVER_REJECT_MESSAGE(0x66),
		QUERY_SM_FAILED(0x67),
		INVALID_OPTIONAL_PARAMETERS(0xc0),
		OPTIONAL_PARAMETER_NOT_ALLOWED(0xc1),
		INVALID_PARAMETER_LENGTH(0xc2),
		MISSING_EXPECTED_PARAMETER(0xc3),
		INVALID_PARAMETER_VALUE(0xc4),
		DELIVERY_FAILED(0xfe);

		private int commandStatus;
		
		private ResponseStatus(int commandStatus) {
			this.commandStatus = commandStatus;
		}

		public int getCommandStatus() {
			return commandStatus;
		}

		public void setCommandStatus(int commandStatus) {
			this.commandStatus = commandStatus;
		}
		
	}

	/**
	 * Process an SMPP Packet and returns a code that will be used as the command status for the response.
	 * 
	 * @param packet the SMPPPacket to be processed.
	 * @return a ResponseStatus with the command status that will be returned to the client.
	 */
	ResponseStatus processPacket(SMPPPacket packet);
	
}
