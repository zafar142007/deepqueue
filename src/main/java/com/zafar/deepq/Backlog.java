package com.zafar.deepq;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class Backlog {
	protected ConcurrentHashMap<Long, ConcurrentHashMap<Long,WritablePacket>> unacknowledgedPackets=new ConcurrentHashMap<Long, ConcurrentHashMap<Long,WritablePacket>>();

	/**
	 * extract timestamp from messageId, calculate the bucket in which it would fall into, and delete it from that bucket
	 * @param messageId
	 */
	public abstract void deleteFromBacklog(String messageId);
	/**
	 * delete from the map the entry with this key
	 * @param timeBucket
	 */
	public abstract Map<Long, WritablePacket> removeFromBacklog(long timeBucket);
	/**
	 * calculate the bucket from timestamp, and push the packet into it
	 * @param messageId
	 * @param packet
	 */
	public abstract void addToBacklog(WritablePacket packet);
}
