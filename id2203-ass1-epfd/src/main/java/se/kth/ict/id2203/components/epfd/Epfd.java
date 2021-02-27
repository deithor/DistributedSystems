package se.kth.ict.id2203.components.epfd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.kth.ict.id2203.ports.epfd.EventuallyPerfectFailureDetector;
import se.kth.ict.id2203.ports.epfd.Restore;
import se.kth.ict.id2203.ports.epfd.Suspect;
import se.kth.ict.id2203.ports.pp2p.PerfectPointToPointLink;
import se.kth.ict.id2203.ports.pp2p.Pp2pSend;
import se.sics.kompics.*;
import se.sics.kompics.address.Address;
import se.sics.kompics.timer.ScheduleTimeout;
import se.sics.kompics.timer.Timer;

import java.util.HashSet;
import java.util.Set;

public class Epfd extends ComponentDefinition {

	private static final Logger logger = LoggerFactory.getLogger(Epfd.class);
	private Positive<Timer> timer = requires(Timer.class);

	private Positive<PerfectPointToPointLink> pp2pPos;
	private Positive<EventuallyPerfectFailureDetector> epfdPos;
	private Negative<PerfectPointToPointLink> pp2pNeg;
	private Negative<EventuallyPerfectFailureDetector> epfdNeg;

	private HashSet<Address> suspected;
	private HashSet<Address> alive;
	private HashSet<Address> allProcesses;
	private long seqnum;
	private long delay;
	private final long deltaDelay;
	private Address selfAddress;

	public Epfd(EpfdInit init) {
		seqnum = 0;
		// something strange with hashset
		Set temp = init.getAllAddresses();
		alive = new HashSet<>(temp);
		//alive = new HashSet<>();
		suspected = new HashSet<>();
		allProcesses = new HashSet<>(temp);
		delay = init.getInitialDelay();
		deltaDelay = init.getDeltaDelay();
		selfAddress = init.getSelfAddress();

		pp2pPos = requires(PerfectPointToPointLink.class);
		epfdNeg = provides(EventuallyPerfectFailureDetector.class);
		pp2pNeg = provides(PerfectPointToPointLink.class);
		epfdPos = requires(EventuallyPerfectFailureDetector.class);

		subscribe(handleStart, control);
		subscribe(handleCheckTimeout, timer);
		subscribe(handleReply, pp2pPos);
		subscribe(handleRequest, pp2pPos);
	}

	private Handler<Start> handleStart = new Handler<Start>() {
		@Override
		public void handle(Start event) {
			ScheduleTimeout st = new ScheduleTimeout(delay);
			st.setTimeoutEvent(new CheckTimeout(st));
			trigger(st, timer);
		}
	};

	private Handler<CheckTimeout> handleCheckTimeout = new Handler<CheckTimeout>() {
		@Override
		public void handle(CheckTimeout event) {
			logger.info("alive: "+alive.toString());
			logger.info("suspected: "+ suspected.toString());
			HashSet<Address> aliveAndSuspected = new HashSet<>(suspected);
			aliveAndSuspected.retainAll(alive);
			if (!aliveAndSuspected.isEmpty()) {
				delay += deltaDelay;
				logger.info("delay: "+ delay);
			}
			++seqnum;
			for (Address process : allProcesses){
				if (!alive.contains(process) && !suspected.contains(process)) {
					suspected.add(process);
					trigger(new Suspect(process), epfdNeg);
				}
				else if (alive.contains(process) && suspected.contains(process)){
					suspected.remove(process);
					trigger(new Restore(process), epfdNeg);
				}
				trigger(new Pp2pSend(process, new HeartbeatRequestMessage(selfAddress, seqnum)), pp2pPos);
			}
			alive.clear();

			ScheduleTimeout st = new ScheduleTimeout(delay);
			st.setTimeoutEvent(new CheckTimeout(st));
			trigger(st, timer);
		}
	};

	private Handler<HeartbeatRequestMessage> handleRequest = new Handler<HeartbeatRequestMessage>() {
		@Override
		public void handle(HeartbeatRequestMessage event) {
			trigger(new Pp2pSend(event.getSource(),
					new HeartbeatReplyMessage(selfAddress, event.getSeqnum())),
					pp2pPos);
		}
	};

	private Handler<HeartbeatReplyMessage> handleReply = new Handler<HeartbeatReplyMessage>() {
		@Override
		public void handle(HeartbeatReplyMessage event) {
			Address process = event.getSource();
			if (event.getSeqnum() == seqnum || suspected.contains(process)) alive.add(process);
		}
	};
}