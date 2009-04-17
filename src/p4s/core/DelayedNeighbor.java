package p4s.core;

import p4s.util.RandomRLC;
import peersim.core.*;
import peersim.config.Configuration;

/**
 * This class is similar to IdleProtocol, but it implements some
 * randomized GET methods and allows to remove some neighbors,
 * providing a dynamic neighborhood
 *
 * @author ax
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
     * Initial capacity. Defaults to {@value #DEFAULT_INITIAL_CAPACITY}.
     * @config
     */
    private static final String PAR_INITCAP = "capacity";
    private static final String PAR_MINDELAY = "mindelay";
    private static final String PAR_MAXDELAY = "maxdelay";
    private static final String PAR_MUDELAY = "mudelay";
    private static final String PAR_DEVDELAY = "devdelay";
    private static final String PAR_DELAY = "delay";
    private static final String PAR_SELECT = "select";
    private static final String PAR_DEBUG = "debug";
    /**
     * Many kinds of delay distributions
     * 0 Uniform between max and min
     * 1 Gaussian with mean
     * 2 Exponential Truncated with mean
     */
// --------------------------------------------------------------------------
// Fields
// --------------------------------------------------------------------------
    /** Neighbors */
    protected NeighborElement[] neighbors;
    protected Node current;
    /** Actual number of neighbors in the array */
    protected int len;
    private final int delaydist;
    private final int select;
    private final double mu;
    private final double dev;
    private final long min;
    private final long range;
    private final long max;
    private final int debug;
    protected static long[][] delays;
    private double prob[];

// --------------------------------------------------------------------------
// Initialization
// --------------------------------------------------------------------------
    public DelayedNeighbor(String prefix) {
        neighbors = new NeighborElement[Configuration.getInt(prefix + "." + PAR_INITCAP, DEFAULT_INITIAL_CAPACITY)];
        select = Configuration.getInt(prefix + "." + PAR_SELECT, 0);
        debug = Configuration.getInt(prefix + "." + PAR_DEBUG, 0);
        len = 0;
        min = Configuration.getLong(prefix + "." + PAR_MINDELAY, 0);
        max = Configuration.getLong(prefix + "." + PAR_MAXDELAY, 0);
        range = max - min;
        delaydist = Configuration.getInt(prefix + "." + PAR_DELAY, 0);
        mu = Configuration.getDouble(prefix + "." + PAR_MUDELAY, 0) - min;
        dev = Configuration.getDouble(prefix + "." + PAR_DEVDELAY, 0);
        current = null;
        prob = null;
        delays = null;
        System.err.println("Init DelayedNeighbor: Len " + len + ", Select " + select + ", DelayDist " + delaydist + ", MinDelay " + min + ", MaxDelay " + max + ", Mean " + mu + ", Dev " + dev);
    }

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
                        val = (val < max ? val : max);
                        delays[i][j] = delays[j][i] = val;
                    } else if (delaydist == 2) {//trunc exponential
                        long val = min + rlc.trunc_exp(mu, range);
                        val = val < min ? min : val;
                        val = val < max ? val : max;
                        delays[i][j] = delays[j][i] = val;
                    }
                }
            }
        }
    }

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
                        val = val < max ? val : max;
                        tmpDelay[i][j] = tmpDelay[j][i] = val;
                    } else if (delaydist == 2) {//trunc exponential
                        long val = min + rlc.trunc_exp(mu, range, rlc.nextLong());
                        val = val < min ? min : val;
                        val = val < max ? val : max;
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
//        if (debug >= 8) {
//            for (int k = 0; k < delays.length; k++) {
//                System.out.print(k + "]]] ");
//                for (int v = 0; v < delays[k].length; v++) {
//                    System.out.print(delays[k][v] + ";");
//                }
//                System.out.println("[[[ " + delays[k].length);
//            }
//        }
    }

//--------------------------------------------------------------------------
    public Object clone() {
        DelayedNeighbor rn = null;
        try {
            rn = (DelayedNeighbor) super.clone();
        } catch (CloneNotSupportedException e) {
        } // never happens
        rn.neighbors = new NeighborElement[neighbors.length];
        System.arraycopy(neighbors, 0, rn.neighbors, 0, len);
        rn.len = len;
        rn.current = null;
        rn.prob = null;
        return rn;
    }
