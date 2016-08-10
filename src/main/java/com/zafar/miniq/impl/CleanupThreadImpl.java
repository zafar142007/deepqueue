package com.zafar.miniq.impl;

import com.zafar.miniq.Backlog;
import com.zafar.miniq.CleanupThread;
import com.zafar.miniq.MiniQ;

public class CleanupThreadImpl<A> extends CleanupThread<A>{

	public CleanupThreadImpl(Backlog<A> backlog, MiniQ<A> miniQ) {
		super(backlog, miniQ);
	}

	@Override
	public void run() {
		long currentTime=System.currentTimeMillis();
		long firstBucketOfExpiredPackets=BacklogImpl.calculateBucket(currentTime-queue.timeoutInSeconds.getTime()-1000);
		//delete all packets from this bucket onwards till EPOCH
		for(long bucket=firstBucketOfExpiredPackets;bucket>MiniQImpl.EPOCH_TIME;bucket-=1000){
			backlog.deleteFromBacklog(bucket);
		}
	}

}
