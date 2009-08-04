package p4s.core;

import bandwidth.core.BandwidthAwareProtocol;
import peersim.config.FastConfig;
import peersim.core.*;
import p4s.util.*;

/**
 * This class is a useful and common data structure
 * that may be used for all kinds of protocols.
 *
 */
public class AlternateDataStructure implements AlternateDataSkeleton, Protocol {

    /**Array that contains the time in which the node receive the i-th chunk*/
    protected long[] chunk_list = null;
    /**Total number of chunks that the node should receive*/
    protected int number_of_chunks;
    /**Source identifier*/
    protected int source;
    /**Cycle of the node : 0 push 1 pull */
    protected int cycle;
    /**Debug level */
    protected int debug;
    /**# of chunks transmitted correctly via push*/
    protected int success_upload;
    /**# of chunks transmitted correctly via pull*/
    protected int success_download;
    /**# of chunks offered in push*/
    protected int push_window;
    /**# of chunks offered in pull*/
    protected int pull_window;
    /**Protocol ID for bandwidth mechanism*/
    protected int bandwidth;
    /**#of chunks received in push*/
    protected int chunkpush;
    /**#of chunks received in pull*/
    protected int chunkpull;
    /**Chunk's size in bits*/
    protected long chunk_size;
    /**Last chunk retrieved via pull*/
    protected int last_chunk_pulled;
    /**Time in which the node has stared to satisfy a pull request, -1 no pull*/
    protected long pulling;
    /**# of failed push*/
    protected int fail_push;
    /**# of failed pull*/
    protected int fail_pull;
    /**Time in which node completes its chunk-list*/
    protected long completed;
    /**Max # of push attempts */
    protected int max_push_attempts;
    /**Max # of pull attempts */
    protected int max_pull_attempts;
    /**Current push attempts */
    protected int push_attempts;
    /**Current pull attempts */
    protected int pull_attempts;
    /**Time spent in push*/
    protected long time_in_push;
    /**Time spent in pull*/
    protected long time_in_pull;
    /**Time needed to change state*/
    protected long switchtime;
    protected long push_propose;
    protected long push_success;
    protected long pull_propose;
    protected long pull_success;
    /**Contains the chunk-id that the source will push*/
//    private static LinkedList lastsrc;
    /**Total neightbor knowledge*/
    private int nk;
    /**Time to emerge new chunk*/
    private long new_chunk_delay;

    public AlternateDataStructure(String prefix) {
        super();
    }

    /**
     * 
     * Clone method
     * 
     */
    public Object clone() {
        AlternateDataStructure clh = null;
        try {
            clh = (AlternateDataStructure) super.clone();
        } catch (CloneNotSupportedException e) {
        } // never happens
        clh.chunk_list = null;// new long[1];
        clh.bandwidth = new Integer(0);
        clh.number_of_chunks = new Integer("0");
        clh.last_chunk_pulled = new Integer("-1");
        clh.completed = new Long("0");
        clh.pulling = new Long("-1");
        clh.debug = new Integer("0");
        clh.fail_pull = new Integer("0");
        clh.fail_push = new Integer("0");
        clh.chunk_size = new Integer("0");
        clh.push_window = new Integer("0");
        clh.cycle = new Integer("0");
        clh.source = new Integer("0");
        clh.success_upload = new Integer("0");
        clh.success_download = new Integer("0");
        clh.chunkpush = new Integer("0");
        clh.chunkpull = new Integer("0");
        clh.max_push_attempts = new Integer("0");
        clh.max_pull_attempts = new Integer("0");
        clh.push_attempts = new Integer("0");
        clh.pull_attempts = new Integer("0");
        clh.time_in_push = new Long("0");
        clh.time_in_pull = new Long("0");
        clh.switchtime = new Long("0");
        clh.push_propose = new Long("0");
        clh.push_success = new Long("0");
        clh.pull_propose = new Long("0");
        clh.pull_success = new Long("0");
        clh.nk = new Integer("0");
        clh.new_chunk_delay = new Long(0);
//        clh.lastsrc = new Integer("0");
        return clh;
    }

