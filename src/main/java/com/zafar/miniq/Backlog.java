package com.zafar.miniq;

import java.util.concurrent.ConcurrentHashMap;

public abstract class Backlog<A> {
	protected ConcurrentHashMap<Long, ConcurrentHashMap<Long,A>> unacknowledgedPackets=new ConcurrentHashMap<Long, ConcurrentHashMap<Long,A>>();

	/**
	 * extract timestamp from messageId, calculate the bucket in which it would fall into, and delete it from that bucket
	 * @param messageId
	 */
	public abstract void deleteFromBacklog(String messageId);
	/**
	 * delete from the map the entry with this key
	 * @param timeBucket
	 */
	public void deleteFromBacklog(long timeBucket){
		unacknowledgedPackets.remove(timeBucket);
	}
	/**
	 * calculate the bucket from timestamp, and push the packet into it
	 * @param messageId
	 * @param packet
	 */
	public abstract void addToBacklog(long timestamp, A packet);
}
