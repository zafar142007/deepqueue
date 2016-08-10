package com.zafar.miniq;


/**
 * This will be scheduled for periodic runs with the period of timeout 
 * @author zafar
 *
 * @param <A>
 */
public abstract class CleanupThread<A> implements Runnable{
	
	protected MiniQ<A> queue;
	protected Backlog<A> backlog;
	
	public CleanupThread(Backlog<A> backlog, MiniQ<A> miniQ) {
		this.backlog=backlog;
		queue=miniQ;
	}
	/**
	 * calculate the time interval in terms of timeOutInSeconds since the epoch, 
	 * push messages into the MinQ from the front all messages from intervals till EPOCH 
	 * except the interval timeOutInSeconds from now in the past
	 */
	public abstract void run();
}
