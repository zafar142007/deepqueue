package com.zafar.deepq.impl;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import rx.Observable;

import com.zafar.deepq.UnacknowledgedPackets;
import com.zafar.deepq.domain.WritablePacket;
import com.zafar.deepq.executors.ExecutorUtil;

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
		long timestamp=queue.getReadTimestamps().get(messageId);	//get read timestamp
		lock.writeLock().lock();
		unacknowledgedPackets.remove(messageId);
		queue.getReadTimestamps().remove(timestamp);
		logger.debug("removed packet from backlog {} wth read timestamp {}",messageId, timestamp);
		lock.writeLock().unlock();
	}

	@Override
	public void addToUnacknowldged(WritablePacket packet) {
		logger.debug("adding to backlog packet {}",packet);
		lock.writeLock().lock();
		WritablePacket p=unacknowledgedPackets.get(packet.getUuid());
		if(p==null){
			unacknowledgedPackets.put(packet.getUuid(), packet);
			Observable<WritablePacket> cleanup=Observable.just(packet).subscribeOn(executorUtil.getCleanupExecutors());
			cleanup.delay(queue.expiryTime.getTimeInMs(), TimeUnit.MILLISECONDS).subscribe(
					(element) -> {
						logger.debug("Time is up! Removing expired packet {}",element);
						WritablePacket m=unacknowledgedPackets.remove(element.getUuid());
						if(m!=null){
							lock.writeLock().lock();
							queue.pushToHead(m);
							lock.writeLock().unlock();
						}else
							logger.debug("not found {}",element);
					}, 
					(exception) -> {
						logger.error("Some error:{}",exception);						
					}
			);			
		}
		else
			logger.error("{} is already present!",packet);
		lock.writeLock().unlock();
	}

	
	
}