    /**
     * 
     * Reset protocol's data structure
     * Invoked in the Initializer
     * 
     */
    public void resetAll() {
        this.chunk_list = null;
        this.last_chunk_pulled = -1;
        this.pulling = -1;
        this.chunk_size = 0;
        this.completed = 0;
        this.push_window = 0;
        this.debug = 0;
        this.bandwidth = 0;
        this.fail_pull = 0;
        this.fail_push = 0;
        this.number_of_chunks = 0;
        this.cycle = -1;
        this.source = 0;
        this.chunkpush = 0;
        this.chunkpull = 0;
        this.max_push_attempts = 0;
        this.max_pull_attempts = 0;
        this.push_attempts = 0;
        this.pull_attempts = 0;
        this.time_in_push = 0;
        this.time_in_pull = 0;
        this.switchtime = 0;
        this.success_download = 0;
        this.success_upload = 0;
//        lastsrc = null;
        this.nk = 0;
        this.new_chunk_delay = 0;
        this.push_propose = this.push_success = this.push_success = this.pull_propose = 0;
    }

    /**
     * This method is invoked in the Initialized, after the reset one.
     * @param items The number of chunks that will be distributed
     *
     * */
    public void Initialize(int items) {
        this.resetAll();
//        this.lastsrc = new LinkedList();
        this.chunk_list = new long[items];
//        this.lastsrc = new LinkedList();
        for (int i = 0; i < items; i++) {
            this.chunk_list[i] = Message.NOT_OWNED;
        }
    }

    /**
     *
     * Set the cycle of the current node {@link interleave.util.Message}
     * @param cycle The cycle node will switch in
     *
     */
    public void setCycle(int _cycle) {
        this.cycle = _cycle;
        this.checkpull();
        if (this.cycle == Message.PULL_CYCLE && this.last_chunk_pulled != -1) {
            if ((this.getSize() + 1) == this.getNumberOfChunks() && this.last_chunk_pulled == (this.getNumberOfChunks() - 1)) {
                this.addChunk(last_chunk_pulled, Message.PULL_CYCLE);
                this.last_chunk_pulled = -1;
            }
        }
    }

    /**
     * The current cycle of the ndoe
     * @return an integer tha identify the state, see (@link interleave.util.Message)
     *
     *
     */
    public int getCycle() {
        return this.cycle;
    }

    /**
     * Set the time to produce a new chunk
     * @param delay time in ms
     */
    public void setNewChunkDelay(long delay) {
        this.new_chunk_delay = delay;
    }

    /**
     * Return the time for producing a new chunk
     * @return Time in ms
     */
    public long getNewChunkDelay() {
        return new_chunk_delay;
    }

    /**
     *
     * Set bandwidth protocol
     * @param bw the protocol identifier (PID) of the protocol that
     * implements the bandwidth mechanism
     *
     */
    public void setBandwidth(int bw) {
        this.bandwidth = bw;
    }

    /**
     *
     * Returnt he PID of the protocol that identify the bandwidth mechanism.
     * @return int PID of the bandwidth protocol
     *
     */
    public int getBandwidth() {
        return this.bandwidth;
    }

    /**
     *
     * Time needs by the node to switch its state,
     * @param long time in ms
     *
     */
    public void setSwitchTime(long time) {
        this.switchtime = time;
    }

    /**
     *
     * Return the time needs by the node to switch its state
     * @returnt the time in ms
     *
     */
    public long getSwitchTime() {
        return this.switchtime;
    }

    /**
     * Set the debug level
     * @param value the level of verbosity 0 up to 10
     */
    public void setDebug(int value) {
        this.debug = value;
    }

    /**
     * Get the debug level
     * @retunr value the level of verbosity 0 up to 10
     */
    public int getDebug() {
        return this.debug;
    }

    /**
     * Set the neighbors knowledge: 0 is no, 1 is complete!
     * @param value the flag of knowledge: from stupid (0) to wise (1)
     */
    public void setNeighborKnowledge(int value) {
        this.nk = value;
    }

    /**
     * Set the max number of push retries
     * @param push_retries max number of push attempts
     */
    public void setPushRetry(int push_retries) {
        this.max_push_attempts = push_retries;
    }

    /**
     * Set the max number of pull retries
     * @param pull_retries max number of pull attempts
     */
    public void setPullRetry(int pull_retries) {
        this.max_pull_attempts = pull_retries;
    }

    /**
     * Get the current number of push attempts
     * @return the number of push attempts
     */
    public int getPushRetry() {
        return this.max_push_attempts;
    }

    /**
     * Get the current number of pull attempts
     * @return the number of pull attempts
     */
    public int getPullRetry() {
        return this.max_pull_attempts;
    }

    /**
     * Add one to the number of push attempts
     */
    public void addPushAttempt() {
        this.push_attempts++;
    }

    /**
     * Add one to the number of push attempts
     */
    public int getPushAttempt() {
        return this.push_attempts;
    }

