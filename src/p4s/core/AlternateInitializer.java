package p4s.core;

import p4s.util.*;
import peersim.config.*;
import peersim.core.*;
import peersim.edsim.*;

public class AlternateInitializer implements Control {

    /**
     * Initialize the alternate protocol using parameter given in the configuration file.
     * 
     * @author Alessandro Russo
     * @version 1.0
     */

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    private static final String PAR_PROT = "protocol";
    private static final String PAR_BANDWIDTH = "bandwidth";
    private static final String PAR_CHUNKS = "chunks";
    private static final String PAR_CHUNK_SIZE = "chunk_size";   
    private static final String PAR_PUSH_RETRY = "push_retry";
    private static final String PAR_PULL_RETRY = "pull_retry";
    private static final String PAR_SWITCH_TIME = "switchtime";
    private static final String PAR_NEIGH_KNOW = "neighborsknow";
    private static final String PAR_PUSH_WINDOW = "push_window";
    private static final String PAR_PULL_WINDOW = "pull_window";
    private static final String PAR_DEBUG = "debug";
    // ------------------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------------------         
    private final int number_of_chunks;    
    private final int pid;
    private final int neigh;
    private final long chunk_size;    
    private final int debug;
    private int bandiwdthp;
    private final int push_retry;
    private final int pull_retry;
    private final long switchtime;
    private final int push_window;
    private final int pull_window;

    // //////////////////////////////
    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Creates a new instance and read parameters from the config file.
     */
    public AlternateInitializer(String prefix) {
        number_of_chunks = Configuration.getInt(prefix + "." + PAR_CHUNKS);
        pid = Configuration.getPid(prefix + "." + PAR_PROT);
        neigh = Configuration.getInt(prefix+"."+PAR_NEIGH_KNOW,0);
        chunk_size = Configuration.getLong(prefix + "." + PAR_CHUNK_SIZE, 0);
        push_retry = Configuration.getInt(prefix + "." + PAR_PUSH_RETRY, 1);
        pull_retry = Configuration.getInt(prefix + "." + PAR_PULL_RETRY, 1);
        switchtime = Configuration.getLong(prefix + "." + PAR_SWITCH_TIME, 1);
        push_window = Configuration.getInt(prefix + "." + PAR_PUSH_WINDOW, 1);
        pull_window = Configuration.getInt(prefix + "." + PAR_PULL_WINDOW, 1);
        debug = Configuration.getInt(prefix + "." + PAR_DEBUG);
        bandiwdthp = Configuration.getPid(prefix + "." + PAR_BANDWIDTH);        
    }

    // ------------------------------------------------------------------------
    // Methods
    // ------------------------------------------------------------------------
    public boolean execute() {
        System.err.print("Alternate Initializer: Start...");
        Node source = Network.get(Network.size() - 1);//the source is always the last node.
        for (int i = 0; i < Network.size(); i++) {
            Node aNode = Network.get(i);
            AlternateDataSkeleton prot = (AlternateDataSkeleton) aNode.getProtocol(pid);
            prot.resetAll();
            prot.Initialize(number_of_chunks);
            prot.setNumberOfChunks(number_of_chunks);
            prot.setSource(source.getIndex());
            prot.setChunkSize(chunk_size);
            prot.setPushRetry(push_retry);
            prot.setPullRetry(pull_retry);
            prot.setSwitchTime(switchtime);
            prot.setBandwidth(bandiwdthp);
            prot.setPushWindow(push_window);
            prot.setPullWindow(pull_window);
            prot.setNeighborKnowledge(neigh);
            prot.setDebug(debug);
        }
        AlternateDataSkeleton prot = (AlternateDataSkeleton) source.getProtocol(pid);
        prot.setCycle(Message.PUSH_CYCLE);
        EDSimulator.add(1, new P4SMessage(null, source, Message.SWITCH_PUSH, 0L), source, pid);
        System.err.println("finished");
        return false;
    }
}
