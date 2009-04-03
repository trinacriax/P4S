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

    private Node neighbor;
    private long delay;
    //altri campi

    public NeighborElement(Node neighbor, long delay){
        this.neighbor = neighbor;
        this.delay = delay;
    }

    public Node getNeighbor(){
        return this.neighbor;
    }

    public long getDelay(){
        return this.delay;
    }
    public void setDelay(long delay){
         this.delay = delay;
    }

    public String toString(){
        return "Node "+ this.neighbor.getIndex()+" delay "+this.delay;
    }
}
