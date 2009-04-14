/**
 * This class implements the core protocol ALTERNATE: each node alternates push and pull states.
 * In PUSH each node pushes the latest chunk it possesses to a target peer selected randomly.
 * In PULL each node pulls the oldes chunk not owned to a target peer selected randomly.
 * No signaling a part from push/pull proposes.
 *
 * @author Alessandro Russo
 * @version 1.1
 */
package p4s.core;

import p4s.transport.*;
import p4s.util.*;
import peersim.config.*;
import peersim.core.*;
import peersim.cdsim.*;
import peersim.edsim.*;
import bandwidth.*;

public class Alternate extends AlternateDataStructure implements CDProtocol, EDProtocol {
//--------------------------------------------------------------------------
// Initialization
//--------------------------------------------------------------------------
    /**
     * @param prefix string prefix for config properties
     */
    public Alternate(String prefix) {
        super(prefix);
    }

    /**
     * This is the standard method the define periodic activity.
     * The frequency of execution of this method is defined by a
     * {@link peersim.edsim.CDScheduler} component in the configuration.
     */
    public void nextCycle(Node node, int pid) {        
    }

    /**
     * This method simulates a message from a {@peersim.core.Node} source to a receiver {@peersim.core.Node}
     * @param Node src Sender node
     * @param Node dest Receiver node
     * @param Object msg Message to deliver
     * @param int pid Protocol identifier
     */
    public long send(Node src, Node dest, Object msg, int pid) {
        DelayedNeighbor dn = (DelayedNeighbor) src.getProtocol(FastConfig.getLinkable(pid));
//        long delay = dn.getNeighbor(dest).getDelay();       
        long delay = dn.delays[src.getIndex()][dest.getIndex()];
        delay = (long) (Math.ceil(delay / 2.0));
        this.send(src, dest, msg, delay, pid);
        return delay;
    }

    /**
     * It is simply a shortcut to add event with a given delay in the queue of the simulator.
     */
    public void send(Node src, Node dest, Object msg, long delay, int pid) {
        ((TransportP4S) src.getProtocol(FastConfig.getTransport(pid))).sendControl(src, dest, msg, delay, pid);
    }

