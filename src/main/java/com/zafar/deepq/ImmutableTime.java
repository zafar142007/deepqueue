package com.zafar.deepq;

import java.util.concurrent.TimeUnit;

public final class ImmutableTime {
	private long time=0;
	private TimeUnit unit;
	public ImmutableTime(long time, TimeUnit timeUnit){
		this.time=time;
		this.unit=timeUnit;
	}
	public long getTimeInMs(){
		return TimeUnit.MILLISECONDS.convert(time, unit);
	}
}
