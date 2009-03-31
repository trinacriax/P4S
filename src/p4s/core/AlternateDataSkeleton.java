package p4s.core;

import peersim.core.Node;


public interface AlternateDataSkeleton {

public int getLast();

public boolean addChunk(int index, int method);

public int getSize();

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

public void setPushWindow(int window);
public void setPullWindow(int window);

public String getNeighborhood(Node node,int pid);

public void setBandwidth(int bandiwdthp);

public void setNeighborKnowledge(int value);
}

