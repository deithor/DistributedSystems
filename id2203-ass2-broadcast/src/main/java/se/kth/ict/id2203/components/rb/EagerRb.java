package se.kth.ict.id2203.components.rb;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.kth.ict.id2203.ports.beb.BebBroadcast;
import se.kth.ict.id2203.ports.beb.BestEffortBroadcast;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Positive;
import se.sics.kompics.Negative;
import se.sics.kompics.Start;
import se.sics.kompics.address.Address;
import se.kth.ict.id2203.ports.rb.RbBroadcast;
import se.kth.ict.id2203.ports.rb.ReliableBroadcast;

public class EagerRb extends ComponentDefinition {

	private static final Logger logger = LoggerFactory.getLogger(EagerRb.class);

	private Positive<BestEffortBroadcast> bebPos = requires(BestEffortBroadcast.class);
	private Negative<ReliableBroadcast> rbNeg = provides(ReliableBroadcast.class);

	private Address selfAddress;
	private Integer seqNum;
	private Set<RbDataMessage> delivered;

	public EagerRb(EagerRbInit init) {
		selfAddress = init.getSelfAddress();
		seqNum = 0;
		delivered = new HashSet<RbDataMessage>();

		subscribe(handleRbBroadcast, rbNeg);
		subscribe(handleBebDelivery, bebPos);
	}

	/*
	 upon event < rb, Broadcast | m > do
	   seqnum := seqnum + 1;
       trigger < beb, Broadcast | [Data, seqnum, self , m] >;
	 */
	private Handler<RbBroadcast> handleRbBroadcast = new Handler<RbBroadcast>() {

		@Override
		public void handle(RbBroadcast event) {
			seqNum++;
			trigger(new BebBroadcast(new RbDataMessage(selfAddress,
					event.getDeliverEvent(), seqNum)), bebPos);
		}

	};

	/*
	upon event〈beb,Deliver|p,[Data,sn,s,m]〉do
	  if(sn,rank(s)) !∈ delivered then
	    delivered:=delivered∪{(sn,rank(s))};
	    trigger〈rb,Deliver|s,m〉;
	    trigger〈beb,Broadcast|[Data,sn,s,m]〉;
	 */
	private Handler<RbDataMessage> handleBebDelivery = new Handler<RbDataMessage>() {

		@Override
		public void handle(RbDataMessage event) {
			
			if (!delivered.contains(event)) {
				delivered.add(event);

				trigger(event.getData(), rbNeg);
				trigger(new BebBroadcast(event), bebPos);
			}
		}
	};
}