    /**
     * Remove one to the number of push attempts
     */
    public void remPushAttempt() {
        this.push_attempts--;
    }

    /**
     * Reset the number of push attempts
     */
    public void resetPushAttempt() {
        this.push_attempts = 0;
    }

    /**
     * Add one to the number of pull attempts
     */
    public void addPullAttempt() {
        this.pull_attempts++;
    }

    /**
     * Get the number of pull attempts
     */
    public int getPullAttempt() {
        return this.pull_attempts;
    }

    /**
     * Remove one to the number of pull attempts
     */
    public void remPullAttempt() {
        this.pull_attempts--;
    }

    /**
     * Reset the number of pull attempts
     */
    public void resetPullAttempt() {
        this.pull_attempts = 0;
    }

    /**
     * Return the time spent in push transmission by the node
     * @return the time spent in push
     * */
    public long getTimePush() {
        return this.time_in_push;
    }

    /**
     * Return the time spent in pull transmission by the node
     * @return the time spent in pull
     * */
    public long getTimePull() {
        return this.time_in_pull;
    }

    /**
     * Set the number of chunks the node proposes in push
     * @param int window size for the number of chunks proposed in push
     * */
    public void setPushWindow(int window) {
        this.push_window = window;
    }

    /**
     * Return the number of chunks the node proposes in push
     * @return the window size for the number of chunks proposed in push
     **/
    public int getPushWindow() {
        return this.push_window;
    }

    /**
     * Set the number of chunks the node proposes in pull
     * @param int window size for the number of chunks proposed in pull
     **/
    public void setPullWindow(int window) {
        this.pull_window = window;
    }

    /**
     * Return the number of chunks the node proposes in pull
     * @return the window size for the number of chunks proposed in pull
     **/
    public int getPullWindow() {
        return this.pull_window;
    }

    public long getRTTDelay(Node from, Node to) {
        long delay = DelayedNeighbor.delays[from.getIndex()][to.getIndex()];
        return delay;
    }

    /**
     * Return the number of active uploads
     * @return the number of active uploads
     **/
    public int getActiveUpload(Node node) {
        BandwidthAwareProtocol bap = (BandwidthAwareProtocol) node.getProtocol(this.bandwidth);
        return bap.getActiveUpload();
    }

    public int getActiveUp(Node node) {
        BandwidthAwareProtocol bap = (BandwidthAwareProtocol) node.getProtocol(this.bandwidth);
        return bap.getActiveUp();
    }

    public void addActiveUp(Node node) {
        BandwidthAwareProtocol bap = (BandwidthAwareProtocol) node.getProtocol(this.bandwidth);
        bap.addActiveUp();
    }

    public void remActiveUp(Node node) {
        BandwidthAwareProtocol bap = (BandwidthAwareProtocol) node.getProtocol(this.bandwidth);
        bap.remActiveUp();
    }

    public void resetActiveUp(Node node) {
        BandwidthAwareProtocol bap = (BandwidthAwareProtocol) node.getProtocol(this.bandwidth);
        bap.resetActiveUp();
    }

    public int getActiveDownload(Node node) {
        BandwidthAwareProtocol bap = (BandwidthAwareProtocol) node.getProtocol(this.bandwidth);
        return bap.getActiveDownload();
    }

    public int getActiveDw(Node node) {
        BandwidthAwareProtocol bap = (BandwidthAwareProtocol) node.getProtocol(this.bandwidth);
        return bap.getActiveDw();
    }

    public void addActiveDw(Node node) {
        BandwidthAwareProtocol bap = (BandwidthAwareProtocol) node.getProtocol(this.bandwidth);
        bap.addActiveDw();
    }

    public void remActiveDw(Node node) {
        BandwidthAwareProtocol bap = (BandwidthAwareProtocol) node.getProtocol(this.bandwidth);
        bap.remActiveDw();
    }

    public void resetActiveDw(Node node) {
        BandwidthAwareProtocol bap = (BandwidthAwareProtocol) node.getProtocol(this.bandwidth);
        bap.resetActiveDw();
    }

    public int getPassiveUpload(Node node) {
        BandwidthAwareProtocol bap = (BandwidthAwareProtocol) node.getProtocol(this.bandwidth);
        return bap.getPassiveUpload();
    }

    public int getPassiveUp(Node node) {
        BandwidthAwareProtocol bap = (BandwidthAwareProtocol) node.getProtocol(this.bandwidth);
        return bap.getPassiveUp();
    }

