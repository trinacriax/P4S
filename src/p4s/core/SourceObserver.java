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
            System.err.println("Problema nella produzione del chunk, selezionata sorgente " +
                    src.getID() + " invece di " + protocol.getSource());
            return false;
        }
        if (protocol.produce() == false) {
        } else if (protocol.getDebug() >= 2) {
            System.out.println(CommonState.getTime() + " >> Sorgente " + src.getID() + " produce " + protocol.getLast() + " <<");
        }
        return false;
    }
}
