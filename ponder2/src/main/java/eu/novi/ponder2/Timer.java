/**
 * Copyright � 2008 Kevin Twidle, Imperial College, London, England.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General
 * Public License along with this library; if not, write to the
 * Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA 02110-1301 USA
 *
 * Contact: Kevin Twidle <kpt@doc.ic.ac.uk>
 *
 */
package eu.novi.ponder2;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


//import eu.novi.ponder2.Ticker;
import eu.novi.ponder2.apt.Ponder2op;
import eu.novi.ponder2.exception.Ponder2ArgumentException;
import eu.novi.ponder2.objects.P2Hash;
import eu.novi.ponder2.objects.P2Object;

import eu.novi.ponder2.ManagedObject;

/**
 * Implements a multiple timer. Events may be fired off at regular intervals or
 * after a single interval. <br>
 * Time can be given in milliseconds as a simple number or as hh:mm:ss. e.g.
 * 0:22 would be 22 seconds.<br>
 * If the event is declared with a count attribute then the attribute will be
 * set to the event number before each event is sent.
 * 
 * @author Kevin Twidle
 * @version $Id:$
 */
public class Timer implements ManagedObject {

	Map<Integer, Ticker> tickers;
	int index;

	/**
	 * Creates a new timer managed object
	 */
	@Ponder2op("create")
	Timer() {
		tickers = new HashMap<Integer, Ticker>();
		index = 0;
	}

	/**
	 * sets up and starts a one off timer. Sends the event after the specified
	 * time.
	 * 
	 * @param time
	 *            the time before the event is sent
	 * @param event
	 *            the event to send
	 * @return the timer's index for later stopping or restarting
	 * @throws Ponder2ArgumentException
	 */
	@Ponder2op("tick:event:")
	protected int tick(P2Object source, String time, P2Object event)
			throws Ponder2ArgumentException {
		Ticker ticker = new Ticker(source, false, time, event);
		return add(ticker);
	}

	/**
	 * sets up and starts a repetitive timer that send the event periodically.
	 * 
	 * @param time
	 *            the time before the event is sent
	 * @param event
	 *            the event to send
	 * @return the timer's index for later stopping or restarting
	 * @throws Ponder2ArgumentException
	 */
	@Ponder2op("repeat:event:")
	protected int repeat(P2Object source, String time, P2Object event)
			throws Ponder2ArgumentException {
		Ticker ticker = new Ticker(source, true, time, event);
		return add(ticker);
	}

	private synchronized int add(Ticker ticker) {
		index++;
		tickers.put(index, ticker);
		return index;
	}

	/**
	 * starts the timer at anIndex
	 * 
	 * @param anIndex
	 *            the index of the timer to be started
	 */
	@Ponder2op("start:")
	protected void start(int anIndex) {
		Ticker ticker = tickers.get(anIndex);
		if (ticker != null)
			ticker.start();
	}

	/**
	 * stops the timer at anIndex
	 * 
	 * @param anIndex
	 *            the index of the timer to be stopped
	 */
	@Ponder2op("stop:")
	protected void stop(int anIndex) {
		Ticker ticker = tickers.get(anIndex);
		if (ticker != null)
			ticker.stop();
	}

	/**
	 * stops and cancels all the timers. The indexes are invalid after this
	 * operation.
	 */
	@Ponder2op("cancel")
	protected void cancel() {
		for (Iterator<Map.Entry<Integer, Ticker>> iterator = tickers.entrySet()
				.iterator(); iterator.hasNext();) {
			Map.Entry<Integer, Ticker> entry = iterator.next();
			entry.getValue().stop();
			iterator.remove();
		}
	}

	/**
	 * stops and cancels the timer at anIndex. anIndex cannot be used again.
	 * 
	 * @param anIndex
	 */
	@Ponder2op("cancel:")
	protected void cancel(int anIndex) {
		Ticker ticker = tickers.remove(anIndex);
		if (ticker != null)
			ticker.stop();
	}

	private static long decode(String time) throws Ponder2ArgumentException {
		try {
			String[] times = time.split(":");
			if (times.length == 0)
				throw new Ponder2ArgumentException("Bad time string: " + time);
			if (times.length == 1)
				return Long.parseLong(time);

			// Ok we must have hh:mm:ss or something like it.
			long msecs = 0;
			for (String number : times) {
				msecs = msecs * 60 + Long.parseLong(number);
			}
			return msecs * 1000;
		} catch (NumberFormatException e) {
			throw new Ponder2ArgumentException("Bad time string: " + time);
		}
	}

	public class Ticker implements Runnable {
		boolean repeat;
		boolean running;
		long count = 1;
		private final P2Object event;
		private final P2Object source;
		private final long msecs;
		private Thread thread;

		Ticker(P2Object source, boolean repeat, String time, P2Object event)
				throws Ponder2ArgumentException {
			this.repeat = repeat;
			running = false;
			this.source = source;
			this.msecs = decode(time);
			this.event = event;
			start();
		}

		public void start() {
			if (!running) {
				thread = new Thread(this);
				thread.start();
			}
		}

		public void stop() {
			running = false;
			if (thread != null)
				thread.interrupt();
		}

		public boolean isRunning() {
			return running;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Runnable#run()
		 */
		public void run() {
			P2Hash hash = new P2Hash();
			running = repeat;
			while (true) {
				try {
					Thread.sleep(msecs);
					hash.put("count", P2Object.create(count++));
					event.operation(source, "fromHash:", hash);
				} catch (Exception e) {
					// System.err
					// .println("Timer thread caught error, terminating: "
					// + e.getMessage());
					running = false;
				}
				if (!running)
					break;
			}
		}
	}
}