    public void addPassiveUp(Node node) {
        BandwidthAwareProtocol bap = (BandwidthAwareProtocol) node.getProtocol(this.bandwidth);
        bap.addPassiveUp();
    }

    public void remPassiveUp(Node node) {
        BandwidthAwareProtocol bap = (BandwidthAwareProtocol) node.getProtocol(this.bandwidth);
        bap.remPassiveUp();
    }

    public void resetPassiveUp(Node node) {
        BandwidthAwareProtocol bap = (BandwidthAwareProtocol) node.getProtocol(this.bandwidth);
        bap.resetPassiveUp();
    }

    public int getPassiveDownload(Node node) {
        BandwidthAwareProtocol bap = (BandwidthAwareProtocol) node.getProtocol(this.bandwidth);
        return bap.getPassiveDownload();
    }

    public int getPassiveDw(Node node) {
        BandwidthAwareProtocol bap = (BandwidthAwareProtocol) node.getProtocol(this.bandwidth);
        return bap.getPassiveDw();
    }

    public void addPassiveDw(Node node) {
        BandwidthAwareProtocol bap = (BandwidthAwareProtocol) node.getProtocol(this.bandwidth);
        bap.addPassiveDw();
    }

    public void remPassiveDw(Node node) {
        BandwidthAwareProtocol bap = (BandwidthAwareProtocol) node.getProtocol(this.bandwidth);
        bap.remPassiveDw();
    }

    public void resetPassiveDw(Node node) {
        BandwidthAwareProtocol bap = (BandwidthAwareProtocol) node.getProtocol(this.bandwidth);
        bap.resetPassiveDw();
    }

    /**
     * Restituisce la banda minima in upload
     * */
    public long getUploadMin(Node node) {
        return ((BandwidthAwareProtocol) node.getProtocol(this.getBandwidth())).getUploadMin();
    }

    public long getUploadMax(Node node) {
        return ((BandwidthAwareProtocol) node.getProtocol(this.getBandwidth())).getUploadMax();
    }

    /**
     * Restituisce la banda minima in download
     * */
    public long getDownloadMin(Node node) {
        return ((BandwidthAwareProtocol) node.getProtocol(this.getBandwidth())).getUploadMin();
    }

    public long getDownloadMax(Node node) {
        return ((BandwidthAwareProtocol) node.getProtocol(this.getBandwidth())).getUploadMax();
    }

    /**
     * Restiuisce la banda attuale in upload
     * */
    public long getUpload(Node node) {
        return ((BandwidthAwareProtocol) node.getProtocol(this.getBandwidth())).getUpload();
    }

    /**
     * Restituisce la banda attuale in download
     * */
    public long getDownload(Node node) {
        return ((BandwidthAwareProtocol) node.getProtocol(this.getBandwidth())).getDownload();
    }

    public String getBwInfo(Node node) {
        return ((BandwidthAwareProtocol) node.getProtocol(this.getBandwidth())).toString();
    }

    /**
     * Add one to success uploads
     * */
    public void addSuccessUpload() {
        this.success_upload++;
    }

    /**
     * Add one to success uploads
     * */
    public int getSuccessUpload() {
        return this.success_upload;
    }

    /**
     * Reset il numero di upload finiti con successo
     * */
    public void resetSuccessUpload() {
        this.success_upload = 0;
    }

    /**
     * Aggiunge 1 al numero di download finiti con successo
     * */
    public void addSuccessDownload() {
        this.success_download++;
    }

    /**
     * Restituisce il numero di download finiti con successo
     * */
    public int getSuccessDownload() {
        return this.success_download;
    }

    /**
     * Reset il numero di download finiti con successo
     * */
    public void resetSuccessDownload() {
        this.success_download = 0;
    }

    /**
     * Aggiunge 1 al numero di chunk ottenuti mediante push
     * */
    public void addChunkInPush() {
        this.chunkpush++;
    }

    /**
     * Restituisce il numero di chunk ottenuti in push
     * */
    public int getChunkInPush() {
        return this.chunkpush;
    }

    /**
     * Aggiunge 1 al numero di chunk ottenuti in pull
     * */
    public void addChunkInPull() {
        this.chunkpull++;
    }

    /**
     * Restituisce il numero di chunk ottenuti in pull
     * */
    public int getChunkInPull() {
        return this.chunkpull;
    }

    /**
     * Imposta la sorgente della trasmissione
     * */
    public void setSource(int _source) {
        this.source = _source;
    }

    /**
     * Restituisce la sorgente della trasmissione
     * */
    public int getSource() {
        return this.source;
    }

