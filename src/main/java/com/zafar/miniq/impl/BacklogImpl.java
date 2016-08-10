package com.zafar.miniq.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zafar.miniq.Backlog;
import com.zafar.miniq.MiniQ;
import com.zafar.miniq.WritablePacket;

public class BacklogImpl extends Backlog{

	private static Logger logger = LoggerFactory.getLogger(BacklogImpl.class);

	private ReentrantReadWriteLock lock= new ReentrantReadWriteLock();
	@Override
	public void deleteFromBacklog(String messageId) {
		logger.debug("deleting from backlog {}",messageId);
		long timestamp=MiniQ.getTimeStampFromId(messageId);	
		long bucket=calculateBucket(timestamp);
		lock.writeLock().lock();
		Map<Long, WritablePacket> packets=unacknowledgedPackets.get(bucket);
		if(packets!=null)
			packets.remove(timestamp);
		lock.writeLock().unlock();
	}

	public static long calculateBucket(long timestamp){
		return (timestamp/1000)*1000;
	}
	@Override
	public void addToBacklog(WritablePacket packet) {
		logger.debug("adding to backlog packet {}",packet);
		long timestamp=MiniQImpl.getTimeStampFromId(packet.getUuid());
		long bucket=calculateBucket(timestamp);
		lock.writeLock().lock();
		Map<Long, WritablePacket> packets=unacknowledgedPackets.get(bucket);
		if(packets==null){
			ConcurrentHashMap<Long,WritablePacket> map=new ConcurrentHashMap<Long, WritablePacket>();
			map.put(timestamp, packet);			
			unacknowledgedPackets.put(bucket, map);
		}
		else
			packets.put(timestamp, packet);
		lock.writeLock().unlock();
	}

	@Override
	public Map<Long, WritablePacket> removeFromBacklog(long timeBucket){
		lock.writeLock().lock();
		Map<Long, WritablePacket> m =unacknowledgedPackets.remove(timeBucket);
		if(m!=null)
			logger.debug("removed {}",m);	
		lock.writeLock().unlock();	
		return m;
	}

	
}
