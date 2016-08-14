package com.zafar.deepq;

import java.util.Map;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.context.request.async.DeferredResult;

public abstract class DeepQ {
	
	protected LinkedBlockingDeque<WritablePacket> queue= new LinkedBlockingDeque<WritablePacket>();//has unread messages
	
	public ImmutableTime expiryTime;
	
	@Value("${expiry.time.s}")
	private int timeout;
		
	@PostConstruct
	public void init(){
		this.expiryTime=new ImmutableTime(timeout, TimeUnit.SECONDS);
	}
	/**
	 * take the message out from the queue,
	 * push it into backlog and return it.
	 * If no messages are there, return null
	 * @return A
	 */
	public abstract DeferredResult<WritablePacket> read();
	
	/**
	 * take the message out from the queue,
	 * push it into backlog and return it.
	 * If no messages are there, block until one becomes available.
	 * @return A
	 * @throws InterruptedException 
	 */
	public abstract DeferredResult<WritablePacket> readWithBlocking();
	/**
	 * calculate a UUID to give back to the writer, and push it into the queue from the front with the UUID 
	 * @param packet
	 * @return
	 */
	public abstract DeferredResult<WritablePacket> write(String packet);
	/**
	 * delete from backlog this uuid as it has been now acknowledged by the reader
	 * @param uuid
	 */
	public abstract void processAck(String uuid);
	
	/**
	 * Push to the front of the queue all entries of this bucket in the order of their timestamps
	 * ie the newest packet in this bucket should be at the front in the end (LIFO). This is done because 
	 * individual buckets will get expired in this fashion. Bucket A with lesser timestamp will always get expired before
	 * bucket B with a higher timestamp, and thus packets of B will be ahead of packets of A in the main queue (LIFO).
	 * So, inside a bucket also, the same order should be followed.  
	 * @param m
	 */
	public abstract void pushToHead(Map<Long, WritablePacket> m);
	
}
