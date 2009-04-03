package p4s.transport;

/**
 *
 * This class is very similar to (@link peersim.transport.UniformRandomTrasport)
 * It implements the method for sending control messages with a given delay
 *
 */
import peersim.config.*;
import peersim.core.*;
import peersim.edsim.*;
public class UniformRandomTransportP4S implements TransportP4S
{

//---------------------------------------------------------------------
//Parameters
//---------------------------------------------------------------------

	
private static final String PAR_MINDELAY = "mindelay";	
private static final String PAR_MAXDELAY = "maxdelay";
//private static final String PAR_MUDELAY = "mudelay";
//private static final String PAR_DEVDELAY = "devdelay";
//private static final String PAR_DELAY = "delay";
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
//private final double mu;
//private final double dev;
//protected static long[][] owd;
//private final int delay;
/**
 * Many kinds of delay distributions
 * 0 Uniform between max and min
 * 1 Gaussian with mean
 * 2 Exponential Truncated with mean
 */

//---------------------------------------------------------------------
//Initialization
//---------------------------------------------------------------------

/**
 * Reads configuration parameter.
 */
public UniformRandomTransportP4S(String prefix)
{
    
	min = Configuration.getLong(prefix + "." + PAR_MINDELAY,0);
	max = Configuration.getLong(prefix + "." + PAR_MAXDELAY,0);
	if (max < min) 
	   throw new IllegalParameterException(prefix+"."+PAR_MAXDELAY, 
	   "The maximum latency cannot be smaller than the minimum latency");
	range = max-min+1;
    
//	System.out.println("Min delay "+min +", Max delay "+max);
}

//---------------------------------------------------------------------

public Object clone()
{
	return this;
}

//---------------------------------------------------------------------
//Methods
//---------------------------------------------------------------------

public long sendControl(Node src, Node dest, Object msg, int pid)
{	
	long delay = (range==1?min:min + CommonState.r.nextLong(range));
//    long delay = this.owd[src.getIndex()][dest.getIndex()];
	EDSimulator.add(delay, msg, dest, pid);
	return delay;
}
public void sendControl(Node src, Node dest, Object msg, long delay, int pid){
	EDSimulator.add(delay, msg, dest, pid);
}

public long getLatency(Node src, Node dest)
{
	return (range==1?min:min + CommonState.r.nextLong(range));
}
}