// --------------------------------------------------------------------------
// Methods
// --------------------------------------------------------------------------

    public boolean contains(Node n) {
        for (int i = 0; i < len; i++) {
            if (neighbors[i] == n) {
                return true;
            }
        }
        return false;
    }

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

    public Node getCurrent() {
        return this.current;
    }

// --------------------------------------------------------------------------
    /** Adds given node if it is not already in the network.*/
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

// --------------------------------------------------------------------------
    /**Get the i-th neighbor*/
    public Node getNeighbor(int i) {
        return neighbors[i].getNeighbor();
    }

    public NeighborElement getNeighbor(Node nd) {
        for (int i = 0; i < neighbors.length; i++) {
            if (neighbors[i].getNeighbor() == nd) {
                return neighbors[i];
            }
        }
        return null;
    }

    public NeighborElement getTargetNeighbor() {
        System.out.println("Select "+select);
        if (select == 1) {
            return getDelayNeighbor();
        } else {
            return getRNDNeighbor();
        }
    }

    public NeighborElement getDelayNeighbor() {
        if (prob == null || prob.length < neighbors.length) {
            double tot = 0;
            this.delaySort(neighbors);
            double viz[] = new double[neighbors.length];
            double sv = 0;
            prob = new double[neighbors.length];
            for (int i = 0; i < viz.length; i++) {
                viz[i] = 1.0 / neighbors[i].getDelay();
                if (debug >= 8) {
                    System.out.println(" Node " + current.getID() + " 1/RTT(" + neighbors[i].getNeighbor().getID() + ") is " + viz[i]);
                }
                sv += viz[i];
            }
            for (int i = 0; i < prob.length; i++) {
                prob[i] = viz[i] / sv;
                if (debug >= 8) {
                    System.out.println(" Node " + current.getID() + " Prob[" + neighbors[i].getNeighbor().getID() + "] = " + prob[i]);
                }
                tot += prob[i];
            }
            if (debug >= 8) {
                System.out.println("Total prob. is " + tot);
            }
        }
        NeighborElement candidate = null;
        RandomRLC rlc = (RandomRLC) CommonState.r;
        double value = rlc.uniform_0_1(rlc.nextLong());
        if (debug >= 8) {
            System.out.println("Extract " + value);
        }
        int id = 0;
        while (value > 0 && candidate == null && id < neighbors.length) {
            if (debug >= 8) {
                System.out.println("\t(" + id + ") Value " + value + ", Prob " + prob[id] + " (" + neighbors[id] + ")");
            }
            value -= prob[id];
            if (value <= 0) {
                candidate = neighbors[id];
            } else {
                id++;
            }
        }
        if (candidate == null) {
            if (id >= neighbors.length) {
                id = neighbors.length - 1;
            }
            candidate = neighbors[id];
        }
        return candidate;
    }

///**Get a randomly selected neighbor*/
    public NeighborElement getRNDNeighbor() {
//        int swap[] = new int[len];
//        for (int i = 0; i < len; i++)
//            swap[i] = i;
//        int temp = 0;
//        for (int i = 0; i < len; i++) {
//            int out = CommonState.r.nextInt(len - i);
//            temp = swap[i];
//            swap[i] = swap[out];
//            swap[out] = temp;
//        }
        int out = CommonState.r.nextInt(len);
        return this.neighbors[out];
    }

    /**Performs a permutation of the neighbors*/
    public void permutation() {
        NeighborElement swap = null;
        for (int i = 0; i < len; i++) {
            int out = CommonState.r.nextInt(len - i);
            swap = this.neighbors[out];
            this.neighbors[out] = this.neighbors[i];
            this.neighbors[i] = swap;
        }
    }

// --------------------------------------------------------------------------
    public int degree() {
        return len;
    }

// --------------------------------------------------------------------------
    public void pack() {
        if (len == neighbors.length) {
            return;
        }
        NeighborElement[] temp = new NeighborElement[len];
        System.arraycopy(neighbors, 0, temp, 0, len);
        neighbors = temp;
    }

// --------------------------------------------------------------------------
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
     * @param Neighbour The neighbor to remove from current node's neighborhood
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
     * Internal method that makes recursive calls.
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
//            System.out.println(leftPos + " << "+ a[leftPos].getDelay());
//            System.out.println(rightPos + " >> "+ a[rightPos].getDelay());
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

