package p4s.core;

import peersim.config.*;
import peersim.core.*;

/**
 * Streaming source. <p>
 * This class implements the streaming server which emerges a new chunk periodically, every time unit.
 *
 * @author Alessandro Russo
 * @version 1.0
 */
public class StreamingSource implements Control {
    // ///////////////////////////////////////////////////////////////////////
    // Constants
    // ///////////////////////////////////////////////////////////////////////

    private static final String PAR_PROT = "protocol";
    // ///////////////////////////////////////////////////////////////////////
    // Fields
    // ///////////////////////////////////////////////////////////////////////
    /** Observer name from config file. */
    private final String name;
    /** Protocol identifier, obtained from config property {@link #PAR_PROT}.*/
    private final int pid;
    /** Latest chunk produced. */
    private int new_chunk;
    // ///////////////////////////////////////////////////////////////////////
    // Constructor
    // ///////////////////////////////////////////////////////////////////////

    /**
     * Standard constructor that reads the configuration parameters. <p>
     * Initialize the Streaming source with the parameters given in the config file.
     * 
     * @param name the configuration prefix for this class.
     */
    public StreamingSource(String name) {
        this.name = name;
        this.new_chunk = -1;
        pid = Configuration.getPid(name + "." + PAR_PROT);
        System.err.println("#Streaming source " + name + " is ready to emerge chunks.");
    }

    /**
     * Control entity invoked periodically to produce a new chunk.<p>
     * Produces a chunk every time unit.
     *
     * @return False continue the simulation, true exit.
     */
    public boolean execute() {
        // when the simulation finishes, nothing to do.
        if (CommonState.getPhase() == CommonState.POST_SIMULATION) {
            return false;
        }
        //pick up the source node
        Node src = Network.get(Network.size() - 1);
        Alternate protocol = (Alternate) src.getProtocol(pid);
        //check whether is the real source or not
        if (protocol.getSource() != src.getIndex()) {
            System.err.println("There is a problem to produce the chunk " + new_chunk + ", it was selected a node " + src.getIndex() + " different from source " + protocol.getSource() + ".");
            return false;
        }
        //produce a chunk
        int maxchunk = protocol.getNumberOfChunks();
        //if it is valid
        if (new_chunk + 1 < maxchunk) {
            new_chunk++;
            if (protocol.getDebug() >= 2) {
                System.out.println(CommonState.getTime() + " >> Source node " + src.getID() + " emerges " + new_chunk + " <<");
            }
            //adds it in the list of available chunks.
            protocol.enqueueChunk(new_chunk);
        } else if (protocol.getDebug() >= 2) {
            System.out.println(CommonState.getTime() + " >> Source node " + src.getID() + " finishes to produce chunks " + protocol.getAllChunks() + " <<");
        }
        return false;
    }
}
