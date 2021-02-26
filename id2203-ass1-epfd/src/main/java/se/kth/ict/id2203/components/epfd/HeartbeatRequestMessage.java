package se.kth.ict.id2203.components.epfd;

import se.kth.ict.id2203.ports.pp2p.Pp2pDeliver;
import se.sics.kompics.address.Address;

public class HeartbeatRequestMessage extends Pp2pDeliver {

	private static final long serialVersionUID = 9193713942730123560L;

	private long seqnum;

	public HeartbeatRequestMessage(Address source, long seqnum) {
		super(source);
		this.seqnum = seqnum;
	}

	public final long getSeqnum() {
		return seqnum;
	}
	public final void setSeqnum(long seqnum) { this.seqnum = seqnum; }
}