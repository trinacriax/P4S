/*
 * This class is used to collect statistics on chunks time
 */
package p4s.core;

import java.io.*;
import java.util.*;
import java.util.zip.*;
import peersim.config.*;
import peersim.core.*;

public class ChunksObserver implements Control {

    // ///////////////////////////////////////////////////////////////////////
    // Constants
    // ///////////////////////////////////////////////////////////////////////
    /**
     * The protocol to operate on.
     * 
     * @config
     */
    private static final String PAR_PROT = "protocol";
    private static final String PAR_SIZE = "size";
    private static final String PAR_CHUNKS = "chunks";//oppure inizio con push   
    // ///////////////////////////////////////////////////////////////////////
    // Fields
    // ///////////////////////////////////////////////////////////////////////

    private final String name;
    /** Protocol identifier, obtained from config property {@link #PAR_PROT}. */
    private final int pid;
    private final int size;
    public PrintWriter outMatrix = null;
    public PrintWriter outOperation = null;
    private File peer_matrix;
    private File operation_file;
    private String fpeermatrix;
    private String foperation;
    private String dirname;
    private int chunks;
    private long seed;
    // ///////////////////////////////////////////////////////////////////////
    // Constructor
    // ///////////////////////////////////////////////////////////////////////
    /**
     * Standard constructor that reads the configuration parameters. 
     * @param name the configuration prefix for this class.
     */
    public ChunksObserver(String name) {
        this.name = name;
        pid = Configuration.getPid(name + "." + PAR_PROT);
        size = Configuration.getInt(name + "." + PAR_SIZE);
        chunks = (Configuration.getInt(name + "." + PAR_CHUNKS, 1));
        seed = CommonState.r.getLastSeed();
        System.err.println("#Chunks Observer is ready");
    }

    public boolean execute() {
        if (CommonState.getTime() == 0) {
            return false;
        }
        if (CommonState.getPhase() == CommonState.POST_SIMULATION) {
            System.err.println("All nodes complete! PostSimulation...");//+".\n"+assenti);
            System.err.println("Esecuzione Observer: Inizio scrittura...");
            try {
                GregorianCalendar tmp = new GregorianCalendar();
                dirname = tmp.get(Calendar.YEAR) + "-" + (tmp.get(Calendar.MONTH) + 1);
                try {
                    if (!new File(dirname).exists()) {
                        new File(dirname).mkdir();
                    }
                } catch (Exception e) {
                    System.err.println(this.name + " - " + e.toString());
                }
                fpeermatrix = "EDPeerMatrix-Seed_" + seed + "-N" + size + "-C" + chunks + "-" + CommonState.getConfig() + "-time_" + tmp.get(Calendar.HOUR_OF_DAY) + "-" + tmp.get(Calendar.MINUTE) + "-" + tmp.get(Calendar.SECOND) + ".gz";
                foperation = "EDOperation-Seed_" + seed + "-N" + size + "-C" + chunks + "-" + CommonState.getConfig() + "-time_" + tmp.get(Calendar.HOUR_OF_DAY) + "-" + tmp.get(Calendar.MINUTE) + "-" + tmp.get(Calendar.SECOND) + ".gz";
                this.peer_matrix = new File(dirname, fpeermatrix);
                this.operation_file = new File(dirname, foperation);
                System.err.println("\tCreazione file :" + peer_matrix.getCanonicalPath());
                System.err.println("\tCreazione file :" + operation_file.getCanonicalPath());
                this.peer_matrix.createNewFile();
                this.operation_file.createNewFile();
                FileOutputStream fosm = new FileOutputStream(peer_matrix);
                FileOutputStream foso = new FileOutputStream(operation_file);
                GZIPOutputStream gzipmatrix = new GZIPOutputStream(fosm);
                GZIPOutputStream gzipoperation = new GZIPOutputStream(foso);
                this.outMatrix = new PrintWriter(gzipmatrix);
                this.outOperation = new PrintWriter(gzipoperation);
                outOperation.write("#Chunk in push, Time in Push, Push Propose, Push success, Fail Push, Chunk in Pull, Time in Pull, pull propose, pull success, Fail Pull\n");
                for (int i = 0; i < Network.size(); i++) {
                    Node nodos = Network.get(i);
                    Alternate protocol = (Alternate) nodos.getProtocol(pid);
                    if (nodos.getID() != protocol.getSource()) {
                    for (int j = 0; j < chunks; j++) {
                        outMatrix.write(protocol.chunk_list[j] + " ");
                    }
                    outMatrix.write("\n");
                    outOperation.write(protocol.getChunkInPush() + " " + protocol.getTimePush() + " " +protocol.getProposePush()+" " +protocol.getSuccessPush()+" " + protocol.getFailPush() + " " +
                            protocol.getChunkInPull() + " " + protocol.getTimePull() + " " +protocol.getProposePull()+" " +protocol.getSuccessPull()+" " + protocol.getFailPull() + " " + protocol.getSkipped());
                            //                            "\t# Node " + nodos.getID() + " Chunks " + protocol.getSize() + " on " + protocol.getNumberOfChunks() + "\n");
                    }
                }
                outMatrix.flush();
                outOperation.flush();

                outMatrix.close();
                outOperation.close();
            } catch (Exception e) {
                System.err.println(e.toString());
            }
            System.err.println("Interleave Observer. Fine Scrittura.");
            return false;
        }

        int count = 0;
        int active = 0;
        boolean notallfinish = false;
        for (int i = 0; i < Network.size(); i++) {
            Alternate protocol = (Alternate) Network.get(i).getProtocol(pid);
            if (protocol.getCycle() != -1) {
                active++;
            }
            if (protocol.getSize() < protocol.getNumberOfChunks() || protocol.getCompleted() == 0) {
                notallfinish = true;
            } else {
                count++;
            }
        }
        Runtime r = Runtime.getRuntime();
        r.gc();
        Alternate protocol = (Alternate) Network.get(Network.size()-1).getProtocol(pid);
        System.err.print("Time " + CommonState.getTime());
        if (notallfinish) {
            System.err.print(": Active " + active + ". Completed " + count + ". Simulation continues...("+ protocol.getFirstChunk()+")\n");//+".\n"+assenti);
            return false;
        } else if ((!notallfinish) && CommonState.getTime() != CommonState.getEndTime()) {
            System.err.println(". All Nodes complete: " + count + ". Set time to end time...");//+".\n"+assenti);
            CommonState.setTime(CommonState.getEndTime());
        }
        return true;
    }
}
