package p4s.core;

import p4s.util.RandomRLC;
import p4s.util.Message;
import peersim.core.*;
import peersim.config.Configuration;

/**
 * This class store the links between nodes and allow also several peer selection strategies.<p>
 * Nodes can be added and removed, and this could be used as a data structure for a neighborhood protocol.
 *
 * @author Alessandro Russo
 * @version 1.0
 */
public class DelayedNeighbor implements Protocol, Linkable {

// --------------------------------------------------------------------------
// Parameters
// --------------------------------------------------------------------------
    /**
     * Default init capacity
     */
    private static final int DEFAULT_INITIAL_CAPACITY = 10;
    /**
     * Pure random peer selection.
     */
    private static final int RND_DUMMY = 0;
    /**
     * Random peer selection with few knowledge.
     */
    private static final int RND_SMART = 1;
    /**
     * Delay oriented peer selection
     */
    private static final int DELAY_ORIENTED = 2;
    /**
     * Initial capacity. Defaults to {@value #DEFAULT_INITIAL_CAPACITY}.
     * @config
     */
    private static final String PAR_INITCAP = "capacity";
    private static final String PAR_NEWCHUNK = "new_chunk";
    private static final String PAR_MINDELAY = "mindelay";
    private static final String PAR_MAXDELAY = "maxdelay";
    private static final String PAR_MUDELAY = "mudelay";
    private static final String PAR_DEVDELAY = "devdelay";
    private static final String PAR_DELAY = "delay";
    private static final String PAR_SELECT = "select";
    private static final String PAR_DEBUG = "debug";
// --------------------------------------------------------------------------
// Fields
// --------------------------------------------------------------------------
    /** Neighbors list*/
    protected NeighborElement[] neighbors;
    /** Current node    */
    protected Node current;
    /** Actual number of neighbors in the array */
    protected int len;
    /**
     * RTT delay distribution.
     */
    private final int delaydist;
    /**
     * Peer selection algorithm
     */
    private final int select;
    /**
     * Mean RTT time, depends on RTT distribution
     */
    private final double mu;
    /**
     * Deviation of the RTT time, depends on RTT distribution
     */
    private final double dev;
    /**
     * Minimum RTT value
     */
    private final long min;
    /**
     * Range value between max and min.
     */
    private final long range;
    /**
     * Time to produce a new chunk.
     */
    private final long new_chunk;
    /**
     * Level of verbosity.
     */
    private final int debug;
    /**
     * matrix of RTT delays between nodes. It could be modified to dynamically change during simulation time.
     */
    protected static long[][] delays;
    /**
     * Probability assigned to neighbors.
     */
    private double prob[];

    /**
     * Constructor
     * @param prefix name assigned in the configuration file.
     */
    public DelayedNeighbor(String prefix) {
        neighbors = new NeighborElement[Configuration.getInt(prefix + "." + PAR_INITCAP, DEFAULT_INITIAL_CAPACITY)];
        select = Configuration.getInt(prefix + "." + PAR_SELECT, 0);
        debug = Configuration.getInt(prefix + "." + PAR_DEBUG, 0);
        len = 0;
        min = Configuration.getLong(prefix + "." + PAR_MINDELAY, 0);
        long max = Configuration.getLong(prefix + "." + PAR_MAXDELAY, 0);
        range = max - min + 1;
        delaydist = Configuration.getInt(prefix + "." + PAR_DELAY, 0);
        mu = Configuration.getDouble(prefix + "." + PAR_MUDELAY, 0) - min;
        dev = Configuration.getDouble(prefix + "." + PAR_DEVDELAY, 0);
        current = null;
        prob = null;
        delays = null;
        new_chunk = Configuration.getLong(prefix + "." + PAR_NEWCHUNK, 0);
        System.err.println("Init DelayedNeighbor: Len " + len + ", Select " + select + ", DelayDist " + delaydist + ", MinDelay " + min + ", MaxDelay " + max + ", Mean " + mu + ", Dev " + dev + ", new chunk " + new_chunk);
    }

