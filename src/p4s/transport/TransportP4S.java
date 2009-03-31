package p4s.transport;

import peersim.core.*;

public interface TransportP4S extends Protocol
{
//---------------------------------------------------------------------
//Methods
//---------------------------------------------------------------------

/**
 * Delivers the message with either a uniform random delay or a given delay.
*/
public long sendControl(Node src, Node dest, Object msg, int pid);
public void sendControl(Node src, Node dest, Object msg, long delay, int pid);
/**
 * 
 * Per spedire pacchetti e sapere il ritardo, più per debug che per altro.
 * 
 * */
//public long sendChunk(Node src, Node dest, Object msg, int pid);
///**
// *
// * Per spedire pacchetti ed impostare il ritardo, per la spedizione di chunks
// *
// * */
//public void sendChunk(Node src, Node dest, Object msg, long delay, int pid);
/**
 * Returns a random
 * delay, that is drawn from the configured interval according to the uniform
 * distribution.
*/
public long getLatency(Node src, Node dest);


}
