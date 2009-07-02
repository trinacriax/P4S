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
    private long last_contact;
    private int contacts;
    //altri campi

    public NeighborElement(Node neighbor, long delay){
        this.neighbor = neighbor;
        this.delay = delay;
        this.last_contact = -1;// this.pullcontact = 0;
        this.contacts = 1;
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
 public int getContacts(){
    return  this.contacts;
 }
 public void addContact(){
     this.contacts++;
 }

    public String toString(){
        return "Node "+ this.neighbor.getIndex()+" delay "+this.delay;
    }

    public void setContactTime(long value){
        this.last_contact = value;
    }
    public long getContactTime(){
        return this.last_contact;
    }

//    public void setPulltime(long value){
//        this.pullcontact = value;
//    }
//    public long getPulltime(){
//        return this.pullcontact;
//    }
}
