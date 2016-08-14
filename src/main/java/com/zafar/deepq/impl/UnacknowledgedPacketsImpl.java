package com.zafar.deepq.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import rx.Observable;

import com.zafar.deepq.UnacknowledgedPackets;
import com.zafar.deepq.WritablePacket;
import com.zafar.deepq.util.Utilities;
import com.zafar.executors.ExecutorUtil;

@Service
public class UnacknowledgedPacketsImpl extends UnacknowledgedPackets{

	@Autowired 
	private DeepQImpl queue;
	
	private static Logger logger = LoggerFactory.getLogger(UnacknowledgedPacketsImpl.class);
	
	@Autowired
	private ExecutorUtil executorUtil;

	private ReentrantReadWriteLock lock= new ReentrantReadWriteLock();
	@Override
	public void acknowledgePacket(String messageId) {
		logger.debug("deleting from backlog {}",messageId);
		long timestamp=Utilities.getTimeStampFromId(messageId);	
		long bucket=calculateBucket(timestamp);
		lock.writeLock().lock();
		Map<Long, WritablePacket> packets=unacknowledgedPackets.get(bucket);
		if(packets!=null){
			packets.remove(timestamp);
			logger.debug("removed packet from backlog {}",timestamp);
		}
		lock.writeLock().unlock();
	}

	public static long calculateBucket(long timestamp){
		return (timestamp/1000)*1000;
	}
	@Override
	public void addToUnacknowldged(WritablePacket packet) {
		logger.debug("adding to backlog packet {}",packet);
		long timestamp=Utilities.getTimeStampFromId(packet.getUuid());
		long bucket=calculateBucket(timestamp);
		lock.writeLock().lock();
		Map<Long, WritablePacket> packets=unacknowledgedPackets.get(bucket);
		if(packets==null){
			logger.debug("creating bucket {}",bucket);
			ConcurrentHashMap<Long,WritablePacket> map=new ConcurrentHashMap<Long, WritablePacket>();
			map.put(timestamp, packet);			
			unacknowledgedPackets.put(bucket, map);
			Observable<Long> cleanup=Observable.just(bucket).subscribeOn(executorUtil.getCleanupExecutors());
			cleanup.delay(queue.expiryTime.getTimeInMs(), TimeUnit.MILLISECONDS).subscribe(
					(element) -> {
						logger.debug("removing expired bucket {}",element);
						Map<Long, WritablePacket> m=removeFromBacklog(element);
						if(m!=null){
							lock.writeLock().lock();
							queue.pushToHead(m);
							lock.writeLock().unlock();
						}
					}, 
					(exception) -> {
						logger.error("Some error:{}",exception);						
					}
			);			
		}
		else
			packets.put(timestamp, packet);
		lock.writeLock().unlock();
	}

	@Override
	public Map<Long, WritablePacket> removeFromBacklog(long timeBucket){
		Map<Long, WritablePacket> m =unacknowledgedPackets.remove(timeBucket);
		if(m!=null)
			logger.debug("removed {}",m);	
		return m;
	}

	
}