    /**
     * Populates the array of RTT delay in according to RTT distribution and the given value.
     */
    public void populate() {
        this.delays = new long[Network.size()][Network.size()];
        for (int i = 0; i < delays.length; i++) {
            for (int j = 0; j < delays[i].length; j++) {
                delays[i][j] = -1;
            }
        }
        RandomRLC rlc = (RandomRLC) CommonState.r;
        for (int i = 0; i < delays.length; i++) {
            for (int j = 0; j < delays[i].length; j++) {
                if (delays[i][j] < min) {
                    if (delaydist == 0) {//uniform distribution
                        delays[i][j] = delays[j][i] = min + rlc.nextLong(range);
                    } else if (delaydist == 1) {//gaussian
                        long val = min + (long) (rlc.NextGaussian(mu, dev));
                        val = (val < min ? min : val);
                        val = (val < (min + range) ? val : (min + range));
                        delays[i][j] = delays[j][i] = val;
                    } else if (delaydist == 2) {//trunc exponential
                        long val = min + rlc.trunc_exp(mu, range);
                        val = val < min ? min : val;
                        val = val < (min + range) ? val : (min + range);
                        delays[i][j] = delays[j][i] = val;
                    }
                }
            }
        }
    }

    /**
     * Populates the array of RTT delay in according to RTT distribution and the value given, after peers arrival/departure.
     */
    public void repopulate() {
        long[][] tmpDelay = new long[Network.size()][Network.size()];
        for (int i = 0; i < tmpDelay.length; i++) {
            for (int j = 0; j < tmpDelay[i].length; j++) {
                tmpDelay[i][j] = -1;
            }
        }
        for (int i = 0; i < delays.length; i++) {
            for (int j = 0; j < delays[i].length; j++) {
                tmpDelay[i][j] = delays[i][j];
            }
        }
        RandomRLC rlc = (RandomRLC) CommonState.r;
        for (int i = 0; i < tmpDelay.length; i++) {
            for (int j = 0; j < tmpDelay[i].length; j++) {
                if (tmpDelay[i][j] < min) {
                    if (delaydist == 0) {//uniform distribution
                        tmpDelay[i][j] = tmpDelay[j][i] = min + rlc.nextLong(range);
                    } else if (delaydist == 1) {//gaussian
                        long val = (long) (rlc.NextGaussian(mu, dev));
                        val = val < min ? min : val;
                        val = val < (min + range) ? val : (min + range);
                        tmpDelay[i][j] = tmpDelay[j][i] = val;
                    } else if (delaydist == 2) {//trunc exponential
                        long val = min + rlc.trunc_exp(mu, range, rlc.nextLong());
                        val = val < min ? min : val;
                        val = val < (min + range) ? val : (min + range);
                        tmpDelay[i][j] = tmpDelay[j][i] = val;
                        if (debug >= 8) {
                            System.out.println("[" + i + "," + j + "]=" + tmpDelay[i][j] + " (" + val + ")");
                        }
                    }
                }
            }
        }
        delays = tmpDelay;
        tmpDelay = null;
    }

//--------------------------------------------------------------------------
    public Object clone() {
        DelayedNeighbor rn = null;
        try {
            rn = (DelayedNeighbor) super.clone();
        } catch (CloneNotSupportedException e) {
        } // never 
        rn.neighbors = new NeighborElement[neighbors.length];
        System.arraycopy(neighbors, 0, rn.neighbors, 0, len);
        rn.len = len;
        rn.current = null;
        rn.prob = null;
        return rn;
    }

    /**
     * Check whether the current node has this node as neighbor or not.
     * @param n neighbor to check
     * @return true if it has this node as neighbor, false otherwise.
     */
    public boolean contains(Node n) {
        for (int i = 0; i < len; i++) {
            if (neighbors[i] == n) {
                return true;
            }
        }
        return false;
    }

    /**
     * Set the current node instance in this class.
     * @param current node on which this instance belongs to.
     */
    public void setCurrent(Node current) {
        this.current = current;
        this.pack();
        for (int i = 0; i < neighbors.length; i++) {
            NeighborElement ne = neighbors[i];
            ne.setDelay(delays[current.getIndex()][ne.getNeighbor().getIndex()]);
        }
        this.delaySort(neighbors);
        if (debug >= 8) {
            System.out.print("<><>Node " + current.getIndex() + " >> \n\t");
            for (int i = 0; i < neighbors.length; i++) {
                System.out.print(neighbors[i] + "; ");
            }
            System.out.println();
        }
    }

    /**
     * Get the current node.
     * @return Current node.
     */
    public Node getCurrent() {
        return this.current;
    }

