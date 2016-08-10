package com.zafar.miniq.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.zafar.miniq.Backlog;
import com.zafar.miniq.MiniQ;

public class BacklogImpl<A> extends Backlog<A>{

	private ReentrantReadWriteLock lock= new ReentrantReadWriteLock();
	@Override
	public void deleteFromBacklog(String messageId) {
		long timestamp=MiniQ.getTimeStampFromId(messageId);	
		long bucket=calculateBucket(timestamp);
		lock.writeLock().lock();
		Map<Long, A> packets=unacknowledgedPackets.get(bucket);
		packets.remove(timestamp);
		lock.writeLock().unlock();
	}

	public static long calculateBucket(long timestamp){
		return (timestamp/1000)*1000;
	}
	@Override
	public void addToBacklog(long timestamp, A packet) {
		long bucket=calculateBucket(timestamp);
		lock.writeLock().lock();
		Map<Long, A> packets=unacknowledgedPackets.get(bucket);
		if(packets==null){
			ConcurrentHashMap<Long,A> map=new ConcurrentHashMap<Long, A>();
			map.put(timestamp, packet);			
			unacknowledgedPackets.put(bucket, map);
		}
		else
			packets.put(timestamp, packet);
		lock.writeLock().unlock();
	}
	
}
