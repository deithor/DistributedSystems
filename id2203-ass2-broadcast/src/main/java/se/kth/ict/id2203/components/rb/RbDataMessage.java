package se.kth.ict.id2203.components.rb;

import se.kth.ict.id2203.ports.beb.BebDeliver;
import se.kth.ict.id2203.ports.rb.RbDeliver;
import se.sics.kompics.address.Address;

public class RbDataMessage extends BebDeliver {

	private static final long serialVersionUID = 6841283273335602377L;

	private final RbDeliver data;
	private final Integer seqNum;

	public RbDataMessage(Address source, RbDeliver data, Integer seqNum) {
		super(source);
		this.data = data;
		this.seqNum = seqNum;
	}
	
	public RbDeliver getData() {
		return data;
	}

	public Integer getSeqNum() {
		return seqNum;
	}

	@Override
	public boolean equals(Object other) {
		boolean equal = false;

		if (other instanceof RbDataMessage) {
			equal = (this.seqNum.equals(((RbDataMessage) other)
					.getSeqNum()) && super.getSource().equals(
					((RbDataMessage) other).getSource()));
		}
		
		return equal;
	}
	
	@Override
	public int hashCode() {
		return (this.seqNum * super.getSource().hashCode());
	}
}