    /**
     *
     * This is the main method that implements the asynchronous ALTERNATE.
     * Each "case" corresponds to a state of the protocol where the node performs some operations.
     *
     */
    public void processEvent(Node node, int pid, Object event) {
        P4SMessage im = (P4SMessage) event;
        Alternate sender;
        Alternate receiver;
        if (im.getSender() == null) {
            System.err.println("--- Time " + CommonState.getTime() + " Node " + node.getID() + " receives a message with NULL sender, this will be skipped.");
            return;
        }
        switch (im.getMessage()) {
            case Message.SWITCH_PUSH: {
                //**************************** PUSH STATE ****************************\\
                sender = ((Alternate) (node.getProtocol(pid)));
                //Each switch message has to be sent from a node to itself
                if (im.getSender().getID() != node.getID()) {
                    if (sender.getDebug() >= 0) {
                        System.err.println("!!! Time: " + CommonState.getTime() + " Node " + im.getSender().getID() + " tries to change the state of Node " + node.getID() + " in PUSH: NOT ALLOW");
                    }
                    sender = receiver = null;
                    return;
                }
                if (sender.getDebug() >= 1) {
                    System.out.println(CommonState.getTime() + "\tNode " + node.getID() + " PUSH CYCLE (" + sender.getPushAttempt() +
                            "/" + sender.getPushRetry() + ") " + ((sender.getDebug() >= 4) ? " #Chunks " + sender.getSize() + " " + sender.getBwInfo(node) : ""));
                }
                //To do a push a node must be in PUSH :)
                if (sender.getCycle() != Message.PUSH_CYCLE) {
                    if (sender.getDebug() >= 1) 
                    System.out.println("\tNode " + node.getID() + " is in PULL, now it is in pull due to parallel pull");
                    sender = receiver = null;
                    return;
                }
                //****************************************** S O U R C E    P U S H ******************************************\\
                if (node.getIndex() == sender.getSource()) {
                    if (sender.getUpload(node) > sender.getUploadMax(node)) {
                        System.err.println(CommonState.getTime() + " errore " + sender.getUpload(node) + " > " + sender.getUploadMax(node));
                    }
                    Node peer = null;
                    long delay = 0;
                    if (sender.getCompleted() == 0 && sender.getActiveUp(node) == 0) {
                        int chunktopush = sender.getLastSRC();//source select latest chunk
                        while (sender.getActiveUp(node) < sender.getActiveUpload(node) && sender.getUpload(node) > sender.getUploadMin(node)) {
                            sender.addActiveUp(node);
//                            peer = Network.get(((RandomRLC) CommonState.r).nextInt(Network.size() - 1));                            
                            peer = sender.getNeighbor(node, pid);
                            receiver = ((Alternate) (peer.getProtocol(pid)));
                            P4SMessage imm = new P4SMessage(chunktopush, node, Message.PUSH);
                            delay = this.send(node, peer, imm, pid);
                            if (sender.getDebug() >= 2) {
                                System.out.println(CommonState.getTime() + "\t\tNode " + node.getID() + " PUSHes chunk " + imm.getChunkids() + " to Node " + peer.getID() +
                                        " MexRX " + (delay + CommonState.getTime()));
                            }
                        }
                    } else if (sender.getDebug() >= 1) {
                        System.out.println("Source " + node.getID() + " finishes to send chunks");
                    }
                    sender = receiver = null;
                    return;
                } //PUSH PERFORMED BY A NORMAL PEER
                else {//E' un nodo normale
                    if (sender.getSize() == 0) {
                        sender.setCycle(Message.PULL_CYCLE);
                        long delay = sender.getSwitchTime();
                        if (sender.getDebug() >= 3) {
                            System.out.println("\tNode " + node.getID() + " switches to PULL because it has no chunks : " + sender.getSize() +
                                    " SWITCH to PULL at time " + (CommonState.getTime() + delay));
                        }
                        sender.resetPushAttempt();
                        sender.resetActiveUp(node);
                        sender.resetSuccessUpload();
                        this.send(node, node, new P4SMessage(null, node, Message.SWITCH_PULL), delay, pid);
                        sender = receiver = null;
                        return;
                    } //Sender ha esaurito il numero di connessioni attive in upload permesse
                    else if (sender.getActiveUp(node) >= sender.getActiveUpload(node)) {
                        if (sender.getDebug() >= 4) {
                            System.out.println("\tNode " + node.getID() + " reaches maximum number of ActiveUpload");
                        }
                        sender = receiver = null;
                        return;
                    } //sender non ha più banda disponibile
                    else if (sender.getUpload(node) < sender.getUploadMin(node)) {
                        if (sender.getDebug() >= 3) {
                            System.out.print("Node " + node.getID() + " has no upload bandwidth for push: ");
                        }
                        if (sender.getActiveUp(node) == 0) {//il nodo non ha connessioni attive in upload, sta soddisfando un pull

                            long delay = sender.getSwitchTime();
                            if (sender.getDebug() >= 3) {
                                System.out.println(" SWITCH to PULL at time " + (CommonState.getTime() + delay));
                            }
                            sender.setCycle(Message.PULL_CYCLE);
                            sender.resetPushAttempt();
                            sender.resetActiveUp(node);
                            sender.resetSuccessUpload();
                            this.send(node, node, new P4SMessage(null, node, Message.SWITCH_PULL), delay, pid);
                        } else if (sender.getDebug() >= 3) {
                            System.out.println();
                        }
                        sender = receiver = null;
                        return;
                    } //Successful push
                    else if (sender.getSuccessUpload() > 0) {
                        if (sender.getDebug() >= 4) {
                            System.out.print("\tNode " + node.getID() + " has performed " + sender.getSuccessUpload() + " PUSH(es) with success ,");
                        }
                        if (sender.getActiveUp(node) == 0) {//il nodo non ha connessioni attive in upload e pu cambiare stato

                            long delay = sender.getSwitchTime();
                            if (sender.getDebug() >= 4) {
                                System.out.println(" SWITCH to PULL at time " + (CommonState.getTime() + delay));
                            }
                            sender.setCycle(Message.PULL_CYCLE);
                            sender.resetPushAttempt();
                            sender.resetActiveUp(node);
                            sender.resetSuccessUpload();
                            this.send(node, node, new P4SMessage(null, node, Message.SWITCH_PULL), delay, pid);
                        } else if (sender.getDebug() >= 4) {
                            System.out.println(" has other active transmission which have to be close before switch to pull");
                        }
                        sender = receiver = null;
                        return;
                    } //il nodo ha esaurito il numero di tentativi ammessi in push
                    else if (sender.getPushAttempt() >= sender.getPushRetry()) {//ha raggiunto il # di tentativi in push
                        if (sender.getDebug() >= 4) {
                            System.out.print("\tNode " + node.getID() + " reached max number of push attempts (" + sender.getPushAttempt() + "/" + sender.getPushRetry() + ") ");
                        }
                        if (sender.getActiveUp(node) == 0) {
                            long delay = sender.getSwitchTime();
                            if (sender.getDebug() >= 4) {
                                System.out.println(" SWITCH to PULL al tempo " + (CommonState.getTime() + delay));
                            }
                            sender.setCycle(Message.PULL_CYCLE);
                            sender.resetPushAttempt();
                            sender.resetActiveUp(node);
                            sender.resetSuccessUpload();
                            this.send(node, node, new P4SMessage(null, node, Message.SWITCH_PULL), delay, pid);
                        } else if (sender.getDebug() >= 4) {
                            System.out.println(" has other active transmission which have to be close before switch to pull");
                        }
                        sender = receiver = null;
                        return;
                    } else {
                        Node peer = null;
                        long delay = 0;
                        //****************************************** P U S H   G E N E R I C   N O D E ******************************************\\
                        int chunks_push[] = sender.getLast(sender.getPushWindow());
                        sender.addPushAttempt();//aumenta di uno il #di tentativi
                        while (sender.getActiveUp(node) < sender.getActiveUpload(node) && sender.getPushAttempt() <= sender.getPushRetry() && sender.getCycle() == Message.PUSH_CYCLE) {
                            peer = sender.getNeighbor(node, pid);
                            if (peer != null) {//there exist a target candidate
                                receiver = (Alternate) peer.getProtocol(pid);
                                sender.addActiveUp(node);
                                P4SMessage imm = new P4SMessage(chunks_push, node, Message.PUSH);
                                delay = this.send(node, peer, imm, pid);
                                if (sender.getDebug() >= 2) {
                                    System.out.println("\t\tNode " + node.getID() + ((sender.getDebug() >= 6) ? " (" + sender.getPushAttempt() + "/" + sender.getPushRetry() +
                                            ")" : "") + " PUSHes " + imm.getChunkids() + " to Node " + peer.getID() + " MexRX " + (CommonState.getTime() + delay));
                                }
                            } else {
                                while (sender.getPushAttempt() < sender.getPushRetry()) {
                                    sender.addPushAttempt();
                                }
                                if (sender.getDebug() >= 4) {
                                    System.out.println("\tNode " + node.getID() + " has its neighbors with same chunks, ");
                                }
                                if (sender.getCompleted() > 0 && sender.getSize() == sender.getNumberOfChunks()) {
                                } else {
                                    if (sender.getActiveUp(node) == 0) {
                                        delay = sender.getSwitchTime();
                                        if (sender.getDebug() >= 4) {
                                            System.out.println(" SWITCH to PULL at time " + (CommonState.getTime() + delay));
                                        }
                                        sender.setCycle(Message.PULL_CYCLE);
                                        sender.resetPushAttempt();
                                        sender.resetActiveUp(node);
                                        sender.resetSuccessUpload();
                                        this.send(node, node, new P4SMessage(null, node, Message.SWITCH_PULL), delay, pid);
                                    } else if (sender.getDebug() >= 4) {
                                        System.out.println(" has other active transmission which have to be close before switch to pull");
                                    }
                                }
                                sender = receiver = null;
                                return;
                            }
                        }
                    }
                    sender = receiver = null;
                    return;
                }
            }
            case Message.PUSH: //************************************* N O D O   R E C E I V E    P U S H   ******************************************
            {
                receiver = (Alternate) (node.getProtocol(pid));
                if (receiver.getDebug() >= 2) {
                    System.out.println(CommonState.getTime() + "\tNode " + node.getID() + " receives " + im.getMessageID() + " from " + im.getSender().getID() + " for chunk(s) " + im.getChunkids() +
                            ((receiver.getDebug() >= 6) ? " " + receiver.getBwInfo(node) + " " + receiver.getConnections() : "") + ".");

                }
                int chunktopush = -1;
                long response = Message.OWNED; //nella proposta di push i chunk sono ordinati in modo descrescente
                for (int i = 0; i < im.getChunks().length && response != Message.NOT_OWNED; i++) {//recupera il chunk con id più alto che manca al nodo target tra quelli proposti dal sender
                    chunktopush = im.getChunks()[i];
                    response = receiver.getChunk(chunktopush);
                }
                //************************** NODO NON HA BANDA ****************************
                if (receiver.getPassiveDw(node) >= receiver.getPassiveDownload(node) || receiver.getDownload(node) < receiver.getDownloadMin(node)) {//numero massimo di download passivi raggiunto
                    if (receiver.getDebug() >= 3) {
                        System.out.println("\tREFUSE - it has either reached the max number of passive downloads " + receiver.getPassiveDw(node) + "/" + receiver.getPassiveDownload(node) + " no more bandwidth in download");
                    }
                    P4SMessage imm = new P4SMessage(chunktopush, node, Message.NO_DOWNLOAD_BANDWIDTH_PUSH);
                    long delay = this.send(node, im.getSender(), imm, pid);
                    if (receiver.getDebug() >= 4) {
                        System.out.println("\tNode " + node.getID() + " sends " + imm.getMessageID() + " to " + im.getSender().getID() + " MexRx " + (CommonState.getTime() + delay));
                    }
                } //************************** NODO POSSIEDE IL CHUNK OPPURE E' IN DOWNLOAD DA UN ALTRO NODO*****************************
                else if (response != Message.NOT_OWNED || response == Message.IN_DOWNLOAD) {
                    //il Nodo ha quel chunk
                    if (receiver.getDebug() >= 3) {
                        System.out.println("\tREFUSE - chunk owned or in download (" + response + "). #Chunks " + receiver.getSize());
                    }
                    //applicare tecniche di completamento: es il receiver propone il chunk nel caso in cui abbia il chunk che sender vuole push			
                    P4SMessage imm = new P4SMessage(chunktopush, node, Message.NO_CHUNK_OWNED);
                    long delay = this.send(node, im.getSender(), imm, pid);
                    if (receiver.getDebug() >= 4) {
                        System.out.println("\tNode " + node.getID() + " sends " + imm.getMessageID() + " to " + im.getSender().getID() + " MexRx " + (CommonState.getTime() + delay));
                    }
                } //************************** NODO NON HA IL CHUNK ED ACCETTA IL PUSH ****************************
                else if (response == Message.NOT_OWNED) {
                    if (receiver.getDebug() >= 3) {
                        System.out.println("\tNode " + node.getID() + " accepts chunk " + chunktopush + " from " + im.getSender().getID());
                    }
                    P4SMessage imm = new P4SMessage(chunktopush, node, Message.OK_PUSH);
                    long delay = this.send(node, im.getSender(), imm, pid);
                    receiver.addPassiveDw(node);
                    receiver.setInDown(chunktopush);
                    if (receiver.getDebug() >= 4) {
                        System.out.println("\tNode " + node.getID() + " sends OK_PUSH to " + im.getSender().getID() +
                                " for chunks m:" + chunktopush + " MexRx " + (CommonState.getTime() + delay)+" "+ (receiver.getDebug() >= 6 ? receiver.getBwInfo(node) + " " + receiver.getConnections() + " " : " "));
                    }
                } else {
                    if (receiver.getDebug() >= 0) {
                        System.err.println("::: ATTENTION - case not threated in PUSH " + CommonState.getTime());
                    }
                    System.exit(1);
                }
                sender = receiver = null;
                return;
            }
            //************************************* S O U R C E   R I C E V E    O K    P E R   I L   P U S H ******************************************
            case Message.OK_PUSH: {	//Receiver ha accetta il PUSH di sender sul chunk
                sender = ((Alternate) (node.getProtocol(pid)));
                receiver = ((Alternate) (im.getSender().getProtocol(pid)));
                int chunktopush = im.getChunks()[0];
                long response = sender.getChunk(chunktopush);
                if (sender.getDebug() >= 1) {
                    System.out.println(CommonState.getTime() + "\tNode " + node.getID() + " PUSH CYCLE (" + sender.getPushAttempt() +
                            "/" + sender.getPushRetry() + ") " + ((sender.getDebug() >= 4) ? " #Chunks " + sender.getSize() + " " + sender.getBwInfo(node) : "") +
                            " rec-OK_PUSH from " + im.getSender().getID() + " for chunk " + chunktopush + "(" + response + ")");
                }
                //***********************************SENDER HA IL CHUNK RICHIESTO *************************************			
                if (response != Message.NOT_OWNED && response != Message.IN_DOWNLOAD) {
                    BandwidthAwareProtocol bap = (BandwidthAwareProtocol) node.getProtocol(sender.getBandwidth());
                    DelayedNeighbor dn = (DelayedNeighbor) node.getProtocol(FastConfig.getLinkable(pid));
                    long result = dn.delays[node.getIndex()][im.getSender().getIndex()];
                    result = (long) (Math.ceil(result / 2.0));
                    result = 0;
//                    if(bap.getUploadConnections().getSize() > 0)
//                        result = -1;
//                    else
                    result = bap.sendData(sender.getChunkSize(), node, im.getSender(), result, sender.getBandwidth());
                    if (result == BandwidthMessage.NO_UP || result == BandwidthMessage.NO_DOWN || result == -1) {
                        receiver.resetInDown(chunktopush);
                        if (sender.getDebug() >= 4 && result == BandwidthMessage.NO_UP) {
                            System.out.println("\tNode " + node.getID() + " has no more upload bandwidth for transmission with Node " + im.getSender().getID() + ", upload " + sender.getUpload(node));
                        } else if (sender.getDebug() >= 4 && result == BandwidthMessage.NO_DOWN) {
                            System.out.println("\tNode " + im.getSender().getID() + " has no more download bandwidth for receiving chunks from Node " + node.getID() + ", download " + receiver.getDownload(im.getSender()));
                        }
                        if (sender.getDebug() >= 4) {
                            System.out.print("\tSender Active up from " + sender.getActiveUp(node));
                        }
                        sender.remActiveUp(node);
                        if (receiver.getDebug() >= 4) {
                            System.out.println(" to " + sender.getActiveUp(node));
                        }
                        if (receiver.getDebug() >= 4) {
                            System.out.print("\tReceiver Passive down from " + receiver.getPassiveDw(im.getSender()));
                        }
                        receiver.remPassiveDw(im.getSender());
                        if (receiver.getDebug() >= 4) {
                            System.out.println(" to " + receiver.getPassiveDw(im.getSender()));
                        }
                        long delay = sender.getSwitchTime();
                        if (sender.getDebug() >= 4) {
                            System.out.println("\tNode " + node.getID() + " SWITCH to PUSHa (" + sender.getPushAttempt() + "/" + sender.getPushRetry() + ") at time " + CommonState.getTime() + " MexRX " + (CommonState.getTime() + delay));
                        }

                        this.send(node, node, new P4SMessage(null, node, Message.SWITCH_PUSH), delay, pid);
                        sender.addFailPush();
                    } else {
                        long delay = this.send(node, im.getSender(), new P4SMessage(chunktopush, node, Message.START_PUSH), pid);
                        delay += result;
                        this.send(node, im.getSender(), new P4SMessage(chunktopush, node, Message.FINISH_PUSH), delay, pid);
                        delay += sender.getSwitchTime();
                        this.send(node, node, new P4SMessage(null, node, Message.SWITCH_PUSH), delay, pid);
                        if (sender.getDebug() >= 4) {
                            System.out.println("\tNode " + node.getID() + " sends START_PUSH m:" + chunktopush + " to " + im.getSender().getID() + " MexRx " + (CommonState.getTime() + sender.getSwitchTime()));
                        }
                        if (sender.getDebug() >= 4) {
                            System.out.println("\tNode " + node.getID() + " sends FINISH_PUSH m:" + chunktopush + " to " + im.getSender().getID() + " MexRx " + (CommonState.getTime() + result));
                        }
                        if (sender.getDebug() >= 4) {
                            System.out.println("\tNode " + node.getID() + " will SWITCH to PUSHb (" + sender.getPushAttempt() + "/" + sender.getPushRetry() + ") al tempo " + (CommonState.getTime() + result + sender.getSwitchTime()));
                        }

                    }
                } else {
                    if (sender.getDebug() >= 1) {
                        System.err.println("::: ATTENTION - case not threated in OK_PUSH " + CommonState.getTime() +
                                " Receiver proposes a chunks that the sender does not own: chunk " + chunktopush);
                    }
                    System.exit(-10);
                }
                sender = receiver = null;
                return;
            }
            case Message.START_PUSH: {
                receiver = ((Alternate) (node.getProtocol(pid)));
                long chunktopush = im.getChunks()[0];
                if (receiver.getDebug() >= 1) {
                    System.out.println(CommonState.getTime() + "\tNode " + node.getID() + " PUSH CYCLE (" + receiver.getPushAttempt() +
                            "/" + receiver.getPushRetry() + ") " + ((receiver.getDebug() >= 4) ? " #Chunks " + receiver.getSize() + " " + receiver.getBwInfo(node) : "") +
                            " recSTART_PUSH " + chunktopush + " from " + im.getSender().getID());
                }
                sender = receiver = null;
                return;
            }
            case Message.FINISH_PUSH://il receiver riceve il messaggio di fine PUSH		
            {
                sender = ((Alternate) (im.getSender().getProtocol(pid)));
                receiver = ((Alternate) (node.getProtocol(pid)));
                int chunktopush = (int) im.getChunks()[0];
                if (receiver.getDebug() >= 2) {
                    System.out.println(CommonState.getTime() + "\tNode " + node.getID() + " recFINISH_PUSH " + im.getChunkids() + " from " + im.getSender().getID());
                }
                sender.remActiveUp(im.getSender());
                sender.addSuccessUpload();
                if (receiver.getCycle() == -1) {//e` la prima attivazione.
                    receiver.setCycle(Message.PUSH_CYCLE);
                    this.send(node, node, new P4SMessage(null, node, Message.SWITCH_PUSH), receiver.getSwitchTime(), pid);
                    if (sender.getDebug() >= 4) {
                        System.out.println("\t>>Node " + node.getID() + " has just been ACTIVATED!!! SWITCH PUSH at time " + (CommonState.getTime() + receiver.getSwitchTime()));
                    }
                }
                if (sender.getSource() == im.getSender().getIndex()) {
                    if (sender.getDebug() >= 4) {
                        System.out.println("\t>>Source " + im.getSender().getID() + " add last src " + chunktopush + "(" + sender.getLastSRC() + ")");
                    }
                    sender.addLastSRC(chunktopush);
                }
                receiver.remPassiveDw(node);
                receiver.addChunk(chunktopush, Message.PUSH_CYCLE);
                if (receiver.getDebug() >= 6) {
                    System.out.println("\tSender " + im.getSender().getID() + " " + sender.getConnections() + "\n\t---  Receiver " + node.getID() + " " + receiver.getBwInfo(node) + " " + receiver.getConnections());
                }
                sender = receiver = null;
                return;
            }
            case Message.NO_CHUNK_OWNED: {                //Il Nodo possiede già il chunk che si vuole pushare
                sender = (Alternate) (node.getProtocol(pid));
                long chunktopush = im.getChunks()[0];
                if (sender.getDebug() >= 3) {
                    System.out.print(CommonState.getTime() + "\tNode " + node.getID() + " " + sender.getBwInfo(node) + " " + sender.getSize() + " receives " + im.getMessageID() + " for chunk " + chunktopush + " from node " + im.getSender().getID());
                }
                sender.addFailPush();
                sender.remActiveUp(node);
                if (sender.getDebug() >= 4) {
                    System.out.println("...updating" + sender.getConnections());
                }
                long delay = sender.getSwitchTime();
                if (sender.getDebug() >= 4) {
                    System.out.println("\tNode " + node.getID() + " will SWITCH to PUSHc (" + sender.getPushAttempt() + "/" + sender.getPushRetry() + ") at time " + CommonState.getTime() + " MexRX " + (CommonState.getTime() + delay));
                }
                this.send(node, node, new P4SMessage(null, node, Message.SWITCH_PUSH), delay, pid);
                sender = receiver = null;
                return;
            }
            case Message.NO_DOWNLOAD_BANDWIDTH_PUSH: {
                sender = ((Alternate) (node.getProtocol(pid)));
                if (sender.getDebug() >= 3) {
                    System.out.println(CommonState.getTime() + "\tNode " + node.getID() + " receives a message from Node " + im.getSender().getID() + " that does not have more download bandwidth");
                }
                sender.addFailPush();
                sender.remActiveUp(node);
                long delay = sender.getSwitchTime();
                if (sender.getDebug() >= 4) {
                    System.out.println("\tNode " + node.getID() + " will SWITCH to PUSHd (" + sender.getPushAttempt() + "/" + sender.getPushRetry() + ") at time " + CommonState.getTime() + " MexRX " + (CommonState.getTime() + delay));
                }
                this.send(node, node, new P4SMessage(null, node, Message.SWITCH_PUSH), delay, pid);
                sender = receiver = null;
                return;
            }
            // ********************* CICLO DI PULL ***********************
            case Message.SWITCH_PULL: {
                receiver = ((Alternate) (node.getProtocol(pid)));
                if (receiver.getDebug() >= 2) {
                    System.out.println(CommonState.getTime() + "\tNode " + im.getSender().getID() + " PULL CYCLE (" + receiver.getPullAttempt() + "/" + receiver.getPullRetry() +
                            ") #Chunks " + receiver.getSize() + " " + receiver.getBwInfo(node));
                }
                if (im.getSender().getID() != node.getID()) {
                    if (receiver.getDebug() >= 4) {
                        System.out.println("\tNode " + node.getID() + " receives a message for changing its state " + im.getSender().getID());
                    }
                    sender = receiver = null;
                    return;
                }
                if (node.getIndex() == receiver.getSource()) {
                    System.err.println(CommonState.getTime() + "\tNode " + node.getID() + " is the source...it cannot be in PULL...ABSURDE!");
                }
                //****************************************** P U L L    N O D O    G E N E R I C O ******************************************\\
                if (receiver.getCycle() != Message.PULL_CYCLE) {
                    if (receiver.getDebug() >= 4) {
                        System.out.println("\tNode " + node.getID() + " is in PULL, now it is in push due to parallel push");
                    }
                    sender = receiver = null;
                    return;
                }
                int pull_chunks[] = receiver.getLeast(receiver.getPullWindow());
                if (receiver.getDownload(node) <= receiver.getDownloadMin(node)) {
                    if (receiver.getActiveDw(node) == 0) {
                        long delay = receiver.getSwitchTime();
                        if (receiver.getDebug() >= 3) {
                            System.out.println("\tNode " + node.getID() + " " + receiver.getBwInfo(node) + " " + receiver.getConnections() + " has low donalod bandwidth, will switch to push at time " + (CommonState.getTime() + delay));
                        }
                        receiver.setCycle(Message.PUSH_CYCLE);
                        receiver.resetActiveDw(node);
                        receiver.resetPullAttempt();
                        receiver.resetSuccessDownload();
                        this.send(node, node, new P4SMessage(null, node, Message.SWITCH_PUSH), delay, pid);
                    }
                    sender = receiver = null;
                    return;
                } // Nodo possiede tutti i chunks
                else if ((pull_chunks == null) || (receiver.getSize() == receiver.getNumberOfChunks()) || (receiver.getCompleted() != 0)) {
                    if (receiver.getDebug() >= 3) {
                        System.out.print("\tNode " + node.getID() + " has all chunks " + receiver.getSize() + " or it is receiving the last one : 1) " + pull_chunks + " 2) " +
                                (receiver.getSize() == receiver.getNumberOfChunks()) + "; 3) Completed " + receiver.getCompleted());
                    }
                    long delay = receiver.getSwitchTime();
                    if (receiver.getDebug() >= 3) {
                        System.out.println(" will SWITCH to PUSHe at time MexRX " + (CommonState.getTime() + delay));
                    }
                    receiver.setCycle(Message.PUSH_CYCLE);
                    receiver.resetActiveDw(node);
                    receiver.resetPullAttempt();
                    receiver.resetSuccessDownload();
                    this.send(node, node, new P4SMessage(null, node, Message.SWITCH_PUSH), delay, pid);
                    sender = receiver = null;
                    return;
                } else if (receiver.getSuccessDownload() > 0) {//Ha fatto un pull con successo
                    if (receiver.getDebug() >= 3) {
                        System.out.print("\tNode " + node.getID() + " has performed a " + receiver.getSuccessDownload() + " PULL with success ");
                    }
                    if (receiver.getActiveDw(node) == 0) {
                        long delay = receiver.getSwitchTime();
                        if (receiver.getDebug() >= 3) {
                            System.out.println(", will SWITCH to PUSHf at time " + CommonState.getTime() + " MexRX " + (CommonState.getTime() + delay));
                        }
                        receiver.setCycle(Message.PUSH_CYCLE);
                        receiver.resetActiveDw(node);
                        receiver.resetPullAttempt();
                        receiver.resetSuccessDownload();
                        this.send(node, node, new P4SMessage(null, node, Message.SWITCH_PUSH), delay, pid);
                    } else if (receiver.getDebug() >= 4) {
                        System.out.print("\n");
                    }
                    sender = receiver = null;
                    return;
                } else if (receiver.getActiveDw(node) >= receiver.getActiveDownload(node)) {//ha raggiunto il # di download attivi possibili
                    if (receiver.getDebug() >= 3) {
                        System.out.println("\tNode " + node.getID() + " has reached max number of active download ");
                    }
                    sender = receiver = null;
                    return;
                } else if (receiver.getPullAttempt() >= receiver.getPullRetry()) {//ha raggiunto il # di download attivi possibili

                    if (receiver.getDebug() >= 3) {
                        System.out.print("\t--- Il Node " + node.getID() + " has reached max number of PULL attempts " + receiver.getPullAttempt() + "/" + receiver.getPullRetry());
                    }
                    if (receiver.getActiveDw(node) == 0) {
                        long delay = receiver.getSwitchTime();
                        if (receiver.getDebug() >= 3) {
                            System.out.println(" will SWITCH to PUSHg at time " + CommonState.getTime() + " MexRX " + (CommonState.getTime() + delay));
                        }
                        receiver.setCycle(Message.PUSH_CYCLE);
                        receiver.resetActiveDw(node);
                        receiver.resetPullAttempt();
                        receiver.resetSuccessDownload();
                        this.send(node, node, new P4SMessage(null, node, Message.SWITCH_PUSH), delay, pid);
                    }
                    sender = receiver = null;
                    return;
                } else {//richieste pull sequenziali non parallele	
                    Node peer = null;
                    while (receiver.getActiveDw(node) < receiver.getActiveDownload(node) && receiver.getPullAttempt() < receiver.getPullRetry() && receiver.getCycle() == Message.PULL_CYCLE) {
//                        do {
                        peer = receiver.getNeighbor(node, pid);
//                        } while (peer.getIndex() == receiver.getSource());//pull non alla sorgente.
                        if (peer != null) {
                            sender = (Alternate) peer.getProtocol(pid);//!! potrei fare pull + volte allo stesso nodo...potrei usare una tabella che tenga traccia delle connessionio.
                            receiver.addActiveDw(node);
                            receiver.addPullAttempt();
                            P4SMessage imm = new P4SMessage(pull_chunks, node, Message.PULL);
                            long delay = this.send(node, peer, imm, pid);
                            if (receiver.getDebug() >= 2) {
                                System.out.println("\t\tNode " + node.getID() + " " + receiver.getPullAttempt() + "PULL " +
                                        imm.getChunkids() + "to Node " + peer.getID() + " MexRX " + (delay + CommonState.getTime()));
                            }
                        } else {
                            while (receiver.getPullAttempt() < receiver.getPullRetry()) {
                                receiver.addPullAttempt();
                            }
                            if (receiver.getDebug() >= 3) {
                                System.out.println("\t--- Il Node " + node.getID() + " does not have useful neighbors to pulls");
                            }
                            if (receiver.getActiveDw(node) == 0) {
                                long delay = receiver.getSwitchTime();
                                if (receiver.getDebug() >= 3) {
                                    System.out.println(" will SWITCH to PUSHh at time " + CommonState.getTime() + " MexRX " + (CommonState.getTime() + delay));
                                }
                                receiver.setCycle(Message.PUSH_CYCLE);
                                receiver.resetActiveDw(node);
                                receiver.resetPullAttempt();
                                receiver.resetSuccessDownload();
                                this.send(node, node, new P4SMessage(null, node, Message.SWITCH_PUSH), delay, pid);
                            }
                        }
                    }
                    sender = receiver = null;
                    return;
                }
            }
            case Message.PULL: {//Qualcuno vuole fare pull a questo nodo
                sender = (Alternate) (node.getProtocol(pid));
                if (sender.getDebug() >= 2) {
                    System.out.println(CommonState.getTime() + "\tSender " + node.getID() + " " + (sender.getDebug() >= 6 ? sender.getSize() : " ") + " receives PULL from Node " +
                            im.getSender().getID() + " for the chunk " + im.getChunkids());
                }
                receiver = (Alternate) (im.getSender().getProtocol(pid));
                int pull_chunk = -1;
                long response = Message.NOT_OWNED;
                for (int i = 0; i < im.getChunks().length && (response == Message.NOT_OWNED || response == Message.IN_DOWNLOAD); i++) {
                    pull_chunk = im.getChunks()[i];
                    response = sender.getChunk(pull_chunk);
                }
                if (sender.getSource() == node.getIndex() && sender.getLastSRC() != -1) {
                    P4SMessage imm = new P4SMessage(pull_chunk, node, Message.NO_UPLOAD_BANDWIDTH_PULL);
                    long delay = this.send(node, im.getSender(), imm, pid);
                    if (sender.getDebug() >= 3) {
                        System.out.println("\tNode " + node.getID() + " NO-UPLOAD sends " + imm.getMessageID() + " to Node " + im.getSender().getID() + " MexRX " + (CommonState.getTime() + delay));
                    }
                }//******** Il Nodo  occupato in altro pulling
                else if (sender.isPulling() > 0) {
                    P4SMessage imm = new P4SMessage(null, node, Message.IN_PULLING);
                    long delay = this.send(node, im.getSender(), imm, pid);
                    if (sender.getDebug() >= 3) {
                        System.out.println("\tNode " + node.getID() + " INPULL sends " + imm.getMessageID() + " to " + im.getSender().getID() + " MexRX " + (CommonState.getTime() + delay));
                    }
                } else if (response == Message.NOT_OWNED || response == Message.IN_DOWNLOAD) {//il Nodo non ha il chunk richiesto in PULL
                    P4SMessage imm = new P4SMessage(im.getChunks(), node, Message.NO_CHUNK_UNAVAILABLE);
                    long delay = this.send(node, im.getSender(), imm, pid);
                    if (sender.getDebug() >= 3) {
                        System.out.println("\tNode " + node.getID() + " NOTOWN sends " + imm.getMessageID() + "(" + sender.getChunk(pull_chunk) + " ) to " + im.getSender().getID() + " MexRX " + (CommonState.getTime() + delay));
                    }
                } else if (sender.getPassiveUp(node) >= sender.getPassiveUpload(node) || sender.getUpload(node) <= sender.getUploadMin(node)) {
                    receiver.remActiveDw(im.getSender());
                    sender.addFailPull();
                    this.send(im.getSender(), im.getSender(), new P4SMessage(null, im.getSender(), Message.SWITCH_PULL), sender.getSwitchTime(), pid);
                    if (sender.getDebug() >= 3) {
                        System.out.println("\tNode " + node.getID() + " sends SWITCH_PULL to " + im.getSender().getID() + " MexRX " + (CommonState.getTime() + sender.getSwitchTime()));
                    }
                } else if (response > Message.OWNED) { // il Nodo ha il chunk richiesto in PULL
                    P4SMessage imm = new P4SMessage(pull_chunk, node, Message.OK_PULL);
                    sender.setPulling();
                    sender.addPassiveUp(node);
                    long delay = this.send(node, im.getSender(), imm, pid);
                    if (sender.getDebug() >= 3) {
                        System.out.println("\tNode " + node.getID() + " sends " + imm.getMessageID() + " to Node " + im.getSender().getID() + " for the chunk " + pull_chunk +
                                " MexRX " + (CommonState.getTime() + delay));
                    }
                } else {
                    if (sender.getDebug() >= 4) {
                        System.out.println("::: ATTENTION: Node " + node.getID() + " not allowed! " + im.toString());
                    }
                    sender.toString(node);
                }
                sender = receiver = null;
                return;
            }
            case Message.OK_PULL: {   //il Nodo sender accetta il PULL
                sender = ((Alternate) (im.getSender().getProtocol(pid)));
                receiver = ((Alternate) (node.getProtocol(pid)));
                int chunktopull = im.getChunks()[0];
                if (receiver.getDebug() >= 2) {
                    System.out.println(CommonState.getTime() + "\tNode " + node.getID() + " rec" + im.getMessageID() + " from " + im.getSender().getID() + " for the chunk " +
                            im.getChunkids());
                }
                BandwidthAwareProtocol bap = (BandwidthAwareProtocol) im.getSender().getProtocol(sender.getBandwidth());
                DelayedNeighbor dn = (DelayedNeighbor) node.getProtocol(FastConfig.getLinkable(pid));
                long result = dn.delays[node.getIndex()][im.getSender().getIndex()];
                result = (long) (Math.ceil(result / 2.0));
                result = 0; 
                result = bap.sendData(sender.getChunkSize(), im.getSender(), node, result, sender.getBandwidth());
                if (result == BandwidthMessage.NO_UP || result == BandwidthMessage.NO_DOWN) {
                    receiver.addFailPull();
                    if (receiver.getDebug() >= 3 && result == BandwidthMessage.NO_UP) {
                        System.out.print(CommonState.getTime() + "\tNode " + im.getSender().getID() + " no upload: " + sender.getUpload(im.getSender()));
                    } else if (receiver.getDebug() >= 3 && result == BandwidthMessage.NO_DOWN) {
                        System.out.print(CommonState.getTime() + "\tNode " + node.getID() + " no download: " + receiver.getDownload(node));//finire il
                    }

                    if (sender.getDebug() >= 4) {
                        System.out.print("Sender Active up from " + sender.getActiveUp(im.getSender()));
                    }
                    sender.remPassiveUp(im.getSender());
                    if (receiver.getDebug() >= 4) {
                        System.out.print(" to" + sender.getActiveUp(im.getSender()));
                    }

                    if (receiver.getDebug() >= 4) {
                        System.out.print("Receiver Passive down from " + receiver.getPassiveDw(node));
                    }
                    receiver.remActiveDw(node);
                    if (receiver.getDebug() >= 4) {
                        System.out.println(" to " + receiver.getPassiveDw(node));
                    }
                    long delay = receiver.getSwitchTime();
                    if (receiver.getDebug() >= 4) {
                        System.out.println("\tNode " + node.getID() + " will SWITCH to PULLc (" + receiver.getPullAttempt() + "/" + receiver.getPullRetry() + ") at time " + CommonState.getTime() + " MexRX " + (CommonState.getTime() + delay));
                    }
                    if (sender.getPassiveUp(im.getSender()) == 0) {
                        sender.resetPulling();
                    }
                    this.send(node, node, new P4SMessage(null, node, Message.SWITCH_PULL), delay, pid);
                } else {
                    long delay = this.send(im.getSender(), node, new P4SMessage(chunktopull, im.getSender(), Message.START_PULL), pid);
                    delay += result;
                    this.send(im.getSender(), node, new P4SMessage(chunktopull, im.getSender(), Message.FINISH_PULL), delay, pid);
                    receiver.setInDown(chunktopull);
                    delay += receiver.getSwitchTime();
                    if (receiver.getDebug() >= 4) {
                        System.out.println("\tNode " + node.getID() + " SWITCH to PULL at time " + delay);
                    }
                    this.send(node, node, new P4SMessage(null, node, Message.SWITCH_PULL), delay, pid);

                }
                sender = receiver = null;
                return;
            }
            case Message.START_PULL: {	//Il receiver inizia a ricevere il chunks :)

                receiver = ((Alternate) (node.getProtocol(pid)));
                if (receiver.getDebug() >= 2) {
                    System.out.println(CommonState.getTime() + "\tNode " + node.getID() + " rec" + im.getMessageID() + " for chunk  " + im.getChunkids() + " from Node " + im.getSender().getID());
                }
                sender = receiver = null;
                return;
            }
            case Message.FINISH_PULL: {   //Il receiver finisce la ricezione del chunk
                sender = ((Alternate) (im.getSender().getProtocol(pid)));
                receiver = ((Alternate) (node.getProtocol(pid)));
                if (receiver.getDebug() >= 2) {
                    System.out.println(CommonState.getTime() + "\tNode " + node.getID() + " recFINISH_PULL " + im.getChunkids() +
                            " from " + im.getSender().getID());
                }
                sender.remPassiveUp(im.getSender());
                if (sender.getPassiveUp(im.getSender()) == 0) {
                    sender.resetPulling();
                }
                receiver.remActiveDw(node);
                receiver.addSuccessDownload();
                long chunkpulled = im.getChunks()[0];
                receiver.addChunk((int) chunkpulled, Message.PULL_CYCLE);
                sender = receiver = null;
                return;
            }
            case Message.NO_CHUNK_UNAVAILABLE: {	//Il Nodo non ha il chunk richiesto nel PULL
                receiver = ((Alternate) (node.getProtocol(pid)));
                receiver.addFailPull();
                if (receiver.getDebug() >= 3) {
                    System.out.print(CommonState.getTime() + "\tNode " + node.getID() + " pulled chunk " + im.getChunkids() + " from Node " + im.getSender().getID() +
                            " which does not own it ");
                }
                receiver.remActiveDw(node);
                if (receiver.getDebug() >= 3) {
                    System.out.println("...updating " + receiver.getConnections());
                }
                long delay = receiver.getSwitchTime();
                this.send(node, node, new P4SMessage(null, node, Message.SWITCH_PULL), delay, pid);
                if (receiver.getDebug() >= 3) {
                    System.out.println("\t--- Node " + node.getID() + " will aSWITCH to PULL (" + receiver.getPullAttempt() + "/" + receiver.getPullRetry() + "): " + receiver.getCycle() +
                            " : at time " + CommonState.getTime() + " MexRX " + (CommonState.getTime() + delay));
                }
                sender = (Alternate) (im.getSender().getProtocol(pid));
                if (sender.getPassiveUp(im.getSender()) == 0) {
                    sender.resetPulling();
                }
                sender = receiver = null;
                return;
            }
            case Message.IN_PULLING: {   //Il Nodo che  stato interrogato per un PULL occupato
                receiver = ((Alternate) (node.getProtocol(pid)));
                receiver.addFailPull();
                if (receiver.getDebug() >= 3) {
                    System.out.print(CommonState.getTime() + "\tNode " + im.getSender().getID() + " refuses pull of Node " + node.getID() + " because is in pulling " + receiver.getConnections());
                }
                receiver.remActiveDw(node);
                if (receiver.getDebug() >= 4) {
                    System.out.println("...updating " + receiver.getConnections());
                }
                long delay = receiver.getSwitchTime();
                if (receiver.getDebug() >= 4) {
                    System.out.println("\tNode " + node.getID() + " will bSWITCH to PULL (" + receiver.getPullAttempt() + "/" + receiver.getPullRetry() + ") at time " + CommonState.getTime() + " MexRX " + (CommonState.getTime() + delay));
                }
                this.send(node, node, new P4SMessage(null, node, Message.SWITCH_PULL), delay, pid);
                sender = receiver = null;
                return;
            }
            case Message.NO_UPLOAD_BANDWIDTH_PULL: {   //numero di trasmissioni massime passive in upload raggiunto
                receiver = (Alternate) (node.getProtocol(pid));
                receiver.addFailPull();
                if (receiver.getDebug() >= 3) {
                    System.out.print(CommonState.getTime() + "\tNode " + node.getID() + " receives " + im.getMessageID() + " from " + im.getSender().getID());
                }
                receiver.remActiveDw(node);
                if (receiver.getDebug() >= 4) {
                    System.out.println("...updating " + receiver.getConnections());
                }
                long delay = receiver.getSwitchTime();
                if (receiver.getDebug() >= 4) {
                    System.out.println("\tNode " + node.getID() + " cSWITCH to PULL (" + receiver.getPullAttempt() + "/" + receiver.getPullRetry() + ") at time " + CommonState.getTime() + " MexRX " + (CommonState.getTime() + delay));
                }
                sender = (Alternate) (im.getSender().getProtocol(pid));
                if (sender.getPassiveUp(im.getSender()) == 0) {
                    sender.resetPulling();
                }
                this.send(node, node, new P4SMessage(null, node, Message.SWITCH_PULL), delay, pid);
                sender = receiver = null;
                return;
            }
            case Message.NO_DOWNLOAD_BANDWIDTH_PULL: {
                sender = ((Alternate) (node.getProtocol(pid)));
                if (sender.getDebug() >= 3) {
                    System.out.println(CommonState.getTime() + "\tNode " + node.getID() + " receives NO DOWNLOAD from Node " + im.getSender().getID() + " that it does no more receive chunk in PULL");
                }
                sender.remPassiveUp(node);
                if (sender.getPassiveUp(node) == 0) {
                    sender.resetPulling();
                }
                long delay = sender.getSwitchTime();
                if (sender.getDebug() >= 4) {
                    System.out.println("\tNode " + node.getID() + " will cSWITCH to PULL (" + sender.getPullAttempt() + "/" + sender.getPullRetry() + ") at time " + CommonState.getTime() + " MexRX " + (CommonState.getTime() + delay));
                }
                this.send(node, node, new P4SMessage(null, node, Message.SWITCH_PULL), delay, pid);
                sender = receiver = null;
                return;
            }
            default: {
                System.err.println("ERRORE SWITCH CASE MESSAGES " + im.toString());
                System.exit(-23);
            }
        }
    }
}