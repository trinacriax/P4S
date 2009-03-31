package p4s.util;

public interface Message {
	/**
	 * COSTANTI PER LA GESTIONE DEI CICLI DEL NODO
	 */
		public final int PUSH_CYCLE = 0;
		public final int PULL_CYCLE = 1;





	/**
	 * 
	 * INTERLEAVE ED MESSAGE CODE
	 * 
	 * */
	/**
	 * Messaggio di PUSH
	 * */
	public final int PUSH = 3;	
	/**
	 * Messaggio di PULL
	 * */
	public final int PULL = 4;
	/**
	 * Messaggio di inizio trasmissione PUSH
	 * */
	public final int START_PUSH = 6;
	/**
	 * Messaggio di inizio trasmissione PULL
	 * */
	public final int START_PULL = 7;
	/**
	 * Messaggio di fine trasmissione PUSH
	 * */
	public final int FINISH_PUSH = 8 ;
	/**
	 * Messaggio di fine trasmissione PULL
	 * */
	public final int FINISH_PULL = 10;
	/**
	 * Messaggio di OK per il PUSH
	 * */
	public final int OK_PUSH = 12;
	/**
	 * Messaggio di OK per il PULL
	 * */
	public final int OK_PULL = 14;
	/**
	 * Messaggio di chunk non disponibile
	 * 
	 * Nella fase di Pull sul chunk ID, se il 
	 * ricevente non ha quel chunk richiesto 
	 * risponde con questo messaggio
	 * */
	public final int NO_CHUNK_UNAVAILABLE = 16;
	/**
	 * Messaggio di chunk già posseduto
	 * 
	 * Nella fase di push, se il ricevente ha 
	 * già quel chunk risponde con questo
	 * messaggio
	 * */
	public final int NO_CHUNK_OWNED = 18;
	/**
	 * Messaggio di nodo in pulling
	 * 
	 * Nella fase di pull, se il nodo è già
	 * occupato con un altro nodo, e non può
	 * soddisfare le richieste del sender 
	 * perché ha tutta la banda occupata
	 * risponde con questo messaggio 
	 * */
	public final int IN_PULLING = 20;
	
	public final int NO_UPLOAD_BANDWIDTH_PUSH = 26;
	public final int NO_UPLOAD_BANDWIDTH_PULL = 28;
	
	public final int NO_DOWNLOAD_BANDWIDTH_PUSH = 30;
	public final int NO_DOWNLOAD_BANDWIDTH_PULL = 32;

	public final int SWITCH_PUSH = 80;
	
	public final int SWITCH_PULL = 90;




/**
 *  
 * COSTANTE PER IL CALCOLO DEL DELAY IN MILLISECONDI
 * 
 * */	
	public final int MILLISECONDI = 1000;	
	
	

/**
 * 
 * COSTANTI PER LA GESTIONE DELLA LISTA DI CHUNKS
 * 
 * */
	public final long OWNED = -1 ;
	public final long IN_DOWNLOAD = -2;
	public final long NOT_OWNED = -3;


    
	/**
	 * 
	 * COSTANTI PER IDENTIFICAZIONE TIPO CONNESSIONE
	 * 
	 * */
//
//	public final int UPLINK = 1011;
//	public final int DOWNLINK = 2011;

	
//	public final String LIST = "listofchunk";
//	public final String NULL = "empty";
	
        //fattore divisore per banda minima
//        public final double mini = 4;
}