    /**
     * Set the buffermap for each chunk
     * @param chunkn number of chunks to increase the buffer map size.
     */
    public void setChunkListSize(int chunkn) {
        for (int i = 0; i < neighbors.length; i++) {
            if (neighbors[i] != null) {
                neighbors[i].setChunkListSize(chunkn);
            }
        }
    }

    /**
     * Banning a given peer.
     * @param target peer to ban.
     */
    public void setBannedPeer(Node target) {
        NeighborElement ne = this.getNeighbor(target);
        if (debug > 8) {
            System.out.println("Banning  " + target.getIndex() + " > " + ne);
        }
        if (ne != null) {
            ne.setBanned();
        }
    }

// --------------------------------------------------------------------------
    /**
     * Adds given node if it is not already in the network.
     * @param n Node to add in the neighbor.
     * @return True if the node is added, false otherwise.
     */
    public boolean addNeighbor(Node n) {
        for (int i = 0; i < len; i++) {
            if (neighbors[i].getNeighbor() == n) {
                return false;
            }
        }
        if (delays != null && delays.length < Network.size()) {
            this.repopulate();
        }
        if (len == neighbors.length) {
            int newlen = (int) Math.round(1.3 * neighbors.length);
            NeighborElement[] temp = new NeighborElement[newlen];
            System.arraycopy(neighbors, 0, temp, 0, neighbors.length);
            neighbors = temp;
        }
        long ddelay = -1;
        if (current != null) {
            ddelay = delays[current.getIndex()][n.getIndex()];
        }
        NeighborElement ne = new NeighborElement(n, ddelay);
        neighbors[len] = ne;
        len++;
        return true;
    }

    /**
     * Get the maximum RTT delay among node and it's all neighbors.
     * @return Maximum RTT delay among node and it's all neighbors.
     */
    public long getMaxRTT() {
        long max = 0;
        for (int i = 0; i < neighbors.length; i++) {
            if (neighbors[i] != null && delays[this.current.getIndex()][neighbors[i].getNeighbor().getIndex()] > max) {
                max = delays[this.current.getIndex()][neighbors[i].getNeighbor().getIndex()];
            }
        }
        return max;
    }

    /**
     * Get the minimum RTT delay among node and it's all neighbors.
     * @return minimum RTT delay among node and it's all neighbors.
     */
    public long getMinRTT() {
        long _min = -1;
        for (int i = 0; i < neighbors.length; i++) {
            if (neighbors[i] != null && (delays[this.current.getIndex()][neighbors[i].getNeighbor().getIndex()] < _min || _min == -1)) {
                _min = delays[this.current.getIndex()][neighbors[i].getNeighbor().getIndex()];
            }
        }
        return _min;
    }

// --------------------------------------------------------------------------
    /**
     * Get the i-th neighbor in the neighbors list.
     * @param i I-th position of the neighbor.
     * @return the i-th node.
     */
    public Node getNeighbor(int i) {
        return neighbors[i].getNeighbor();
    }

    /**
     * Get the nd-th neighbor in the neighbors list as a Node.
     * @param nd I-th position of the neighbor.
     * @return the neighbor element associated to the node.
     */
    public NeighborElement getNeighbor(Node nd) {
        for (int i = 0; i < neighbors.length; i++) {
            if (neighbors[i].getNeighbor() == nd) {
                return neighbors[i];
            }
        }
        return null;
    }

    /**
     * Get the target neighbor in according to the peer selection algorithm choosen.
     * @return target neighbor to contact.
     */
    public NeighborElement getTargetNeighbor() {
        if (select == RND_DUMMY) {
            return getRNDNeighbor();
        } else if (select == RND_SMART) {
            return getRNDNeighbor();
        } else if (select == DELAY_ORIENTED) {
            return getDelayNeighbor();
        }
        return getRNDNeighbor();
    }

    /**
     * Return a target neighbor among a filterd set using the peer selection choosen.
     * @param chunks Chunks used for filter.
     * @param value Criteria for the chunks given.
     * @return Neighbor to contact.
     */
    public NeighborElement getTargetNeighbor(int chunks[], long value) {
//        System.out.println("Select "+select);
        if (select == RND_SMART) {
            return getRNDSmartNeighbor(chunks, value);
        } else if (select == DELAY_ORIENTED) {
            return getDelayNeighbor(chunks, value);
        }
        return getRNDNeighbor();
    }

