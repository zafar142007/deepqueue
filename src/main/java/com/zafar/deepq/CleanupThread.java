package com.zafar.deepq;

import com.zafar.deepq.impl.BacklogImpl;


/**
 * This will be scheduled for periodic runs with the period of timeout 
 * @author zafar
 *
 * @param <A>
 */
public abstract class CleanupThread implements Runnable{
	
	protected DeepQ queue;
	protected BacklogImpl backlog;
	
	public CleanupThread(BacklogImpl backlog, DeepQ miniQ) {
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
