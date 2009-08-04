/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package p4s.core;

import peersim.core.Node;

/**
 *
 * @author ax
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
    private byte chunks[] = null;
    /**
     * Neighbor may be banned to be queried
     */
    private boolean banned = false;

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
        this.chunks = new byte[num_chunks];
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
     * Set node as banned
     */
    public void setBanned() {
        this.banned = true;
    }

    /**
     * unset banning. Node becomes queriable
     */
    public void unsetBanned() {
        this.banned = false;
    }

    /**
     * Get node state
     * @return true if it is queriable, false otherwise.
     */
    public boolean getBanned() {
        return this.banned;
    }

    /**
     * Return the node instance of the neighbor
     * @return Node
     */
    public Node getNeighbor() {
        return this.neighbor;
    }

    /**
     * Return the RTT delay
     * @return
     */
    public long getDelay() {
        return this.rtt_delay;
    }

    /**
     * Set the RTT delay
     * @param delay
     */
    public void setDelay(long delay) {
        this.rtt_delay = delay;
    }

    /**
     * Get the number of times the current node contacts this neighbor
     * @return
     */
    public int getContacts() {
        return this.contacts;
    }

    /**
     * add one to number of contacts
     */
    public void addContact() {
        this.contacts++;
    }

    public String toString() {
        return "Node " + this.neighbor.getIndex() + " delay " + this.rtt_delay;
    }

    /**
     * set the contact time
     * @param value
     */
    public void setContactTime(long value) {
        this.addContact();
        this.last_contact = value;
    }

    /**
     * return the last contact time
     * @return
     */
    public long getContactTime() {
        return this.last_contact;
    }

    /**
     * Assign the given value to the given chunk
     * @param chunkid
     * @param value
     */
    public void setChunk(int chunkid, long value) {
        byte val = (byte) value;
        this.chunks[chunkid] = val;
    }

    /**
     * Return the value of the given chunk in the buffermap
     * @param chunkid
     * @return
     */
    public int getChunk(int chunkid) {
        return (int) (this.chunks[chunkid]);
    }

    /**
     * Set the size of the buffermap
     * @param n_chunks
     */
    public void setChunks(int n_chunks) {
        if (this.chunks == null) {
            this.chunks = new byte[n_chunks];
        } else {
            byte cpchunk[] = new byte[n_chunks];
            System.arraycopy(this.chunks, Math.min(this.chunks.length, n_chunks), cpchunk, 0, Math.min(this.chunks.length, n_chunks));

        }
    }

    /**
     * Return a integer value which reflects the number of chunks that satisfy the given criteria among the whole set given
     * @param achunks
     * @param value
     * @return
     */
    public int getChunks(int achunks[], long value) {
        if (this.banned) {
            return 0;
        }
        int sel = 0;
        byte val = (byte) value;
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
     * @param achunks
     * @param value
     */
    public void setChunks(int achunks[], long value) {
        byte val = (byte) value;
        for (int k = 0; k < achunks.length; k++) {
            int chunkid = achunks[k];
            this.setChunk(chunkid, val);
        }
    }
}
