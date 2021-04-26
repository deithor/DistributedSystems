package se.kth.ict.id2203.components.crb;

import se.kth.ict.id2203.ports.crb.CrbDeliver;
import se.kth.ict.id2203.ports.rb.RbDeliver;
import se.sics.kompics.address.Address;

public class CrbDataMessage extends RbDeliver {

	private static final long serialVersionUID = -1496425738132488267L;

	private int[] V;
	private CrbDeliver data;

	public CrbDataMessage(Address source, CrbDeliver data, int[] V) {
		super(source);
		this.V = V;
		this.data = data;
	}

	public int[] getV() {
		return V;
	}

	public CrbDeliver getData() {
		return data;
	}
}
