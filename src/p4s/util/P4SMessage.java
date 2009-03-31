package p4s.util;

import peersim.core.Node;
import java.lang.reflect.*;
/**
 *
 * @author ax
 */
public class P4SMessage {

	protected final Node sender;
	protected final int MessageID;	
	protected final long bandwidth;
	protected final int[] chunkids;
	
	public P4SMessage(int[] chunkids, Node sender, int MessageID)
	{				
		this.chunkids = chunkids;
		this.sender = sender;
		this.MessageID = MessageID;
		this.bandwidth = 0;
		
	}
	
	public P4SMessage(int[] chunkids, Node sender, int MessageID, long bandwidth)
	{	
		this.chunkids = chunkids;
		this.sender = sender;
		this.MessageID = MessageID;
		this.bandwidth = bandwidth;		
	}
	
	public P4SMessage(int chunkid, Node sender, int MessageID)
	{				
		this.chunkids = new int[1];
		this.chunkids[0] = chunkid;
		this.sender = sender;
		this.MessageID = MessageID;
		this.bandwidth = 0;
		
	}
	
	public P4SMessage(int chunkid, Node sender, int MessageID, long bandwidth)
	{	
		this.chunkids = new int[1];
		this.chunkids[0] = chunkid;
		this.sender = sender;
		this.MessageID = MessageID;
		this.bandwidth = bandwidth;		
	}
	
    public Node getSender(){
        return this.sender;
    }
    
    public int getMessage(){
        return this.MessageID;
    }
    
    public int[] getChunks(){
        return this.chunkids;
    }
    
    public long getBandwidth(){
        return this.bandwidth;
    }
    
	public String toString(){
		String result = "Sender " + this.sender.getID() + ", Chunk ["+ this.getChunkids()+ "], Message "+ this.getConstantName(this.MessageID)+", Banda "+ this.bandwidth+".";	
		return result;		
	}
	
	public String getChunkids(){
		String result = "";
		if(this.chunkids==null)
			return result;
		for (int i = 0 ; i < this.chunkids.length ; i++)
			result+= "m:"+this.chunkids[i]+"; ";
		return result;
	}
	
	public String getMessageID(){
		return this.getConstantName(this.MessageID);
	}
	
	public String getConstantName(Object obj){
		Field[] myfields = Message.class.getFields();
		String clazz = obj.getClass().toString();
		if(clazz.indexOf("Integer")!=-1)
			clazz=" int ";
		else if(clazz.indexOf("Long")!=-1)
			clazz = " long ";
		else if(clazz.indexOf("String")!=-1)
			clazz = " String ";
//		System.out.println("Obj class "+ clazz);
		try{
		for(int i = 0; i < myfields.length;i++) {			
			if(myfields[i].toString().indexOf(clazz)!=-1){
				if(clazz.equals(" int ")){
					int valuez = myfields[i].getInt(myfields[i]);
					if(((Integer)obj).intValue()==valuez)
//						System.out.println("int "+ valuez+" "+myfields[i].getName());
					return myfields[i].getName();
					}
				else if(clazz.equals(" long ")){
					long valuez = myfields[i].getLong(myfields[i]);
					if(((Long)obj).longValue()==valuez)
//						System.out.println("Long "+ valuez+" "+myfields[i].getName());
						return myfields[i].getName();
				}
				else if(clazz.equals(" String ")){
					String valuez = (String)myfields[i].get(myfields[i]);
					if(((String)obj).equals(valuez))
//						System.out.println("String "+ valuez+" "+myfields[i].getName());
					return myfields[i].getName();
				}
			}
				
			
		}
		}
		catch(Exception e){System.err.println(e.toString());}
		
		return "";
		
	}
}