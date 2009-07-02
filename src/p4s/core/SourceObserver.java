package p4s.core;

import peersim.config.*;
import peersim.core.*;

/**
 *
 * @author ax
 */
public class SourceObserver implements Control {
    // ///////////////////////////////////////////////////////////////////////
    // Constants
    // ///////////////////////////////////////////////////////////////////////

    private static final String PAR_PROT = "protocol";
    // ///////////////////////////////////////////////////////////////////////
    // Fields
    // ///////////////////////////////////////////////////////////////////////
    /**
     * The name of this observer in the configuration file. 
     */
    private final String name;
    /** Protocol identifier, obtained from config property {@link #PAR_PROT}. */
    private final int pid;
    private int new_chunk;
    // ///////////////////////////////////////////////////////////////////////
    // Constructor
    // ///////////////////////////////////////////////////////////////////////
    /**
     * Standard constructor that reads the configuration parameters. 
     * 
     * @param name the configuration prefix for this class.
     */
    public SourceObserver(String name) {
        this.name = name;
        this.new_chunk = -1;
        pid = Configuration.getPid(name + "." + PAR_PROT);
        System.err.println("#Source Observer is ready");
    }

    public boolean execute() {
        if (CommonState.getPhase() == CommonState.POST_SIMULATION) {
            return false;
        }
        Node src = Network.get(Network.size() - 1);
        Alternate protocol = (Alternate) src.getProtocol(pid);
        if (protocol.getSource() != src.getIndex()) {
            System.err.println("Problema nella produzione del new_chunk, selezionata sorgente " +
                    src.getID() + " invece di " + protocol.getSource());
            return false;
        }
        int maxchunk = protocol.getNumberOfChunks();        
        if (new_chunk +1 < maxchunk){
            new_chunk++;
            if (protocol.getDebug() >= 2)
                System.out.println(CommonState.getTime() + " >> Sorgente " + src.getID() + " produce " + new_chunk+ " <<");
            protocol.getLastsrc().addLast(new_chunk);
            protocol.chunk_list[new_chunk] = 1;
        }
        else
            if (protocol.getDebug() >= 2)
                System.out.println(CommonState.getTime() + " >> Sorgente " + src.getID() + " finished to produce chunks " + protocol.getSize()+ " <<");
        
        return false;
    }
}
