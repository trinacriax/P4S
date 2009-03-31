package p4s.core;

import peersim.core.*;
import peersim.config.Configuration;
/**
 * This class is similar to IdleProtocol, but it implements some
 * randomized GET methods and allows to remove some neighbors, 
 * providing a dynamic neighborhood
 *
 * @author ax
 */
public class RandomizedNeighbor implements Protocol, Linkable
{

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
private static final String PAR_MU = "mudelay";
private static final String PAR_DEV = "devdelay";
private static final String PAR_DELAY = "delay";
// --------------------------------------------------------------------------
// Fields
// --------------------------------------------------------------------------

/** Neighbors */
protected Node[] neighbors;

/** Actual number of neighbors in the array */
protected int len;
private final long min;
private final long max;
private final double mu;
private final double dev;

// --------------------------------------------------------------------------
// Initialization
// --------------------------------------------------------------------------

public RandomizedNeighbor(String s)
{
	neighbors = new Node[Configuration.getInt(s + "." + PAR_INITCAP,
			DEFAULT_INITIAL_CAPACITY)];
	len = 0;
    min = Configuration.getLong(prefix + "." + PAR_MINDELAY,0);
	max = Configuration.getLong(prefix + "." + PAR_MAXDELAY,0);
    mu = Configuration.getDouble(prefix + "." + PAR_MU,0);
	dev = Configuration.getDouble(prefix + "." + PAR_DEV,0);
}

//--------------------------------------------------------------------------

public Object clone()
{
	RandomizedNeighbor rn = null;
	try { rn = (RandomizedNeighbor) super.clone(); }
	catch( CloneNotSupportedException e ) {} // never happens
	rn.neighbors = new Node[neighbors.length];
	System.arraycopy(neighbors, 0, rn.neighbors, 0, len);
	rn.len = len;
	return rn;
}

// --------------------------------------------------------------------------
// Methods
// --------------------------------------------------------------------------

public boolean contains(Node n)
{
	for (int i = 0; i < len; i++) {
		if (neighbors[i] == n)
			return true;
	}
	return false;
}

// --------------------------------------------------------------------------

/** Adds given node if it is not already in the network.*/
public boolean addNeighbor(Node n)
{
	for (int i = 0; i < len; i++) {
		if (neighbors[i] == n)
			return false;
	}
	if (len == neighbors.length) {
            int newlen =(int)((3.0 * neighbors.length)/2.0);
		Node[] temp = new Node[newlen];
		System.arraycopy(neighbors, 0, temp, 0, neighbors.length);
		neighbors = temp;
	}
	neighbors[len] = n;
	len++;
	return true;
}

// --------------------------------------------------------------------------
/**Get the i-th neighbor*/
public Node getNeighbor(int i)
{
	return neighbors[i];
}

/**Get a randomly selected neighbor*/
public Node getRNDNeighbor()
{
    this.permutation();
    int out = CommonState.r.nextInt(len);
    Node tmp = this.neighbors[len-1];
    this.neighbors[len-1] = this.neighbors[out];
    this.neighbors[out] = tmp;
    return this.neighbors[len-1];
}

/**Performs a permutation of the neighbors*/
public void permutation(){
    Node swap = null;
    for(int i = 0; i < len;i++){
        int out = CommonState.r.nextInt(len-i);
        swap = this.neighbors[out];
        this.neighbors[out] = this.neighbors[i];
        this.neighbors[i] = swap;
    }
}

// --------------------------------------------------------------------------

public int degree()
{
	return len;
}

// --------------------------------------------------------------------------

public void pack()
{
	if (len == neighbors.length)
		return;
	Node[] temp = new Node[len];
	System.arraycopy(neighbors, 0, temp, 0, len);
	neighbors = temp;
}

// --------------------------------------------------------------------------

public String toString()
{
	if( neighbors == null ) return "DEAD!";
	StringBuffer buffer = new StringBuffer();
	buffer.append("len=" + len + " maxlen=" + neighbors.length + " [");
	for (int i = 0; i < len; ++i) {
		buffer.append(neighbors[i].getIndex() + " ");
	}
	return buffer.append("]").toString();
}

// --------------------------------------------------------------------------

public void onKill()
{
	neighbors = null;
	len = 0;
}

/**Removes a given node in the neighborhood*/
public Node remNeighbor(Node neighbour){
    if(!this.contains(neighbour))
        return null;
    for(int i = 0 ; i < this.neighbors.length ; i++){
        if(this.neighbors[i] == neighbour){
            this.neighbors[i] = null;
            if(this.neighbors[0]!=null)
                this.neighbors[i] = this.neighbors[0];
            Node[] temp = new Node[neighbors.length];            
            System.arraycopy(neighbors, 1, temp, 0, neighbors.length-1);
            neighbors = temp;
            len--;
            return neighbour;
        }    
    }
    return null;
}
}

