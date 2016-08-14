package com.zafar.deepq.impl;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zafar.deepq.CleanupThread;
import com.zafar.deepq.DeepQ;
import com.zafar.deepq.WritablePacket;

public class CleanupThreadImpl extends CleanupThread{

	private static Logger logger = LoggerFactory.getLogger(CleanupThreadImpl.class);

	public CleanupThreadImpl(BacklogImpl backlog, DeepQ miniQ) {
		super(backlog, miniQ);
	}

	@Override
	public void run() {
		logger.debug("started cleaning up");
		long currentTime=System.currentTimeMillis();
		long firstBucketOfExpiredPackets=BacklogImpl.calculateBucket(currentTime-(queue.timeoutInSeconds.getTime()*1000)-1000);
		//delete all packets from this bucket onwards till EPOCH
		for(long bucket=firstBucketOfExpiredPackets;bucket>DeepQImpl.EPOCH_TIME;bucket-=1000){
			logger.debug("cleaning bucket {}",bucket);
			Map<Long, WritablePacket> m=backlog.removeFromBacklog(bucket);
			if(m!=null)
				queue.pushToHead(m);
		}
	}

}
