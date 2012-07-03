package net.gescobar.smppserver;

/**
 * This is a base class for {@link Response} implementations. It implements setters and getters methods so 
 * concrete implementations only have to worry about the {@link #send()} method.
 * 
 * @author German Escobar
 */
public abstract class AbstractResponse implements Response {

	private CommandStatus commandStatus = CommandStatus.OK;
	
	private String messageId;
	
	@Override
	public Response setCommandStatus(CommandStatus commandStatus) {
		this.commandStatus = commandStatus;
		return this;
	}
	
	public CommandStatus getCommandStatus() {
		return commandStatus;
	}
	
	@Override
	public Response setMessageId(String messageId) {
		this.messageId = messageId;
		return this;
	}
	
	public String getMessageId() {
		return messageId;
	}

}
