package com.zafar.miniq;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;

import com.zafar.miniq.impl.BacklogImpl;
import com.zafar.miniq.impl.CleanupThreadImpl;

public abstract class MiniQ {
	
	protected LinkedBlockingDeque<WritablePacket> queue= new LinkedBlockingDeque<WritablePacket>();//has unread messages
	
	public static final String ID_SEPARATOR="_";
	
	/**
	 * 	has those messages which are yet to be acknowledged
	 */
	protected BacklogImpl backlog=new BacklogImpl();

	private ScheduledThreadPoolExecutor cleaner = new ScheduledThreadPoolExecutor(2);
	public static ImmutableTime timeoutInSeconds;
	public static final long EPOCH_TIME=System.currentTimeMillis();
	
	@Value("${expiry.time.s}")
	private int timeout;
	
	private CleanupThread cleanerThread;
	
	@PostConstruct
	public void init(){
		cleanerThread=new CleanupThreadImpl(backlog, this);
		this.timeoutInSeconds=new ImmutableTime(timeout);
		cleaner.scheduleAtFixedRate(cleanerThread, 0, timeout, TimeUnit.SECONDS);
	}
	/**
	 * take the message out from the queue,
	 * push it into backlog and return it.
	 * If no messages are there, return null
	 * @return A
	 */
	public abstract WritablePacket read();
	
	/**
	 * take the message out from the queue,
	 * push it into backlog and return it.
	 * If no messages are there, block until one becomes available.
	 * @return A
	 * @throws InterruptedException 
	 */
	public abstract WritablePacket readWithBlocking() throws InterruptedException;
	/**
	 * calculate a UUID to give back to the writer, and push it into the queue from the front with the UUID 
	 * @param packet
	 * @return
	 */
	public abstract String write(String packet);
	/**
	 * delete from backlog this uuid as it has been now acknowledged by the reader
	 * @param uuid
	 */
	public abstract void processAck(String uuid);
	
	public abstract void pushToHead(Map<Long, WritablePacket> m);
	
	public static final String generateUUID(){
		UUID u=UUID.randomUUID();
		long id=u.getLeastSignificantBits();
		return System.currentTimeMillis()+ID_SEPARATOR+Long.toString(id);
	}
	public static final long getTimeStampFromId(String messageId){
		return Long.parseLong(messageId.substring(0,messageId.indexOf(ID_SEPARATOR)));
	}
	
}