    /**
     * Imposta il numero totale di chunks della trasmissione
     * */
    public void setNumberOfChunks(int _number_of_chunks) {
        this.number_of_chunks = _number_of_chunks;
    }

    /**
     * Restituisce il numero totale di chunks della trasmissione
     * */
    public int getNumberOfChunks() {
        return this.number_of_chunks;
    }

    /**
     * Imposta il tempo in cui il nodo ha completato la ricezione di tutti i chunks
     * */
    public void setCompleted(long value) {
        this.completed = value;
    }

    /**
     * Restituisce il tempo in chui il nodo ha completato la ricezione di tutti i chunks
     * */
    public long getCompleted() {
        return this.completed;
    }

    /**
     * Imposta la dimensione in bit del chunk
     * */
    public void setChunkSize(long chunk_size) {
        this.chunk_size = chunk_size;
    }

    /**
     * Restituisce la dimensione del chunk
     * */
    public long getChunkSize() {
        return this.chunk_size;
    }

    public void addProposePush() {
        this.push_propose++;
    }

    public void addProposePull() {
        this.pull_propose++;
    }

    public void addSuccessPush() {
        this.push_success++;
    }

    public void addSuccessPull() {
        this.pull_success++;
    }

    /**
     * Aggiunge 1 al numero di push falliti
     * */
    public void addFailPush() {
        this.fail_push++;
    }

    /**
     * Restituisce il numero di push falliti
     * */
    public int getFailPush() {
        return this.fail_push;
    }

    /**
     * Aggiunge 1 al numero di pull falliti
     * */
    public void addFailPull() {
        this.fail_pull++;
    }

    /**
     * Restituisce il numero di pull falliti
     * */
    public int getFailPull() {
        return this.fail_pull;
    }

    public void addTimeInPush(long timeinpush) {
        this.time_in_push += timeinpush;
    }

    public void addTimeInPull(long timeinpull) {
        this.time_in_pull += timeinpull;
    }

    public String getConnections() {
        String result = "]] " + this.getSize();// + " : " + this.bitmap();
        return result;
    }

    /**
     * 
     * Set lastpull to the chunk just received in pull
     * 
     */
    public void setLastpull(long _lastpull) {
        this.checkpull();
        if (_lastpull < this.getLast() || _lastpull < this.last_chunk_pulled) {
            this.addChunk((int) _lastpull, Message.PULL_CYCLE);
        } else if (this.last_chunk_pulled == -1) {
            this.last_chunk_pulled = (int) _lastpull;
        } else if (this.last_chunk_pulled < _lastpull) {
            this.addChunk(this.last_chunk_pulled, Message.PULL_CYCLE);
            this.last_chunk_pulled = (int) _lastpull;
        }
        if (this.last_chunk_pulled == (this.getNumberOfChunks() - 1)) {
            this.addChunk(this.last_chunk_pulled, Message.PULL_CYCLE);
            this.last_chunk_pulled = -1;

        }
        return;
    }

    public void checkpull() {
        if (this.last_chunk_pulled != -1 && this.last_chunk_pulled < this.getLast()) {
            int tmp = this.last_chunk_pulled;
            this.last_chunk_pulled = -1;
            this.addChunk(tmp, Message.PULL_CYCLE);
        }
    }

    /**
     * 
     * Il metodo restituisce i chunks ricevuti nell'ultimo pull
     * 
     */
    public int getLastpull() {
        return this.last_chunk_pulled;
    }

    /**
     * 
     * Se il nodo sta servendo un altro nodo in pull, restituisce true,
     * altrimenti false
     * 
     */
    public long isPulling() {
        return this.pulling;
    }

    /**
     * Set pulling state
     */
    public void setPulling() {
        this.pulling = CommonState.getTime();
    }

    /**
     * ReSet pulling state
     */
    public void resetPulling() {
        this.pulling = -1;
    }

    /**
     * Set a chunk in download
     */
    public void setInDown(String chunk) {
        long index = Long.parseLong(chunk.substring(chunk.indexOf(":") + 1,
                chunk.length()));
        this.chunk_list[(int) (index)] = Message.IN_DOWNLOAD;
    }

    /**
     * Set a chunk in download
     */
    public void setInDown(long index) {
        if (this.chunk_list[(int) (index)] == Message.NOT_OWNED) {
            this.chunk_list[(int) (index)] = Message.IN_DOWNLOAD;
        }
    }

    public void resetInDown(long index) {
        if (this.chunk_list[(int) (index)] == Message.IN_DOWNLOAD) {
            this.chunk_list[(int) (index)] = Message.NOT_OWNED;
        }
    }

