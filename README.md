# SMPP Server

A project based on the [smppapi](http://smppapi.sourceforge.net/) library that accepts client connections and allows you to easily handle SMPP packets.

## Starting and stopping

To start the server you need to instantiate the @net.gescobar.smppserver.SmppServer@ class and call the `start()` method:

```java
SmppServer server = new SmppServer(4444); // 4444 is the port, change it as needed
server.start();
		
// somewhere else
server.stop();
```

### Processing SMPP packets

To process SMPP packets, you will need to provide an implementation of the `net.gescobar.smppserver.PacketProcessor`. For example:

```java
public class MyPacketProcessor implements PacketProcessor {
			
	@Override
	public Response processPacket(SMPPPacket packet) {
				
		if (packet.getCommandId() == SMPPPacket.BIND_RECEIVER
	   	 		|| packet.getCommandId() == SMPPPacket.BIND_TRANSCEIVER
	   	 		|| packet.getCommandId() == SMPPPacket.BIND_TRANSMITTER) {
	   	 		
	   	 	// check the credentials and return the corresponding SMPP command status
	   	 	return Response.OK;
	   	 					
	   	 } else if (packet.getCommandId() == SMPPPacket.SUBMIT_SM) {
	   	 		
	   	 	// a message has arrived, what do you want to do with it?
	   	 			
	   	 	return Response.INVALID_DEST_ADDRESS; // just an example
	   	 		
	   	 }
	}
}
```

To use your `PacketProcessor` implementation, set it in the `SmppServer` using the constructor or the setter method:

```java
SmppServer server = new SmppServer(4444, new MyPacketProcessor());
		
// or
		
server.setPacketProcessor(new MyPacketProcessor());
```

If you don't provide a `PacketProcessor` implementation, the default one (that always returns `Response.OK`) will be used.

## Sending SMPP requests to the client

You can also send requests to the client (e.g. deliver_sm or unbind) through a session. For example:

```java
// first, we need to find the session to which we want to send the request
SmppSession targetSession = null;

Collection<SmppSession> sessions = server.getSessions();
for (SmppSession s : sessions) {
	// check if this is the session we are looking for
	if (s.getSystemId().equals("test") && s.getBindType().equals(BindType.TRANSCEIVER)) {
		targetSession = s;
	}
}

// create the request and send it
DeliverSM ds = new DeliverSM();
// ... set other fields

targetSession.sendRequest(ds);
```

*That's it!* As you can see, it's a simple, yet powerful design that will allow you to accept SMPP client connections, process incoming SMPP packets and send requests to the clients.