    /**
     * Compute the probability for peer selection among the given array, updatin the local probability matrix.
     * @param neighborz Array on which compute the probability.
     */
    public void computeProbabilities(NeighborElement[] neighborz) {
        double tot = 0;
        this.delaySort(neighborz);
        double viz[] = new double[neighborz.length];
        double sv = 0;
        prob = new double[neighborz.length];
        for (int i = 0; i < viz.length; i++) {
            viz[i] = 1.0 / neighborz[i].getDelay();
            if (debug >= 8) {
                System.out.println(" Node " + neighborz[i].getNeighbor().getID() + " 1/RTT(" + neighborz[i].getNeighbor().getID() + ") is " + viz[i]);
            }
            sv += viz[i];
        }
        if (debug >= 8) {
            System.out.println(">>> pobabilities <<<");
        }
        for (int i = 0; i < prob.length; i++) {
            prob[i] = viz[i] / sv;
            if (debug >= 8) {
                System.out.println(" Node " + neighborz[i].getNeighbor().getID() + " Prob[" + neighborz[i].getNeighbor().getID() + "] = " + prob[i]);
            }
            tot += prob[i];
        }
        if (debug >= 8) {
            System.out.println("Total prob. is " + tot);
        }
    }

    /**
     * Peer selection algorithm: Delay aware criteria. This selection tecnique picks up closest nodes with higher probability which follow the RTT delay between nodes.
     * It does not use any kind of filter among neighbors in the set of candidates nodes.
     * @return Target neighbor to contact.
     */
    public NeighborElement getDelayNeighbor() {
        if (prob == null || prob.length < neighbors.length) {// fulfill array of probability
            this.computeProbabilities(neighbors);
        }
        NeighborElement candidate = null;
        RandomRLC rlc = (RandomRLC) CommonState.r;
        double value = rlc.nextDouble();
        if (debug >= 8) {
            System.out.println("I Extract this value " + value);
        }
        int id = 0;
        while (value > 0 && candidate == null && id < neighbors.length) {
            if (debug >= 8) {
                System.out.println("\t(" + id + ") Value " + value + ", Prob " + prob[id] + " (" + neighbors[id] + ")\n");
            }
            value -= prob[id];
            if (value <= 0) {
                candidate = neighbors[id];
            }
            id++;
        }
        if (candidate == null && id >= neighbors.length) {
            if (debug >= 10) {
                System.out.println("Out of Candidate ID range: " + id + " -- " + candidate);
            }
            id = 0;
            candidate = neighbors[id];
        }
        if (debug >= 10) {
            System.out.println("Return Candidate ID " + id + " -- " + candidate);
        }
        return candidate;
    }

    /**
     * Filter the neighbors over the chunks required, and extract a neighbor with a delay distribution
     * @param chunks array of chunks desired
     * @param value criteria
     * @return NeighborElement which satisfy the requirement
     */
    public NeighborElement getDelayNeighbor(int chunks[], long value) {
        int val = (int) value;
        NeighborElement[] copy_neighborz = this.getFilteredNeighborhood(chunks, val, 2.1);
        if (copy_neighborz == null) {
            return null;//no neighbors with these properties
        }        //compute probabilities; prob contains new values;
        computeProbabilities(copy_neighborz);
        NeighborElement candidate = null;//set candidate to null
        RandomRLC rlc = (RandomRLC) CommonState.r;//instance of random generator

        double randomv = rlc.nextDouble();//extract first value
        if (debug >= 8) {
            System.out.println("Extract this value " + randomv);
        }
        int id = 0;//reflectes the positionof the pointer in the array of probabilities
        while (randomv > 0 && candidate == null && id < copy_neighborz.length) {
            if (debug >= 8) {
                System.out.println("\t(" + id + ") Val. " + randomv + ", Prob. " + prob[id] + " (" + copy_neighborz[id] + ") " + copy_neighborz[id].getChunks(chunks, val) + " (" + copy_neighborz[id].getBanned() + ") .");
            }
            randomv -= prob[id];//loop until reach the probability extracted
            if (randomv <= 0) {//when i find the node with the given probability
                candidate = copy_neighborz[id];//retrieve the peer
            }
            id++;//add one to go ahead in the probability matrix                            
        }
        if (debug >= 10) {
            System.out.println("Return Candidate ID " + id + " -- " + candidate);
        }
        return candidate;
    }