    public String bitmap() {
        String res = "";
        for (int i = 0; i < this.chunk_list.length; i++) {
            res += (this.normalize(this.chunk_list[i]) > Message.OWNED ? "1" : (this.chunk_list[i] == Message.IN_DOWNLOAD) ? "!" : "0") + (i % 10 == 9 ? "," : "");
        }
        return res;
    }

    /**
     * 
     * Il metodo restituisce l'ultimo chunk che il nodo possiede il lista se la
     * lista è vuota restituisce -1; Non vengono presi in considerazione i
     * chunks in download
     * 
     */
    public int getLast() {
        int last = -1;
        for (int i = this.chunk_list.length - 1; i >= 0; i--) {
            if (normalize(this.chunk_list[i]) > Message.OWNED) {
                return i;
            }
        }
        return last;
    }

    public void enqueueChunk(int new_chunk) {
        this.chunk_list[new_chunk] = 1;
    }

    public int getFirstChunk() {
        int chunktopush = 0;
        while (chunktopush < this.chunk_list.length && this.chunk_list[chunktopush] != 1 && this.chunk_list[chunktopush] != 2) {
            chunktopush++;
        }
        if ((chunktopush + 1 == this.chunk_list.length && this.chunk_list[chunktopush] != 1) || chunktopush >= this.chunk_list.length) {
            chunktopush = -1;
        }

        if (debug > 3) {
            System.out.println("chunk to push is " + chunktopush);
        }
        return chunktopush;
    }

    public void markChunk(int value) {
        if (value + 1 == this.number_of_chunks) {
            if (debug > 3) {
                System.out.println("Setting node as completed " + value);
            }
            this.setCompleted(CommonState.getTime());
        }
        if (this.chunk_list[value] == 2) {
            if (debug > 3) {
                System.out.print("Setting chunk as transmitted " + value);
            }
            if (debug > 3) {
                System.out.println(" >> removing " + value);
            }
            this.chunk_list[value] = CommonState.getTime();
        }
    }

    public void unmarkChunk(int value) {
        if (this.chunk_list[value] == 2) {
            if (debug > 3) {
                System.out.print("Setting chunk as not-transmitted " + value);
            }
            this.chunk_list[value] = 1;
            System.out.println(", now is " + this.chunk_list[value]);
        }
    }

    /**
     * 
     * Il metodo restituisce gli ultimi chunks posseduti dal nodo
     * @param elements numero di chunk con id più alto posseduti dal nodo
     * 
     * @return array di interi di dimensione minore o uguale ad @elements contenente gli id più alti
     * 
     * */
    public int[] getLast(int elements) {
        if (this.getSize() < elements) {
            elements = this.getSize();
        }
        int result[] = new int[elements];
        int index = 0;
        int count = 0;
        while (elements > 0 && count < this.chunk_list.length) {
            int id = (this.chunk_list.length - count - 1);
            if (this.chunk_list[id] != Message.IN_DOWNLOAD && this.chunk_list[id] != Message.NOT_OWNED && this.last_chunk_pulled != id) {
                result[index++] = id;
                elements--;
            }
            count++;
        }
        if (elements > 0) {
            int temp[] = new int[result.length - elements];
            System.arraycopy(result, 0, temp, 0, temp.length);
            result = temp;
        }
        return result;
    }

    public long normalize(long chunktime) {
        if (chunktime != Message.OWNED && chunktime != Message.NOT_OWNED && chunktime != Message.IN_DOWNLOAD) {
            return Math.abs(chunktime);
        } else {
            return chunktime;
        }
    }

    /**
     * 
     * Il metodo restituisce il chunk con l'identificativo passato se posseduto
     * dal nodo, se il chunk è in download restituisce la costante
     * {@value Message.CHUNK_IN_DOWNLOAD}, altrimenti restituisce null;
     * 
     * @param index chunk id
     * @return long time at which the node received this chunk
     */
    public long getChunk(int index) {
        return normalize(this.chunk_list[index]);
    }

    public int getChunks(int[] index) {
        int owned = 0;
        for (int i = 0; i < index.length; i++) {
            if (this.getChunk(index[i]) > Message.OWNED) {
                owned++;
            }
        }
        return owned;
    }

