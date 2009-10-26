package p4s.core;

import peersim.core.Node;

/**
 * Interface which provides the main methods for setting the protocol parameters.
 * @author Alessandro Russo <russo@disi.unitn.it>
 */
public interface AlternateDataSkeleton {

    public boolean addChunk(int index, int method);

    public void Initialize(int n);

    public void resetAll();

    public void setSource(int _source);

    public void setCycle(int ciclo);

    public void setDebug(int debug);

    public void setNumberOfChunks(int _number_of_chunks);

    public int getNumberOfChunks();

    public void setChunkSize(long _chunk_size);

    public long getChunkSize();

    public void setCompleted(long value);

    public long getCompleted();

    public void setPushRetry(int push_retries);

    public void setPullRetry(int pull_retries);

    public void setSwitchTime(long time);

    public void setNewChunkDelay(long delay);

    public void setPushWindow(int window);

    public void setPullWindow(int window);

    public String getNeighborhood(Node node, int pid);

    public void setBandwidth(int bandiwdthp);

    public void setNeighborKnowledge(int value);

    public void setPlayoutTime(int time_sec);

    public void setCurrent(Node current);

    public void setPullRounds(int rounds);
}