    /**
     * Reset the information we have on a given chunk in the neighbors to initial state, we don't know nothing, therefore all nodes become eligible to be pulled.
     * @param chunkid chunk for which the information has to be cleared.
     */
    public void flushNeighborhood(int chunkid) {
        for (int i = 0; i < neighbors.length; i++) {
            if (neighbors[i] != null) {
                neighbors[i].setChunk(chunkid, 0);
            }
        }
    }

    /**
     * Get a randomly selected neighbor without any kind of filtering.
     * @param chunks Chunks used for selection.
     * @param val Filter used among given chunks.
     * @return A set of neighbors to be contacted.
     */
    public NeighborElement[] getFilteredNeighborhood(int chunks[], int val) {
        NeighborElement copy_neighbors[] = new NeighborElement[neighbors.length];
        int index = 0;
        //filtering the array of neighbors with my criteria
        for (int i = 0; i < copy_neighbors.length; i++) {
            copy_neighbors[i] = null;
            if (neighbors[i].getChunks(chunks, val) > 0 && neighbors[i].getContactTime() != CommonState.getTime() && !neighbors[i].getBanned()) {
                copy_neighbors[index++] = neighbors[i];
            }
        }
        if (index == 0)//no nodes satisfy the criteria
        {
            return null;
        }
        //resize the array
        if (index < copy_neighbors.length) {
            NeighborElement copy_neighborz[] = new NeighborElement[index];
            for (int i = 0; i < copy_neighborz.length; i++) {
                copy_neighborz[i] = copy_neighbors[i];
            }
            return copy_neighborz;
        } else {
            return copy_neighbors;
        }
    }

    /**
     * Filters the neighborhood in according to the given criteria.
     * @param chunks Chunks list on which perform the filter.
     * @param val Value for the chunk.
     * @param ts_slot Threshold used to avoid to push consecutive chunks to the same peer.
     * @return a set of target neighbors.
     */
    public NeighborElement[] getFilteredNeighborhood(int chunks[], int val, double ts_slot) {
        NeighborElement copy_neighbors[] = new NeighborElement[neighbors.length];
        int index = 0;
        long thresh = Math.round(ts_slot * new_chunk);
        //filtering the array of neighbors with my criteria
        for (int i = 0; i < copy_neighbors.length; i++) {
            copy_neighbors[i] = null;
//            System.out.println("Comm "+CommonState.getTime()+ " contact "+neighbors[i].getContactTime()+" thre "+thresh);
            if (neighbors[i].getChunks(chunks, val) > 0 && neighbors[i].getContactTime() != CommonState.getTime() && !neighbors[i].getBanned()) {
                //if the node has to pull OR the current time is zero (first time) OR the target peer was never contacted OR it was contacted recently OR it is the last node available
                if ((val == Message.OWNED) || CommonState.getTime() == 0 || neighbors[i].getContactTime() < 0 || ((CommonState.getTime() - neighbors[i].getContactTime()) > thresh)) {
                    copy_neighbors[index++] = neighbors[i];
                }
            }
        }
        if (index == 0)//no nodes satisfy the criteria
        {
            return null;
        }
        //resize the array
        if (index < copy_neighbors.length) {
            NeighborElement copy_neighborz[] = new NeighborElement[index];
            for (int i = 0; i < copy_neighborz.length; i++) {
                copy_neighborz[i] = copy_neighbors[i];
            }
            return copy_neighborz;
        } else {
            return copy_neighbors;
        }
    }

    /**
     * Pure random peer selection.
     * @return Target neighbor to contact.
     */
    public NeighborElement getRNDNeighbor() {
        RandomRLC rlc = (RandomRLC) CommonState.r;
        return this.neighbors[rlc.nextInt(this.neighbors.length)];
    }

    /**
     * Return a neighbors selected randomly among a set of neighbors which satisfy a given criteria
     * @param chunks Chunks to filter.
     * @param value Value for the chunks.
     * @return Target neighbor.
     */
    public NeighborElement getRNDSmartNeighbor(int chunks[], long value) {
        int val = (int) value;
        RandomRLC rlc = (RandomRLC) CommonState.r;
        NeighborElement[] copy_neighborz = this.getFilteredNeighborhood(chunks, val);
        if (copy_neighborz == null) {
            return null;
        }
        return copy_neighborz[rlc.nextInt(copy_neighborz.length)];
    }