    /**
     * 
     * Il metodo aggiunge un chunk alla lista di chunks che il nodo
     * possiede, restituisce vero se il chunk non si possedeva già, false se il
     * chunk si possedeva
     * 
     * @param chunk indice del chunks
     * @param method metodo in cui riceve il chunk
     * @return true se il chunk viene aggiungo, false se si possedeva già
     * 
     */
    public boolean addChunk(int chunk, int method) {
        if ((this.chunk_list[chunk] == Message.NOT_OWNED) || (this.chunk_list[chunk] == Message.IN_DOWNLOAD)) {
            this.chunk_list[chunk] = CommonState.getTime();
            if (method == Message.PULL_CYCLE) {
                this.chunk_list[chunk] *= -1;
                this.chunkpull++;
            } else {
                this.chunkpush++;
                this.checkpull();
            }
        }
        if (this.getSize() == this.getNumberOfChunks() && this.getCompleted() == 0) {
            this.setCompleted(CommonState.getTime());
        }
        return true;
    }

    public boolean skipChunk(int chunk) {
        if (this.chunk_list[chunk] == Message.NOT_OWNED) {
            this.chunk_list[chunk] = Message.SKIPPED;
        }
        if (this.getSize() == this.getNumberOfChunks() && this.getCompleted() == 0) {
            this.setCompleted(CommonState.getTime());
        }
        return true;
    }

    public boolean skipChunk(int chunks[]) {
        for (int k = 0; k < chunks.length; k++) {
            if (this.chunk_list[chunks[k]] == Message.NOT_OWNED) {
                this.chunk_list[chunks[k]] = Message.SKIPPED;
            }
            if (this.getSize() == this.getNumberOfChunks() && this.getCompleted() == 0) {
                this.setCompleted(CommonState.getTime());
            }
        }
        return true;
    }

    /**
     * 
     * Restituisce il numero di chunk posseduti dal nodo, sono esclusi i chunks
     * in Download
     * 
     */
    public int getSize() {
        int size = 0;
        for (int i = 0; i < this.chunk_list.length; i++) {
//            System.out.println(normalize(chunk_list[i]) + " i "+i);
            if (normalize(this.chunk_list[i]) > Message.OWNED || this.chunk_list[i] == Message.SKIPPED) {
                size++;
            }
        }
        return size;
    }

    public int getLeast() {
        int least = -1;
        int max_chunk = this.getLast();
        for (int i = 0; i < this.chunk_list.length && i < max_chunk; i++) {
            if (this.chunk_list[i] == Message.NOT_OWNED) {
                return i;
            }
        }
        return least;
    }

    /**
     * 
     * Restituisce il chunk con id minore non posseduto Se il nodo possiede
     * tutti i chunks restituisce null;
     */
    public int[] getLeast(int elements) {
        int result[] = new int[elements];
        int index = 0;
        int max_chunk = this.getLast();
        if (max_chunk == -1) {
            result[index++] = 0;
            elements--;
        } else {
            for (int i = 0; i < this.chunk_list.length && i < max_chunk && elements > 0; i++) {
                if (this.chunk_list[i] == Message.NOT_OWNED) {// && i != this.last_chunk_pulled) {
                    result[index++] = i;
                    elements--;
                }
            }
        }
        if (elements > 0) {
            int temp[] = new int[result.length - elements];
            System.arraycopy(result, 0, temp, 0, temp.length);
            result = temp;
        }
        if (result.length == 0) {
            return null;
        } else {
            return result;
        }
    }

    /**
     * Stampa le informazioni sul nodo
     * */
    public String toString(Node node) {
        String result = "Nodo " + node.getID() + ", Time " + CommonState.getTime() + " , Fail Push " + this.fail_push + ", Fail Pull " + this.fail_pull + ", Lista " + this.getSize();
        if (this.getSize() == this.getNumberOfChunks()) {
            result += " >>> ha tutti i chunks.";
        } else {
            result += ".";
        }
        return result;
    }
//
//    /**
//     * Restituisce un vicino del nodo in base al peer selection algorithm in uso
//     * */
//    public Node getTargetNeighbor(Node node, int pid) {
//        DelayedNeighbor net = (DelayedNeighbor) node.getProtocol(FastConfig.getLinkable(pid));
//        if (net.getCurrent() == null) {
//            net.setCurrent(node);
//            net.setChunks(this.number_of_chunks);
//        }
//        if (this.getDebug() >= 10) {
//            System.out.println("\tNodo " + node.getID() + "\n\t" + net);
//        }//No knowledge about the neighborhood
//        NeighborElement candidate = net.getTargetNeighbor();
//        if (this.getDebug() >= 10) {
//            System.out.println("\tNodo " + node.getID() + " selects candidate " + candidate + " (Source is " + this.getSource() + ") ");
//        }
//        return candidate.getNeighbor();
//    }

