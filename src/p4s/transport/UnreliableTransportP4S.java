package p4s.transport;

import peersim.config.*;
import peersim.core.*;
import peersim.transport.Transport;

/**
 *
 * Take care how to manage transmission lost with chunkdelivery.
 * This class is the same of (@link peersim.transport.UnreliableTransport)
 * but it adds another control for dropping messages: a message form a node
 * to itself cannot be dropped becauses such a kind of message is used to change
 * node status.
 *
 * @author Alessandro Russo.
 */
public class UnreliableTransportP4S implements Transport {

//---------------------------------------------------------------------
//Parameters
//---------------------------------------------------------------------
    private static final String PAR_TRANSPORT = "transport";
    private static final String PAR_DROP = "drop";
//---------------------------------------------------------------------
//Fields
//---------------------------------------------------------------------
    /** Protocol identifier for the support transport protocol */
    private final int transport;
    /** Probability of dropping messages */
    private final float loss;

//---------------------------------------------------------------------
//Initialization
//---------------------------------------------------------------------
    /**
     * Reads configuration parameter.
     */
    public UnreliableTransportP4S(String prefix) {
        transport = Configuration.getPid(prefix + "." + PAR_TRANSPORT);
        loss = (float) Configuration.getDouble(prefix + "." + PAR_DROP);
    }

//---------------------------------------------------------------------
    /**
     * Retuns <code>this</code>. This way only one instance exists in the system
     * that is linked from all the nodes. This is because this protocol has no
     * state that depends on the hosting node.
     */
    public Object clone() {
        return this;
    }

//---------------------------------------------------------------------
//Methods
//---------------------------------------------------------------------
    /** Sends the message according to the underlying transport protocol.
     * With the configured probability, the message is not sent (ie the method does
     * nothing).
     */
    public void send(Node src, Node dest, Object msg, int pid) {
        try {
            if (CommonState.r.nextFloat() >= loss && src != dest) {
                // Message is not lost
                Transport t = (Transport) src.getProtocol(transport);
                t.send(src, dest, msg, pid);
            }
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Protocol " +
                    Configuration.lookupPid(transport) +
                    " does not implement Transport");
        }
    }

    /** Returns the latency of the underlying protocol.*/
    public long getLatency(Node src, Node dest) {
        Transport t = (Transport) src.getProtocol(transport);
        return t.getLatency(src, dest);
    }
}