    /**
     * Performs a permutation of the neighborhood.
     */
    public void permutation() {
        NeighborElement swap = null;
        for (int i = 0; i < len; i++) {
            int out = CommonState.r.nextInt(len - i);
            swap = this.neighbors[out];
            this.neighbors[out] = this.neighbors[i];
            this.neighbors[i] = swap;
        }
    }

    /**
     * Neighborhood size.
     * @return number of neighbors.
     */
    public int degree() {
        return len;
    }

    /**
     * Reduce the memory used to store the neighbors.
     *
     */
    public void pack() {
        if (len == neighbors.length) {
            return;
        }
        NeighborElement[] temp = new NeighborElement[len];
        System.arraycopy(neighbors, 0, temp, 0, len);
        neighbors = temp;
    }

    /**
     * Printable version of the neighborhood
     * @return String representing the neighborhood.
     */
    public String toString() {
        if (neighbors == null) {
            return "DEAD!";
        }
        StringBuffer buffer = new StringBuffer();
        buffer.append("len=" + len + " maxlen=" + neighbors.length + " NODE " + current.getIndex() + " [");
        for (int i = 0; i < len; ++i) {
            buffer.append("(" + i + ") " + neighbors[i] + "; ");
        }
        return buffer.append("]").toString();
    }

// --------------------------------------------------------------------------
    public void onKill() {
        neighbors = null;
        len = 0;
    }

    /**
     * Removes a given node in the neighborhood     
     * @param neighbour The neighbor to remove from current node's neighborhood
     * @return Node The node removed
     */
    public Node remNeighbor(Node neighbour) {
        if (!this.contains(neighbour)) {
            return null;
        }
        for (int i = 0; i < this.neighbors.length; i++) {
            if (this.neighbors[i] == neighbour) {
                this.neighbors[i] = null;
                if (this.neighbors[0] != null) {
                    this.neighbors[i] = this.neighbors[0];
                }
                NeighborElement[] temp = new NeighborElement[neighbors.length];
                System.arraycopy(neighbors, 1, temp, 0, neighbors.length - 1);
                neighbors = temp;
                len--;
                return neighbour;
            }
        }
        return null;
    }

    public void delaySort(NeighborElement[] a) {
//    System.err.println(a.length);
        NeighborElement[] tmpArray = new NeighborElement[a.length];
        mergeSort(a, tmpArray, 0, a.length - 1);
    }

    /**
     * Sort neighbors on delay.
     * @param a an array of Comparable items.
     * @param tmpArray an array to place the merged result.
     * @param left the left-most index of the subarray.
     * @param right the right-most index of the subarray.
     */
    private void mergeSort(NeighborElement[] a, NeighborElement[] tmpArray, int left, int right) {
        if (left < right) {
            int center = (left + right) / 2;
            mergeSort(a, tmpArray, left, center);
            mergeSort(a, tmpArray, center + 1, right);
            merge(a, tmpArray, left, center + 1, right);
        }
    }

    /**
     * Internal method that merges two sorted halves of a subarray.
     * @param a an array of Comparable items.
     * @param tmpArray an array to place the merged result.
     * @param leftPos the left-most index of the subarray.
     * @param rightPos the index of the start of the second half.
     * @param rightEnd the right-most index of the subarray.
     */
    private void merge(NeighborElement[] a, NeighborElement[] tmpArray, int leftPos, int rightPos, int rightEnd) {
        int leftEnd = rightPos - 1;
        int tmpPos = leftPos;
        int numElements = rightEnd - leftPos + 1;
        // Main loop
        while (leftPos <= leftEnd && rightPos <= rightEnd) {
            if (a[leftPos].getDelay() <= a[rightPos].getDelay()) {
                tmpArray[tmpPos++] = a[leftPos++];
            } else {
                tmpArray[tmpPos++] = a[rightPos++];
            }
        }
        while (leftPos <= leftEnd) // Copy rest of first half
        {
            tmpArray[tmpPos++] = a[leftPos++];
        }
        while (rightPos <= rightEnd) // Copy rest of right half
        {
            tmpArray[tmpPos++] = a[rightPos++];        // Copy tmpArray back
        }
        for (int i = 0; i < numElements; i++, rightEnd--) {
            a[rightEnd] = tmpArray[rightEnd];
        }
    }
}

