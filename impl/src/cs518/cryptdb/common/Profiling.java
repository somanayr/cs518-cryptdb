package cs518.cryptdb.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class Profiling {
	
	private static HashMap<String, Long> timerStart = new HashMap<>();
	private static HashMap<String, Long> timerVal = new HashMap<>();
	
	public static HashMap<String, List<Long>> timerHistory = new HashMap<>();
	
	static {
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			
			@Override
			public void run() {
				for(String name : timerStart.keySet()) {
					System.err.println("WARNING: Shutting down running timer: " + name);
					if(timerHistory.containsKey(name))
						stopTimer(name);
					else
						pauseTimer(name);
				}
				System.out.println("\n--------Profiling information to follow--------");
				for(String name : timerVal.keySet()) {
					long len = timerVal.get(name);
					System.out.println(name + ": " + len);
				}
				for(String name : timerHistory.keySet()) {
					List<Long> hist = timerHistory.get(name);
					long avg = 0;
					long max = 0;
					long min = Long.MAX_VALUE;
					for (Long l : hist) {
						avg += l;
						max = Math.max(max, l);
						min = Math.min(min, l);
					}
					avg /= hist.size();
					System.out.printf("%s: avg=%d,max=%d,min=%d\n",name,avg,max,min );
				}
			}
		}));
	}
	
	
	public static void startTimer(String name) {
		if(!timerVal.containsKey(name)) {
			timerVal.put(name, 0L);
		}
		if(!timerStart.containsKey(name)) {
			timerStart.put(name, System.currentTimeMillis());
		}
	}
	
	public static void pauseTimer(String name) {
		long start = timerStart.remove(name);
		long elapsed = System.currentTimeMillis() - start;
		timerVal.put(name, timerVal.get(name) + elapsed);
	}
	
	public static void stopTimer(String name) {
		if(!timerHistory.containsKey(name)) {
			timerHistory.put(name, new ArrayList<>());
		}
		if(timerStart.containsKey(name)) pauseTimer(name);
		timerHistory.get(name).add(timerVal.remove(name));
	}
	
	public static long getTimerValue(String name) {
		if(timerStart.containsKey(name))
			throw new IllegalAccessError("Timer still running for " + name);
		return timerVal.remove(name);
	}
	
	public static List<Long> getTimerHistory(String name) {
		if(timerVal.containsKey(name))
			throw new IllegalAccessError("Timer still running or paused for " + name);
		return timerHistory.remove(name);
	}
}
