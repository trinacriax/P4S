package p4s.util;

/**
 * This interface contains all the main constants used to define the protocol and the corresponding error codes.
 * @author Alessandro Russo <russo@disi.unitn.it>
 */
public interface Message {

    /**
     * Constant representing the PUSH cycle
     */
    public final int PUSH_CYCLE = 0;
    /**
     * Constant representing the PULL cycle
     */
    public final int PULL_CYCLE = 1;
    /**
     * Offering a PUSH
     * */
    public final int PUSH = 3;
    /**
     * Requesting a PULL
     * */
    public final int PULL = 4;
    /**
     * Starting PUSH
     * */
    public final int START_PUSH = 6;
    /**
     * Starting PULL
     * */
    public final int START_PULL = 7;
    /**
     * Finish PUSH
     * */
    public final int FINISH_PUSH = 8;
    /**
     * Finish PULL
     * */
    public final int FINISH_PULL = 10;
    /**
     * Accept PUSH
     * */
    public final int OK_PUSH = 12;
    /**
     * Accept PULL
     * */
    public final int OK_PULL = 14;
    /**
     * The chunk is not available at the node. Issued by pull request.
     * */
    public final int NO_CHUNK_UNAVAILABLE = 16;
    /**
     * The chunk is already owned. Issued by a push offer.
     * */
    public final int NO_CHUNK_OWNED = 18;
    /**
     * The pulled node is satisfying another PULL. It refuses the transmission.
     * */
    public final int IN_PULLING = 20;
    /**
     * The node pulled does not have enough upload bandwidth to satisfy the pull request.
     */
    public final int NO_UPLOAD_BANDWIDTH_PULL = 28;
    /**
     * The node pushed does not have enough download bandwidth to receive the push.
     */
    public final int NO_DOWNLOAD_BANDWIDTH_PUSH = 30;
    /**
     * Switching to PUSH phase.
     */
    public final int SWITCH_PUSH = 80;
    /**
     * Switching to PULL phase.
     */
    public final int SWITCH_PULL = 90;
    /**
     * Time unit in ms.
     * */
    public final int TIME_UNIT = 1000;
    /**
     * Chunk with normalized value greater than this was received.
     * */
    public final long OWNED = -1;
    /**
     *  Chunk with this normalized value is in download.
     */
    public final long IN_DOWNLOAD = -2;
    /**
     *  Chunk with this normalized value is not owned.
     */
    public final long NOT_OWNED = -3;
    /**
     *  Chunk with this normalized value was skipped.
     */
    public final long SKIPPED = -5;
}
