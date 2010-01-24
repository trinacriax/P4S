package p4s.core;

import bandwidth.core.BandwidthAwareProtocol;
import peersim.config.FastConfig;
import peersim.core.*;
import p4s.util.*;

/**
 * This class is a generic data structure for push/pull protocols.
 * It consists in several fields and methods which reflect the protocol state.
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
    /** Number of chunks transmitted correctly via push*/
    protected int success_upload;
    /** Number of chunks transmitted correctly via pull*/
    protected int success_download;
    /** Number of chunks offered in push*/
    protected int push_window;
    /** Number of chunks offered in pull*/
    protected int pull_window;
    /**Protocol ID for bandwidth mechanism*/
    protected int bandwidth;
    /**Chunk's size in bits*/
    protected long chunk_size;
    /**Last chunk retrieved via pull*/
    protected int last_chunk_pulled;
    /**Time in which the node has stared to satisfy a pull request, -1 no pull*/
    protected long pulling;
    /**Time in which node completes its chunk-list*/
    protected long completed;
    /**Max number of push attempts */
    protected int max_push_attempts;
    /**Max number of pull attempts */
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
    /**Passive Push propose*/
    protected int push_propose_p;
    /**Passive Push successuful*/
    protected int push_success_p;
    /**Passive Push failed*/
    protected int push_failed_p;
    /**Passive Pull propose */
    protected int pull_propose_p;
    /**Passive Pull success*/
    protected int pull_success_p;
    /**Passive Pull failed*/
    protected int pull_failed_p;
    /**Active Push propose */
    protected int push_propose_a;
    /**Active Push success */
    protected int push_success_a;
    /**Active Push failed */
    protected int push_failed_a;
    /**Active Pull propose */
    protected int pull_propose_a;
    /**Active Pull success */
    protected int pull_success_a;
    /**Active Pull failed*/
    protected int pull_failed_a;
    /**Total neightbor knowledge*/
    private int nk;
    /**Playout delay. Infinite is -1, otherwise time in ms.*/
    protected long playout;
    /**Time to emerge new chunk*/
    private long new_chunk_delay;
    private long min_del;
    private long max_del;
    private Node current;
    private int pull_rounds;

    public AlternateDataStructure(String prefix) {
        super();
    }

    /**
     * 
     * Clone method
     * 
     */
    public Object clone() {
        AlternateDataStructure ads = null;
        try {
            ads = (AlternateDataStructure) super.clone();
        } catch (CloneNotSupportedException e) {
        } // never happens
        ads.chunk_list = null;// new long[1];
        ads.bandwidth = new Integer(0);
        ads.number_of_chunks = new Integer(0);
        ads.last_chunk_pulled = new Integer(-1);
        ads.completed = new Long(0);
        ads.pulling = new Long(-1);
        ads.debug = new Integer(0);

        /**Passive Push propose*/
        push_propose_p = new Integer(0);
        /**Passive Push successuful*/
        push_success_p = new Integer(0);
        /**Passive Push failed*/
        push_failed_p = new Integer(0);

        /**Passive Pull propose */
        pull_propose_p = new Integer(0);
        /**Passive Pull success*/
        pull_success_p = new Integer(0);
        /**Passive Pull failed*/
        pull_failed_p = new Integer(0);

        /**Active Push propose */
        push_propose_a = new Integer(0);
        /**Active Push success */
        push_success_a = new Integer(0);
        /**Active Push failed */
        push_failed_a = new Integer(0);

        /**Active Pull propose */
        pull_propose_a = new Integer(0);
        /**Active Pull success */
        pull_success_a = new Integer(0);
        /**Active Pull failed*/
        pull_failed_a = new Integer(0);

        ads.chunk_size = new Integer(0);
        ads.push_window = new Integer(0);
        ads.cycle = new Integer(0);
        ads.source = new Integer(0);
        ads.success_upload = new Integer(0);
        ads.success_download = new Integer(0);

        ads.max_push_attempts = new Integer(0);
        ads.max_pull_attempts = new Integer(0);
        ads.push_attempts = new Integer(0);
        ads.pull_attempts = new Integer(0);
        ads.time_in_push = new Long(0);
        ads.time_in_pull = new Long(0);
        ads.switchtime = new Long(0);
        ads.playout = new Long(0);
        ads.nk = new Integer(0);
        ads.new_chunk_delay = new Long(0);
        ads.min_del = new Long(0);
        ads.max_del = new Long(0);
        ads.pull_rounds = new Integer(0);
        ads.current = null;
        return ads;
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
        push_propose_p = 0;
        /**Passive Push successuful*/
        push_success_p = 0;
        /**Passive Push failed*/
        push_failed_p = 0;

        /**Passive Pull propose */
        pull_propose_p = 0;
        /**Passive Pull success*/
        pull_success_p = 0;
        /**Passive Pull failed*/
        pull_failed_p = 0;

        /**Active Push propose */
        push_propose_a = 0;
        /**Active Push success */
        push_success_a = 0;
        /**Active Push failed */
        push_failed_a = 0;

        /**Active Pull propose */
        pull_propose_a = 0;
        /**Active Pull success */
        pull_success_a = 0;
        /**Active Pull failed*/
        pull_failed_a = 0;
        this.number_of_chunks = 0;
        this.cycle = -1;
        this.source = 0;
        this.max_push_attempts = 0;
        this.max_pull_attempts = 0;
        this.push_attempts = 0;
        this.pull_attempts = 0;
        this.time_in_push = 0;
        this.time_in_pull = 0;
        this.switchtime = 0;
        this.success_download = 0;
        this.success_upload = 0;
        this.playout = 0;
        this.nk = 0;
        this.new_chunk_delay = 0;
        this.min_del = this.max_del = 0;
        this.current = null;
        this.pull_rounds = 0;
    }

    /**
     * This method is invoked in the Initialized, after the reset one.
     * @param items The number of chunks that will be distributed
     *
     * */
    public void Initialize(int items) {
        System.err.println("ID : "+CommonState.getNode().getID()+" Idx "+CommonState.getNode().getIndex() );
        this.resetAll();
        this.chunk_list = new long[items];
        for (int i = 0; i < items; i++) {
            this.chunk_list[i] = Message.NOT_OWNED;
        }
    }

    /**
     *
     * Set the cycle of the current node {@link p4s.util.Message}.
     * @param _cycle The cycle node will switch in.
     *
     */
    public void setCycle(int _cycle) {
        this.cycle = _cycle;
        this.checkpull();
        if (this.cycle == Message.PULL_CYCLE && this.last_chunk_pulled != -1) {
            if ((this.getAllChunks() + 1) == this.getNumberOfChunks() && this.last_chunk_pulled == (this.getNumberOfChunks() - 1)) {
                this.addChunk(last_chunk_pulled, Message.PULL_CYCLE);
                this.last_chunk_pulled = -1;
            }
        }
    }

    /**
     * The current cycle of the node.
     * @return an integer tha identify the state, see (@link interleave.util.Message).
     */
    public int getCycle() {
        return this.cycle;
    }

    /**
     * Set the source node
     * @param _source
     */
    public void setSource(int _source) {
        this.source = _source;
    }

    /**
     * Get the source node.
     * @return Source node.
     */
    public int getSource() {
        return this.source;
    }

    /**
     * Set the number of chunks that compose the streaming session.
     * @param _number_of_chunks chunks of the whole session.
     */
    public void setNumberOfChunks(int _number_of_chunks) {
        this.number_of_chunks = _number_of_chunks;
    }

    /**
     * Get the total number of chunks that compose the session.
     * @return Chunks that compose the session.
     */
    public int getNumberOfChunks() {
        return this.number_of_chunks;
    }

    /**
     * Set the time in which node has completed to receive chunks.
     * @param value Time in which node finishes to receive chunks.
     */
    public void setCompleted(long value) {
        this.completed = value;
    }

    /**
     * Set the node as completed.
     * @return The time in which the node has completed to receive chunks, negative value if it is still working to retrieve chunks.
     */
    public long getCompleted() {
        return this.completed;
    }

    /**
     * Set the chunk size.
     * @param chunk_size Chunk size in bits.
     */
    public void setChunkSize(long chunk_size) {
        this.chunk_size = chunk_size;
    }

    /**
     * Get the chunk size in bits.
     * @return Chunk size in bits.
     */
    public long getChunkSize() {
        return this.chunk_size;
    }

    /**
     * Set the time to produce a new chunk.
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
     * @param bw the protocol identifier (PID) of
     * the protocol that implements the bandwidth mechanism
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
     * @param time switch time in ms
     *
     */
    public void setSwitchTime(long time) {
        this.switchtime = time;
    }

    /**
     *
     * Return the time needs by the node to switch its state
     * @return the time in ms
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
     * @return value the level of verbosity 0 up to 10
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
     * Add a push attempt
     */
    public void addPushAttempt() {
        this.push_attempts++;
    }

    /**
     * Get the current number of push attempts
     * @return number of push attempts
     */
    public int getPushAttempt() {
        return this.push_attempts;
    }

    /**
     * Remove a push attempt
     */
    public void remPushAttempt() {
        this.push_attempts--;
    }

    /**
     * Reset the number of push attempt
     */
    public void resetPushAttempt() {
        this.push_attempts = 0;
    }

    /**
     * Add a pull attempt
     */
    public void addPullAttempt() {
        this.pull_attempts++;
    }

    /**
     * Get the number of pull attempts
     * @return number of pull attempts
     */
    public int getPullAttempt() {
        return this.pull_attempts;
    }

    /**
     * Remove a pull attempt
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
     * Return the time spent in push by the node
     * @return the time spent in push
     * */
    public long getTimePush() {
        return this.time_in_push;
    }

    /**
     * Return the time spent in pull by the node
     * @return the time spent in pull
     * */
    public long getTimePull() {
        return this.time_in_pull;
    }

    /**
     * Set the number of chunks the node proposes in push
     * @param window number of chunks offered in push
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
     * @param window number of chunks requested in pull
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

    /**
     * Get the RTT delay between nodes
     * @param from Current node
     * @param to Neighbor node
     * @return RTT delay between nodes.
     */
    public long getRTTDelay(Node from, Node to) {
        long delay = DelayedNeighbor.delays[from.getIndex()][to.getIndex()];
        return delay;
    }

    /**
     * Add one to success uploads
     * */
    public void addSuccessUpload() {
        this.success_upload++;
    }

    /**
     * Add one to success uploads
     * @return number of successful upload
     * */
    public int getSuccessUpload() {
        return this.success_upload;
    }

    /**
     * Reset number of success uploads.
     */
    public void resetSuccessUpload() {
        this.success_upload = 0;
    }

    /**
     * Add a success download.
     */
    public void addSuccessDownload() {
        this.success_download++;
    }

    /**
     * Get the number of success downloads
     * @return success downloads
     */
    public int getSuccessDownload() {
        return this.success_download;
    }

    /**
     * Reset the number of success downloads.
     */
    public void resetSuccessDownload() {
        this.success_download = 0;
    }

    /**
     * Add a push success
     */
    public void addActivePushSuccess() {
        this.push_success_a++;
    }

    /**
     * Get the number of active successful push
     * @return Number of active successful push
     */
    public int getActivePushSuccess() {
        return this.push_success_a;
    }

    /**
     * Add a active push propose.
     */
    public void addActivePushPropose() {
        this.push_propose_a++;
    }

    /**
     * Get the number of active push proposes.
     * @return Number of active push proposes.
     */
    public int getActivePushPropose() {
        return this.push_propose_a;
    }

    /**
     * Add an active failed push.
     */
    public void addActivePushFailed() {
        this.push_failed_a++;
    }

    /**
     * Get the number of active failed push
     * @return Failed active push.
     */
    public int getActivePushFailed() {
        return this.push_failed_a;
    }

    /**
     * Add a successful active pull.
     */
    public void addActivePullSuccess() {
        this.pull_success_a++;
    }

    /**
     * Get the number of successful active pull.
     * @return Successful active pull.
     */
    public int getActivePullSuccess() {
        return this.pull_success_a;
    }

    /**
     * Add an active pull request
     */
    public void addActivePullPropose() {
        this.pull_propose_a++;
    }

    /**
     * Add an active pull request.
     * @return Number active pull request.
     */
    public int getActivePullRequest() {
        return this.pull_propose_a;
    }

    /**
     * Add an active failed pull.
     */
    public void addActivePullFailed() {
        this.pull_failed_a++;
    }

    /**
     * Get the number of active pull failed.
     * @return failed active pull.
     */
    public int getActivePullFailed() {
        return this.pull_failed_a;
    }

    /**
     * Add a passive successful push.
     */
    public void addPassivePushSuccess() {
        this.push_success_p++;
    }

    /**
     * Get passive successful push.
     * @return Passive successful push.
     */
    public int getPassivePushSuccess() {
        return this.push_success_p;
    }

    /**
     * Add a passive push propose.
     */
    public void addPassivePushPropose() {
        this.push_propose_p++;
    }

    /**
     * Get the passive push proposes.
     * @return Passive push proposes.
     */
    public int getPassivePushPropose() {
        return this.push_propose_p;
    }

    /**
     * Add a passive push failed.
     */
    public void addPassivePushFailed() {
        this.push_failed_p++;
    }

    /**
     * Get the passive failed pushes.
     * @return Passive failed pushes.
     */
    public int getPassivePushFailed() {
        return this.push_failed_p;
    }

    /**
     * Add a passive pull success.
     */
    public void addPassivePullSuccess() {
        this.pull_success_p++;
    }

    /**
     * Get the passive successful pull.
     * @return Number of passive succesful pull.
     */
    public int getPassivePullSuccess() {
        return this.pull_success_p;
    }

    /**
     * Add a passive pull propose.
     */
    public void addPassivePullPropose() {
        this.pull_propose_p++;
    }

    /**
     * Get the passive pull request.
     * @return number of passive pull request.
     */
    public int getPassivePullRequest() {
        return this.pull_propose_p;
    }

    /**
     * Add a passive failed pull.
     */
    public void addPassivePullFailed() {
        this.pull_failed_p++;
    }

    /**
     * Get the passive failed pull.
     * @return Passive failed pull.
     */
    public int getPassivePullFailed() {
        return this.pull_failed_p;
    }

    /**
     * Add time spent in push.
     * @param timeinpush time spent in push.
     */
    public void addTimeInPush(long timeinpush) {
        this.time_in_push += timeinpush;
    }

    /**
     * Add time spent in pull.
     * @param timeinpull time spent in pull.
     */
    public void addTimeInPull(long timeinpull) {
        this.time_in_pull += timeinpull;
    }

    /**
     * Set the playout time of the node.
     * @param time_sec node plyout time in ms, -1 is infinite.
     */
    public void setPlayoutTime(int time_sec) {
        this.playout = new Long(time_sec);
    }

    /**
     * Get the playout time.
     * @return Playout time of the node.
     */
    public long getPlayoutTime() {
        return this.playout;
    }

    /**
     * Compute the deadline for a chunk.
     * @param chunkid Chunk of interest.
     * @return Deadline for the given chunk.
     */
    public long getDeadline(int chunkid) {
        if (this.playout == -1) {
            return -1;
        } else {
            return this.playout + this.new_chunk_delay * chunkid;
        }

    }

    /**
     * Se the max e min RTT delays.
     * @param node current node.
     * @param pid Protocol id.
     */
    public void setRTTDelays(Node node, int pid) {
        DelayedNeighbor net = (DelayedNeighbor) node.getProtocol(FastConfig.getLinkable(pid));
        this.min_del = net.getMinRTT();
        this.max_del = net.getMaxRTT();
    }

    /**
     * Set the number of pull rounds for the pullable set.
     * When a node is unsuccessful pulled for a given set of chunks,
     * this set is marked as missed in that node.
     * This neighbor, however, may receive the some chunks of the set later,
     * so it can be pulled for those chunks marked in the past after several pull rounds.
     * @param rounds
     */
    public void setPullRounds(int rounds) {
        this.pull_rounds = rounds;
    }

    /**
     * Get the pull rounds.
     * @return Number of pull to re-add a node in the set of candidates.
     */
    public int getPullRounds() {
        return this.pull_rounds;
    }

    /**
     * Check whether the chunk is pullable or its deadline is too close, thus it will be discarded.
     * @param chunkid Chunk identier to check.
     * @return True if there is enough time to pull the chunk, false otherwise.
     */
    public boolean isPullable(int chunkid) {
        if (this.playout < 0) {
            if (debug >= 4) {
                System.out.println("\tPlayout is set to infinity, " + chunkid + " will be pullable for ever.");
            }
            return true;
        }
        long time_available = getDeadline(chunkid) - CommonState.getTime();
        if (debug >= 4) {
            System.out.print("\tTime available is " + time_available + " (" + getDeadline(chunkid) + "); ");
        }
        if (time_available < 0 && this.playout != -1) {
            if (this.chunk_list[chunkid] == Message.NOT_OWNED) {
                this.skipChunk(chunkid);
                if (debug >= 4) {
                    System.out.print("is lower than 0, no more time to retrieve " + chunkid + " SKIP IT.");

                }
                this.checkCompleted();
            }
            if (debug >= 4) {
                System.out.println(current.getID() + " It is strange: at time " + CommonState.getTime() + " tries to pull chunk " + chunkid +
                        " with deadline " + time_available + ". It should be already marked as skipped! Playout " + this.playout + ".");
            }
            return false;
        }
        double uptime = Math.ceil((this.chunk_size * 1.0 / (this.getUploadMax(this.current) * 1.0)) * Message.TIME_UNIT);
        long time_needed = Math.round(this.max_del + uptime);//XXX to up[date, nodes can learn the upload speed/time of its neighbors from history
        if (debug >= 4) {
            System.out.print("Time needed to receive the chunk " + chunkid + " (Size " + this.chunk_size + ", Upload " + this.getUploadMax(this.current) + ") is " + time_needed + "; ");
        }
        if (time_available >= time_needed) {
            if (debug >= 4) {
                System.out.println("so we have time to search it for pull");
            }
            return true;
        } else {
            if (this.chunk_list[chunkid] == Message.NOT_OWNED) {
                this.skipChunk(chunkid);
                if (debug >= 4) {
                    System.out.println("we have no more time to retrieve it, SKIP");
                }
            }
            return false;
        }
    }

    /**
     * Set lastpull to the chunk just received in pull
     * @param _lastpull last chunk pulled.
     */
    public void setLastpull(long _lastpull) {
        this.checkpull();
        if (_lastpull < this.getLatest() || _lastpull < this.last_chunk_pulled) {
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

    /**
     * Check the last pull, eventually add it in the list.
     */
    public void checkpull() {
        if (this.last_chunk_pulled != -1 && this.last_chunk_pulled < this.getLatest()) {
            int tmp = this.last_chunk_pulled;
            this.last_chunk_pulled = -1;
            this.addChunk(tmp, Message.PULL_CYCLE);
        }
    }

    /**
     * Get the last chunk pulled.
     * @return last chunk pulled.
     */
    public int getLastpull() {
        return this.last_chunk_pulled;
    }

    /**
     * Check whether the node is already satistying a pull or not.
     * @return The time in which has started the pull, -1  if it is idle.
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
     * Set the chunk in download.
     * @param index Chunk identifier to set in download.
     */
    public void setInDown(long index) {
        if (this.chunk_list[(int) (index)] == Message.NOT_OWNED) {
            this.chunk_list[(int) (index)] = Message.IN_DOWNLOAD;
        }
    }

    /**
     * Reset the state of the chunk in download, as not owned.
     * @param index Chunk identifier to set.
     */
    public void resetInDown(long index) {
        if (this.chunk_list[(int) (index)] == Message.IN_DOWNLOAD) {
            this.chunk_list[(int) (index)] = Message.NOT_OWNED;
        }
    }

    /**
     * Print the bitmap representing the chunks of the node.
     * @return A string which represents the node bitmap.
     */
    public String bitmap() {
        String res = "";
        for (int i = 0; i < this.chunk_list.length; i++) {
            res += (this.normalize(this.chunk_list[i]) > Message.OWNED ? "1" : (this.chunk_list[i] == Message.IN_DOWNLOAD) ? "!" : "0") + (i % 10 == 9 ? "," : "");
        }
        return res;
    }

    /**
     * Get the latest chunk owned.
     * @return latest chunk owned.
     */
    public int getLatest() {
        int last = -1;
        for (int i = this.chunk_list.length - 1; i >= 0; i--) {
            if (normalize(this.chunk_list[i]) > Message.OWNED) {
                return i;
            }
        }
        return last;
    }

    /**
     * Source enqueues new chunks that will be pushed. Each chunk assumes the following values: 1 means that it has to be pushed,
     * 2 means that the source is pushing that chunk while a value greater than 2 is the time in which the source has transmitted that chunk.
     * @param new_chunk
     */
    public void enqueueChunk(int new_chunk) {
        this.chunk_list[new_chunk] = 1;
    }

    /**
     * Get the first chunk that the source has to push.
     * @return The chunk to be pushed.
     */
    public int getFirstChunk() {

        int chunktopush = 0;
        while (chunktopush < this.chunk_list.length && this.chunk_list[chunktopush] != 1 && this.chunk_list[chunktopush] != 2) {
            chunktopush++;
        }
        if ((chunktopush + 1 == this.chunk_list.length && this.chunk_list[chunktopush] != 1) || chunktopush >= this.chunk_list.length) {
            chunktopush = -1;
        }

        if (debug >= 3) {
            System.out.println("chunk to push is " + chunktopush+ " ["+this.chunk_list[chunktopush]+"]");
        }
        return chunktopush;
    }

    /**
     * Mark the chunk as pushed.
     * @param value chunk to mark.
     */
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

    /**
     * Unmark the chunk, it has to be pushed.
     * @param value chunk id to unmark.
     */
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
     * Get a window of latest chunks.
     * @param win_size window size of latest chunk.
     * @return an array of latest with a size less or equal than win_size.
     */
    public int[] getLatest(int win_size) {
        if (this.getOwnedChunks() < win_size) {
            win_size = this.getOwnedChunks();
        }
        int result[] = new int[win_size];
        int index = 0;
        int count = 0;
        while (win_size > 0 && count < this.chunk_list.length) {
            int id = (this.chunk_list.length - count - 1);
            if (this.chunk_list[id] != Message.IN_DOWNLOAD && this.chunk_list[id] != Message.NOT_OWNED && this.last_chunk_pulled != id) {
                result[index++] = id;
                win_size--;
            }
            count++;
        }
        if (win_size > 0) {
            int temp[] = new int[result.length - win_size];
            System.arraycopy(result, 0, temp, 0, temp.length);
            result = temp;
        }
        return result;
    }

    /**
     * Normalize the value of the chunk.
     * @param chunktime
     * @return time normalized, otherwise error code {@link Message}/
     */
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
     * {@code Message.CHUNK_IN_DOWNLOAD}, altrimenti restituisce null;
     * 
     * @param index chunk id
     * @return long time at which the node received this chunk
     */
    public long getChunk(int index) {
        return normalize(this.chunk_list[index]);
    }

    /**
     * Get the number of chunks owned among a given set of given identifier.
     * @param index set of chunks identifiers.
     * @return number of chunks owned.
     */
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
     * Check whether the node has completed to retrieve chunks or not.
     */
    public void checkCompleted() {
        if (this.getAllChunks() == this.getNumberOfChunks() && this.getCompleted() == 0) {
            this.setCompleted(CommonState.getTime());
        }
    }

    /**
     * Add a given chunk with a given method.
     * @param chunk chunk identifier to add
     * @param method operation used to retrieve the chunk.
     * @return True if the node misses the chunk, false otherwise.
     */
    public boolean addChunk(int chunk, int method) {
        if ((this.chunk_list[chunk] == Message.NOT_OWNED) || (this.chunk_list[chunk] == Message.IN_DOWNLOAD)) {
            this.chunk_list[chunk] = CommonState.getTime();
            if (method == Message.PULL_CYCLE) {
                this.chunk_list[chunk] *= -1;
            } else {
                this.checkpull();
            }
        }
        this.checkCompleted();
        return true;
    }

    /**
     * Skip the given chunk.
     * @param chunkid Chunk identifier to skip.
     * @return True if it was skipped without problems, false otherwise.
     */
    public boolean skipChunk(int chunkid) {
        if (this.chunk_list[chunkid] == Message.NOT_OWNED) {
            this.chunk_list[chunkid] = Message.SKIPPED;
            if (debug >= 4) {
                System.out.println("\tSkipping " + chunkid + " is pullable? ");
            }
            this.isPullable(chunkid);
        }
        this.checkCompleted();
        return true;
    }

    /**
     * Skip a set of chunks.
     * @param chunkids Chunks identifiers to skip.
     * @return True if they were skipped without problems, false otherwise.
     */
    public boolean skipChunks(int chunkids[]) {
        for (int k = 0; k < chunkids.length; k++) {
            if (this.chunk_list[chunkids[k]] == Message.NOT_OWNED) {
                this.chunk_list[chunkids[k]] = Message.SKIPPED;
                if (debug >= 4) {
                    System.out.println("\tSkipping " + chunkids[k] + " is pullable? " + this.isPullable(chunkids[k]));
                }
            }
            this.checkCompleted();
        }
        return true;
    }

    /**
     * Get the numebr of chunks owned by the current node.
     * @return number of chunks owned.
     */
    public int getOwnedChunks() {
        int size = 0;
        for (int i = 0; i < this.chunk_list.length; i++) {
            if (normalize(this.chunk_list[i]) > Message.OWNED) {
                size++;
            }
        }
        return size;
    }

    /**
     * Get the number of chunks owned plus those skipped.
     * @return chunk list sise.
     */
    public int getAllChunks() {
        int size = 0;
        for (int i = 0; i < this.chunk_list.length; i++) {
            if (normalize(this.chunk_list[i]) > Message.OWNED || this.chunk_list[i] == Message.SKIPPED) {
                size++;
            }
        }
        return size;
    }

    /**
     * Get the numebr of skipped chunks.
     * @return Skipped chunks.
     */
    public int getSkipped() {
        int size = 0;
        for (int i = 0; i < this.chunk_list.length; i++) {
            if (this.chunk_list[i] == Message.SKIPPED) {
                size++;
            }
        }
        return size;
    }

    /**
     * Get the least chunk missed by the current node.
     * @return Chunk idenfier of the least missed chunk.
     */
    public int getLeast() {
        int least = -1;
        int max_chunk = this.getLatest();
        for (int i = 0; i < this.chunk_list.length && i < max_chunk; i++) {
            if (this.chunk_list[i] == Message.NOT_OWNED && !this.isPullable(i)) {
                this.chunk_list[i] = Message.SKIPPED;
            } else if (this.chunk_list[i] == Message.NOT_OWNED) {
                return i;
            }
        }
        return least;
    }

    /**
     * Get a window of least missed chunks.
     * @param win_size window size of missed chunks.
     * @return an array of least missed chunks, of size less or equal than win_size.
     */
    public int[] getLeast(int win_size) {
        int result[] = new int[win_size];
        int index = 0;
        int max_chunk = this.getLatest();
        if (max_chunk == -1) {
            result[index++] = 0;
            win_size--;
        } else {
            for (int i = 0; i < this.chunk_list.length && i < max_chunk && win_size > 0; i++) {
                if (this.chunk_list[i] == Message.NOT_OWNED && !this.isPullable(i)) {
                    System.out.println("Here");
                    this.chunk_list[i] = Message.SKIPPED;
                } else if (this.chunk_list[i] == Message.NOT_OWNED) {// && i != this.last_chunk_pulled) {
                    result[index++] = i;
                    win_size--;
                }
            }
        }
        if (win_size > 0) {
            int temp[] = new int[result.length - win_size];
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
     * Printable version of node state.
     * @param node Curren node
     * @return State of the node.
     */
    public String toString(Node node) {
        String result = "Nodo " + node.getID() + ", Time " + CommonState.getTime() + ", Lista " + this.getAllChunks();
        if (this.getAllChunks() == this.getNumberOfChunks()) {
            result += " >>> ha tutti i chunks.";
        } else {
            result += ".";
        }
        return result;
    }

    /**
     * Set the current node in this protocol.
     * @param _current Set the current node.
     */
    public void setCurrent(Node _current) {
        this.current = _current;
    }

    /**
     * Reset the information we have on a given chunk for a given neighbor to  the initial state.
     * @param chunk_id chunk identifer to reset
     * @param node neighbor to reset
     * @param pid corresponding protocol identifier.
     */
    public void flushNeighbors(int chunk_id, Node node, int pid) {
        DelayedNeighbor net = (DelayedNeighbor) node.getProtocol(FastConfig.getLinkable(pid));
        net.flushNeighborhood(chunk_id);
    }

    /**
     * Reset the information we have on a set of chunks for a given neighbor to the initial state.
     * @param chunks_id chunk identifer to reset
     * @param node neighbor to reset
     * @param pid corresponding protocol identifier.
     */
    public void flushNeighbors(int chunks_id[], Node node, int pid) {
        DelayedNeighbor net = (DelayedNeighbor) node.getProtocol(FastConfig.getLinkable(pid));
        for (int k = 0; k < chunks_id.length; k++) {
            net.flushNeighborhood(chunks_id[k]);
        }
    }

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
            this.setRTTDelays(node, pid);
            net.setChunkListSize(this.number_of_chunks);
            this.setRTTDelays(node, pid);
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

//XXX FEATURE TO ADD: chose to perform push or pull before peer selection or not.
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
            this.setRTTDelays(node, pid);
            net.setChunkListSize(this.number_of_chunks);
            this.setRTTDelays(node, pid);
            if (net.getNeighbor(Network.get(this.source)) != null) {
                net.setBannedPeer(Network.get(this.getSource()));
            }
        }
        if (this.getDebug() >= 10) {
            System.out.println("\tNodo " + node.getID() + " push selection\n\t" + net);
        }
        NeighborElement candidate = net.getTargetNeighbor(chunks, Message.NOT_OWNED);
        if (this.getDebug() >= 8 && candidate != null) {
            System.out.println("\tNodo " + node.getID() + " selects candidate " + candidate + " (Source is " + this.getSource() + ") CTO = " + candidate.getContactTime() + " CTN = " + CommonState.getTime());
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
            this.setRTTDelays(node, pid);
            net.setChunkListSize(this.number_of_chunks);
            this.setRTTDelays(node, pid);
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

    /*****************************************************************************************************
     * BANDWIDTH METHODS
     *****************************************************************************************************/
    //XXX we could define a class which provides such functionalities, or set an class field which directly access to the protocol.
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

    public long getUploadMin(Node node) {
        return ((BandwidthAwareProtocol) node.getProtocol(this.getBandwidth())).getUploadMin();
    }

    public long getUploadMax(Node node) {
        return ((BandwidthAwareProtocol) node.getProtocol(this.getBandwidth())).getUploadMax();
    }

    public long getDownloadMin(Node node) {
        return ((BandwidthAwareProtocol) node.getProtocol(this.getBandwidth())).getUploadMin();
    }

    public long getDownloadMax(Node node) {
        return ((BandwidthAwareProtocol) node.getProtocol(this.getBandwidth())).getUploadMax();
    }

    public long getUpload(Node node) {
        return ((BandwidthAwareProtocol) node.getProtocol(this.getBandwidth())).getUpload();
    }

    public long getDownload(Node node) {
        return ((BandwidthAwareProtocol) node.getProtocol(this.getBandwidth())).getDownload();
    }

    public String getBwInfo(Node node) {
        return ((BandwidthAwareProtocol) node.getProtocol(this.getBandwidth())).toString();
    }
}
