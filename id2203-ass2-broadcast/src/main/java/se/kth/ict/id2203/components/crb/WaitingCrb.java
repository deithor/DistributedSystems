/**
 * This file is part of the ID2203 course assignments kit.
 * 
 * Copyright (C) 2009-2013 KTH Royal Institute of Technology
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package se.kth.ict.id2203.components.crb;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.kth.ict.id2203.ports.crb.CausalOrderReliableBroadcast;
import se.kth.ict.id2203.ports.crb.CrbBroadcast;
import se.kth.ict.id2203.ports.rb.RbBroadcast;
import se.kth.ict.id2203.ports.rb.ReliableBroadcast;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Positive;
import se.sics.kompics.Start;
import se.sics.kompics.address.Address;

public class WaitingCrb extends ComponentDefinition {

	private static final Logger logger = LoggerFactory.getLogger(WaitingCrb.class);
	
	private Positive<ReliableBroadcast> rbPos = requires(ReliableBroadcast.class);
	private Negative<CausalOrderReliableBroadcast> crbNeg = provides(CausalOrderReliableBroadcast.class);
	
	private final Address selfAddress;
	private final Set<Address> addresses;

	private Integer seqNum;
	private List<CrbDataMessage> pending;
	private int[] V;
	
	public WaitingCrb(WaitingCrbInit init) {
		selfAddress = init.getSelfAddress();
		addresses = new HashSet<Address>(init.getAllAddresses());
		seqNum = 0;
		pending = new LinkedList<CrbDataMessage>();

		V = new int[addresses.size()];
		Arrays.fill(V, 0);

		subscribe(handleCrbBroadcast, crbNeg);
		subscribe(handleRbDeliver, rbPos);
	}
	/*
    upon event〈crb,Broadcast|m〉do
      W:=V;
      W[rank(self)] :=lsn;
      lsn:=lsn+ 1;
      trigger〈rb,Broadcast|[Data,W,m]〉;
     */
	private Handler<CrbBroadcast> handleCrbBroadcast = new Handler<CrbBroadcast>() {

		@Override
		public void handle(CrbBroadcast event) {
			int[] newVector = V.clone();
			newVector[selfAddress.getId() - 1] = seqNum;
			seqNum++;
			CrbDataMessage msg = new CrbDataMessage(selfAddress, event.getDeliverEvent(), newVector);
			trigger(new RbBroadcast(msg), rbPos);
		}
	};
	/*
	upon event〈rb,Deliver|p,[Data,W,m]〉do
	  pending:=pending∪{(p,W,m)};
	  while exists(p′,W′,m′)∈pending such that W′≤V do
	    pending:=pending\{(p′,W′,m′)};
	    V[rank(p′)] :=V[rank(p′)] + 1;
	    trigger〈crb,Deliver|p′,m′〉;
	 */
	private Handler<CrbDataMessage> handleRbDeliver = new Handler<CrbDataMessage>() {

		@Override
		public void handle(CrbDataMessage event) {
			pending.add(event);

			Collections.sort(pending, new Comparator<CrbDataMessage>() {
				public int compare (CrbDataMessage obj0, CrbDataMessage obj1) {
					if (obj0.getV().equals(obj1.getV())) {
						return obj0.getSource().getId() < obj1.getSource().getId() ? -1 : 1;
					} else if (lessThanEqual(obj0.getV(), obj1.getV())){
						return -1;
					} else {
						return 1;
					}
				}
			});

			Iterator<CrbDataMessage> pendingIter = pending.iterator();
			CrbDataMessage dataTemp;

			while (pendingIter.hasNext()) {
				dataTemp = pendingIter.next();

				if (lessThanEqual(dataTemp.getV(), V)) {
					pendingIter.remove();
					V[dataTemp.getSource().getId() - 1]++;
					trigger(dataTemp.getData(), crbNeg);
				} else {
					break;
				}
			}
		}
	};

	private boolean lessThanEqual(int[] vector0, int[] vector1) {
		boolean result = false;

		if (vector0.length == vector1.length) {
			for (int i = 0; i < vector0.length; i++) {
				if (vector0[i] <= vector1[i])
					result = true;
				else {
					result = false;
					break;
				}
			}
		}

		return result;
	}
}
