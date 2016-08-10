package com.zafar.miniq.impl;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.zafar.miniq.DeepQ;
import com.zafar.miniq.WritablePacket;

@Service
public class DeepQImpl extends DeepQ{
	private static Logger logger = LoggerFactory.getLogger(DeepQImpl.class);

	@Override
	public WritablePacket read() {
		WritablePacket packet= queue.pollFirst();
		if(packet!=null){
			backlog.addToBacklog(packet);
			return (WritablePacket) packet;
		}
		else
			return null;
		
	}

	@Override
	public String write(String packet) {
		String uuid=generateUUID();
		if(queue.offer( new WritablePacket(packet, uuid)))
			return uuid;
		else
			return null;	
	}

	@Override
	public WritablePacket readWithBlocking() throws InterruptedException {
		WritablePacket packet= queue.take();
		if(packet!=null){
			backlog.addToBacklog(packet);
			return packet;
		}
		else return null;
	}

	public void processAck(String uuid) {
		long c=System.currentTimeMillis();
		logger.debug("time difference {}",c-getTimeStampFromId(uuid));
		if((c-getTimeStampFromId(uuid))<=(timeoutInSeconds.getTime()*1000))
			backlog.deleteFromBacklog(uuid);
		else
			logger.debug("late ack");
	}

	@Override
	public void pushToHead(Map<Long, WritablePacket> m) {
		for(WritablePacket packet:m.values())
			try {
				packet.setUuid(DeepQ.generateUUID());
				queue.putFirst(packet);
			} catch (InterruptedException e) {
				logger.debug("Exception",e);
			}
	}

}
