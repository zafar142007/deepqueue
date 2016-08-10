package com.zafar.miniq.impl;

import org.springframework.stereotype.Service;

import com.zafar.miniq.MiniQ;

@Service
public class MiniQImpl<A> extends MiniQ<A>{

	@Override
	public A read() {
		A packet=queue.pollFirst();
		if(packet!=null)
			backlog.addToBacklog(System.currentTimeMillis(), packet);
		return packet;
	}

	@Override
	public String write(A packet) {
		if(queue.offerFirst(packet))
			return generateRandomString();
		else
			return null;	
	}

	@Override
	public A readWithBlocking() throws InterruptedException {
		A packet=queue.take();
		if(packet!=null)
			backlog.addToBacklog(System.currentTimeMillis(), packet);
		return packet;
	}

	public void delete(String uuid) {
		backlog.deleteFromBacklog(uuid);
	}

}
