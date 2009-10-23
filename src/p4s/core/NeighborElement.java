package p4s.core;

import peersim.core.Node;

/**
 * This class represents the neighbor element.<p>
 * Collects all the data of the each neighbor for performing peer selection.
 *
 * @author Alessandro Russo
 * @version 1.0
 */
public class NeighborElement {

    /**
     * Neighbor node
     */
    private Node neighbor;
    /**
     * RTT delay between current node and this neighbor
     */
    private long rtt_delay;
    /**
     * Last time in which the current node has contacted this neighbor
     */
    private long last_contact;
    /**
     * Number of times the neighbor was contacted
     */
    private int contacts;
    /**
     * Neighbor's buffer map
     */
    private int chunks[] = null;
    /**
     * Neighbor may be banned to be queried
     */
    private boolean banned = false;
    /*
     * Last chunk receive from this node
     */
    private int chunk_in;

    /*
     * Last chunk sent to this node
     */
    private int chunk_out;

    /**
     * Constuctor
     * @param neighbor Node neighbor
     * @param delay RTT delay
     * @param num_chunks number of chunks in the buffer map
     */
    public NeighborElement(Node neighbor, long delay, int num_chunks) {
        this.neighbor = neighbor;
        this.rtt_delay = delay;
        this.last_contact = -1;
        this.contacts = 1;
        this.chunks = new int[num_chunks];
    }

    /**
     * Constuctor
     * @param neighbor Node neighbor
     * @param delay RTT delay
     */
    public NeighborElement(Node neighbor, long delay) {
        this.neighbor = neighbor;
        this.rtt_delay = delay;
        this.last_contact = -1;
        this.contacts = 1;
        this.chunks = null;
    }

    /**
     * Set node as banned.
     */
    public void setBanned() {
        this.banned = true;
    }

    /**
     * Unset banning, thus becoming queriable.
     */
    public void unsetBanned() {
        this.banned = false;
    }

    /**
     * Get whether the node is queriable or not.
     * @return true if it is queriable, false otherwise.
     */
    public boolean getBanned() {
        return this.banned;
    }

    /**
     * Return the node instance of the neighbor.
     * @return Node.
     */
    public Node getNeighbor() {
        return this.neighbor;
    }

    /**
     * Return the RTT delay.
     * @return.
     */
    public long getDelay() {
        return this.rtt_delay;
    }

    /**
     * Set the RTT delay.
     * @param delay.
     */
    public void setDelay(long delay) {
        this.rtt_delay = delay;
    }

    /**
     * Get the number of times the current node contacts this neighbor.
     * @return.
     */
    public int getContacts() {
        return this.contacts;
    }

    /**
     * Add one to number of contacts.<p>
     * The node was contacted another time.
     */
    public void addContact() {
        this.contacts++;
    }

    /**
     * Printable version of the NeighborElement.
     * @return printable version of neighbor element.
     */
    public String toString() {
        return "Node " + this.neighbor.getIndex() + " delay " + this.rtt_delay;
    }

    /**
     * Set the last time in which the neighbor was contacted by the current node.
     * @param Last time the neighbor was contacted.
     */
    public void setContactTime(long value) {
        this.addContact();
        this.last_contact = value;
    }

    /**
     * Reset the contact time.
     */
    public void resetContactTime() {
        this.last_contact = -1;
    }

    /**
     * Return the last time in which the node was contacted.
     * @return last time in which the node was contacted.
     */
    public long getContactTime() {
        return this.last_contact;
    }

    /**
     * Assign the given value to the given chunk.<p>
     * This chunk can be either set as owned or not by the current node.
     * @param chunkid Chunk identifier.
     * @param value State of the chunk (owned or not).
     */
    public void setChunk(int chunkid, long value) {
        this.chunks[chunkid] = (int) value;
    }

    /**
     * Return the value of the given chunk in the corresponding neighbor.
     *@param chunkid Chunk identifier.
     * @return the state of the chunk (owned or not).
     */
    public int getChunk(int chunkid) {
        return (int) (this.chunks[chunkid]);
    }

    /**
     * Set the size of the buffer map of the corresponding neighbor.
     * @param n_chunks Number of chunks that will be stored in the buffer map.
     */
    public void setChunkListSize(int n_chunks) {
        if (this.chunks == null) {
            this.chunks = new int[n_chunks];
        } else {
            int cpchunk[] = new int[n_chunks];
            System.arraycopy(this.chunks, Math.min(this.chunks.length, n_chunks), cpchunk, 0, Math.min(this.chunks.length, n_chunks));

        }
    }

    /**
     * Filter the buffer map, selectig which chunks satisfy the given criteria among the whole set given.
     * @param achunks Chunks to analyze.
     * @param value Criteria requested.
     * @return number of chunks that satisfy the requirements.
     */
    public int getChunks(int achunks[], long value) {
        if (this.banned) {
            return 0;
        }
        int sel = 0;
        int val = (int) value;
        for (int k = 0; k < achunks.length; k++) {
            int chunkid = achunks[k];
            if (this.getChunk(chunkid) == val || this.getChunk(chunkid) == 0) {
                sel++;
            }
        }
        return sel;
    }

    /**
     * Set a given set of chunks with the given value.
     * @param achunks Chunks which state will be modifed.
     * @param value flag to assign.
     */
    public void setChunks(int achunks[], long value) {
        for (int k = 0; k < achunks.length; k++) {
            this.setChunk(achunks[k], value);
        }
    }

    /**
     * Set the last chunk retrieved from this neighbor.
     * @param _chunkid last chunk identifier received.
     */
    public void setChunkIn(int _chunkid) {
        this.chunk_in = _chunkid;
    }

    /**
     * Give the last chunk retrieved from this neighbor.
     * @return the last chunk retrieved from this neighbor.
     */
    public int getChunkIn() {
        return this.chunk_in;
    }

    /**
     * Set the last chunk transmitted to this neighbor.
     * @param _chunkid chunk uploaded to this neighbor.
     */
    public void setChunkOut(int _chunkid) {
        this.chunk_in = _chunkid;
    }

    /**
     * Get the latest chunk pushed to this neighbor.
     * @return latest chunk pushed to this neighbor.
     */
    public int getChunkOut() {
        return this.chunk_out;
    }
}
