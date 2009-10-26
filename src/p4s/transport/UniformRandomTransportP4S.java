package p4s.transport;

import peersim.config.*;
import peersim.core.*;
import peersim.edsim.*;

/**
 *
 * This class is very similar to (@link peersim.transport.UniformRandomTrasport)
 * It implements the method for sending control messages with a given delay
 *
 */
public class UniformRandomTransportP4S implements TransportP4S {

//---------------------------------------------------------------------
//Parameters
//---------------------------------------------------------------------
    private static final String PAR_MINDELAY = "mindelay";
    private static final String PAR_MAXDELAY = "maxdelay";
///**
//---------------------------------------------------------------------
//Fields
//---------------------------------------------------------------------
    /** Minimum delay for message sending */
    private final long min;
    /** Difference between the max and min delay plus one. That is, max delay is
     * min+range-1.
     */
    private final long range;
    private final long max;

//---------------------------------------------------------------------
//Initialization
//---------------------------------------------------------------------
    /**
     * Reads configuration parameter.
     */
    public UniformRandomTransportP4S(String prefix) {

        min = Configuration.getLong(prefix + "." + PAR_MINDELAY, 0);
        max = Configuration.getLong(prefix + "." + PAR_MAXDELAY, 0);
        if (max < min) {
            throw new IllegalParameterException(prefix + "." + PAR_MAXDELAY,
                    "The maximum latency cannot be smaller than the minimum latency");
        }
        range = max - min + 1;

//	System.out.println("Min delay "+min +", Max delay "+max);
    }

//---------------------------------------------------------------------
    public Object clone() {
        return this;
    }

//---------------------------------------------------------------------
//Methods
//---------------------------------------------------------------------
    /**
     * Send a message with a uniform delay between min and max.
     * @param src
     * @param dest
     * @param msg
     * @param pid
     * @return delay time
     */
    public long sendControl(Node src, Node dest, Object msg, int pid) {
        long delay = (range == 1 ? min : min + CommonState.r.nextLong(range));
//    long delay = this.owd[src.getIndex()][dest.getIndex()];
        EDSimulator.add(delay, msg, dest, pid);
        return delay;
    }

    /**
     * Send a message with a given delay.
     * @param src
     * @param dest
     * @param msg
     * @param delay
     * @param pid
     */
    public void sendControl(Node src, Node dest, Object msg, long delay, int pid) {
        EDSimulator.add(delay, msg, dest, pid);
    }

    /**
     * Return the latency between two nodes.
     * @param src sender node
     * @param dest receiver node
     * @return latency between nodes
     */
    public long getLatency(Node src, Node dest) {
        return (range == 1 ? min : min + CommonState.r.nextLong(range));
    }
}