    /**
     * Given a neighbor, returns the NeighborElement associated
     * @param node current node
     * @param target neighbor needed
     * @param pid protocol id
     * @return The NeighborElement instance associated to target node
     */
    public NeighborElement getNeighbor(Node node, Node target, int pid) {
        DelayedNeighbor net = (DelayedNeighbor) node.getProtocol(FastConfig.getLinkable(pid));
        if (net.getCurrent() == null) {
            net.setCurrent(node);
            net.setChunks(this.number_of_chunks);
            if (net.getNeighbor(Network.get(this.source)) != null) {
                net.setBannedPeer(Network.get(this.getSource()));
            }
        }
        if (this.getDebug() >= 10) {
            System.out.println("\tNodo " + node.getID() + "\n\t" + net);
        }//No knowledge about the neighborhood
        NeighborElement candidate = net.getNeighbor(target);
        if (this.getDebug() >= 10) {
            System.out.println("\tNodo " + node.getID() + " selects candidate " + candidate + " (Source is " + this.getSource() + ") ");
        }
        if (candidate != null) {
            return candidate;
        }
        return null;
    }

//XXX e` da capire se nel push conviene fare prima il peer selection e poi il piece o viceversa.
    /***
     * Return a target node to push, in according to the peer selection policies,
     * selecting the target peer among a subset of peers which don't have at least one of the pushed chunks
     * @param chunks array of chunks to push
     * @param node current node
     * @param pid procol id
     * @return Target node
     */
    public Node getTargetNeighborPush(int chunks[], Node node, int pid) {
        DelayedNeighbor net = (DelayedNeighbor) node.getProtocol(FastConfig.getLinkable(pid));
        if (net.getCurrent() == null) {
            net.setCurrent(node);
            net.setChunks(this.number_of_chunks);
            if (net.getNeighbor(Network.get(this.source)) != null) {
                net.setBannedPeer(Network.get(this.getSource()));
            }
        }
        if (this.getDebug() >= 10) {
            System.out.println("\tNodo " + node.getID() + " push selection\n\t" + net);
        }
        NeighborElement candidate = net.getTargetNeighbor(chunks, Message.NOT_OWNED);
        if (this.getDebug() >= 10) {
            System.out.println("\tNodo " + node.getID() + " selects candidate " + candidate + " (Source is " + this.getSource() + ") ");
        }
        if (candidate == null) {
            return null;
        }
        candidate.setContactTime(CommonState.getTime());
        return candidate.getNeighbor();
    }

    /***
     * Return a target node to pull, in according to the peer selection policies,
     * selecting the target peer among a subset of peers which have at least one of the desired chunks
     * @param chunks array of chunks needed
     * @param node current node
     * @param pid procol id
     * @return Target node
     */
    public Node getTargetNeighborPull(int chunks[], Node node, int pid) {
        DelayedNeighbor net = (DelayedNeighbor) node.getProtocol(FastConfig.getLinkable(pid));
        if (net.getCurrent() == null) {
            net.setCurrent(node);
            net.setChunks(this.number_of_chunks);
            if (net.getNeighbor(Network.get(this.source)) != null) {
                net.setBannedPeer(Network.get(this.getSource()));
            }
        }
        if (this.getDebug() >= 10) {
            System.out.println("\tNodo " + node.getID() + " pull selection\n\t" + net);
        }//No knowledge about the neighborhood
        NeighborElement candidate = net.getTargetNeighbor(chunks, Message.OWNED);
        if (this.getDebug() >= 10) {
            System.out.println("\tNodo " + node.getID() + " selects candidate " + candidate + " (Source is " + this.getSource() + ") ");
        }
        if (candidate == null) {
            return null;
        }
        return candidate.getNeighbor();
    }

    /**
     * Return a printable version of the node's neighborhood
     * @param node current node
     * @param pid protocol id
     * @return string containing the neighborhood
     */
    public String getNeighborhood(Node node, int pid) {
        Linkable linkable = (Linkable) node.getProtocol(FastConfig.getLinkable(pid));
        String results = "Node " + node.getID() + ": " + linkable.degree() + " [ ";
        for (int i = 0; i < linkable.degree(); i++) {
            results += linkable.getNeighbor(i).getID() + ", ";
        }
        results += " ]";
        return results;
    }

    /**
     * Restituisce per ogni chunk, il tempo in cui è stato ricevuto
     * */
    public long getChunkInfo(long id) {
        int idi = (int) id;
        return this.chunk_list[idi];
    }
}
