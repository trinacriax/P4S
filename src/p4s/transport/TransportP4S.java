package p4s.transport;

import peersim.core.*;

/**
 * Interface used to provide the main methods for sending messages in teh protocol.
 * @author Alessandro Russo.
 */
public interface TransportP4S extends Protocol {
//---------------------------------------------------------------------
//Methods
//---------------------------------------------------------------------

    /**
     * Delivers the message with either a uniform random delay .
     */
    public long sendControl(Node src, Node dest, Object msg, int pid);

    /**
     * Delivers the message with either a given delay .
     */
    public void sendControl(Node src, Node dest, Object msg, long delay, int pid);

    /**
     * Returns a random
     * delay, that is drawn from the configured interval according to the uniform
     * distribution.
     */
    public long getLatency(Node src, Node dest);
}
