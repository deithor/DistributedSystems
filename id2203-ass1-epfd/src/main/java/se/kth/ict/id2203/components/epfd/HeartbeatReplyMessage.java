package se.kth.ict.id2203.components.epfd;

import se.kth.ict.id2203.ports.pp2p.Pp2pDeliver;
import se.sics.kompics.address.Address;

public class HeartbeatReplyMessage extends Pp2pDeliver {

	private static final long serialVersionUID = 2193713542080123560L;
	
	private long seqnum;

	public HeartbeatReplyMessage(Address source,long seqnum) {
		super(source);
		this.seqnum = seqnum;
	}

	public final long getSeqnum() {
		return seqnum;
	}
	public final void setSeqnum(long seqnum) { this.seqnum = seqnum; }
}