package com.zafar.deepq;

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
	 * If no messages are there, block until one becomes available until default timeout.
	 * @return A
	 * @throws InterruptedException 
	 */
	public abstract DeferredResult<WritablePacket> readWithBlocking();
	/**
	 * calculate a UUID to give back to the writer, and push it into the queue at the tail with the UUID 
	 * @param packet
	 * @return
	 */
	public abstract DeferredResult<WritablePacket> write(String packet);
	/**
	 * delete from backlog this uuid as it has now been acknowledged by the reader
	 * @param uuid
	 */
	public abstract void processAck(String uuid);
	
	/**
	 * Push to the head of the queue this packet
	 * @param m
	 */
	public abstract void pushToHead(WritablePacket m);
	
}
