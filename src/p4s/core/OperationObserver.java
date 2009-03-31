/*
 * This class is used to collect statistics on type of operations performed
 */
package p4s.core;

import java.io.*;
import java.util.*;
import java.util.zip.*;
import peersim.config.*;
import peersim.core.*;

/**
 *
 * @author ax
 */
public class OperationObserver implements Control {
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
    private static final String PAR_CHUNKS = "chunks";
    // ///////////////////////////////////////////////////////////////////////
    // Fields
    // ///////////////////////////////////////////////////////////////////////
    /**
     * The name of this observer in the configuration file. Initialized by the
     * constructor parameter.
     */
    private final String name;
    /** Protocol identifier, obtained from config property {@link #PAR_PROT}. */
    private final int pid; 
    private final int size;    
    public PrintWriter gzWriter = null;
    private File operation_size;
    private String foperationsize;
    private String dirname;
    private int oldpush,  actualpush;
    private int oldpull,  actualpull;
    private final int chunks;    
    // ///////////////////////////////////////////////////////////////////////
    // Constructor
    // ///////////////////////////////////////////////////////////////////////
    /**
     * Standard constructor that reads the configuration parameters.
     * 
     * @param name the configuration prefix for this class.
     */
    public OperationObserver(String name) {
        this.name = name;
        pid = Configuration.getPid(name + "." + PAR_PROT);
        size = Configuration.getInt(name + "." + PAR_SIZE);        
        GregorianCalendar tmp = new GregorianCalendar();
        dirname = tmp.get(Calendar.YEAR) + "-" + (tmp.get(Calendar.MONTH) + 1);
        chunks = (Configuration.getInt(name + "." + PAR_CHUNKS, 1));
        this.oldpull = this.actualpull = this.oldpush = this.actualpush = -1;
        System.err.println("#Operation Observer is ready");
        this.actualpull = this.actualpush = 0;
    }

    public boolean execute() {
        try {
            if (!new File(dirname).exists()) {
                new File(dirname).mkdir();
            }
        } catch (Exception e) {
            System.err.println("ERR " + this.name + " - " + e.toString());
        }
        long seed = CommonState.r.getLastSeed();
        foperationsize = "EDOperationSize-" + seed + "-N" + size + "-C" + chunks + "-" + CommonState.getConfig() + ".gz";
        this.operation_size = new File(dirname, foperationsize);
        try {
            FileOutputStream fosm = null;
            if (!this.operation_size.exists()) {
                this.operation_size.createNewFile();
                fosm = new FileOutputStream(operation_size);
            } else {
                fosm = new FileOutputStream(operation_size, true);
            }
            GZIPOutputStream gzipoperationsize = new GZIPOutputStream(fosm);
            this.gzWriter = new PrintWriter(gzipoperationsize);            
            if (CommonState.getPhase() == CommonState.POST_SIMULATION || CommonState.getTime() == CommonState.getEndTime()) {
                System.err.println("Operation Observer. Fine scrittura.");
            } else {
                int pushy, pully;
                pushy = pully = 0;
                for (int i = 0; i < Network.size(); i++) {
                    Alternate protocol = (Alternate) Network.get(i).getProtocol(pid);
                    pushy += protocol.getChunkInPush();
                    pully += protocol.getChunkInPull();
                }
                if (CommonState.getTime() == 0) {
                    this.gzWriter.write("#Time,chunk in push,chunk in pull\n");
                }
                this.gzWriter.write(CommonState.getTime() + " " + (pushy - this.actualpush) + " " + this.actualpush + " " +
                        (pully - this.actualpull) + " " + this.actualpull + "\n");
                this.gzWriter.flush();
                this.actualpush = pushy;
                this.actualpull = pully;
            }
            this.gzWriter.flush();
            this.gzWriter.close();
        } catch (Exception e) {
            System.err.println(e.toString());
        }
        return false;
    }
}
