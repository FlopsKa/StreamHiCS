package environment;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;

public class Stopwatch {

	HashMap<String, NamedWatch> watches = new HashMap<String, NamedWatch>();

	public void start(String name) {
		if (!watches.containsKey(name)) {
			watches.put(name, new NamedWatch(name));
		}
		watches.get(name).start();
	}

	public void stop(String name) {
		if (watches.containsKey(name)) {
			watches.get(name).stop();
		}
	}

	@Override
	public String toString() {
		String res = "";

		for(NamedWatch w : watches.values()){
			res += w.toString() + "; ";
		}
		
		return res;
	}

	private class NamedWatch {
		private String name;
		private Duration totalTime;
		private Instant beginning;
		private Instant end;
		private boolean running = false;

		private NamedWatch(String name){
			this.name = name;
		}
		
		private String getName() {
			return name;
		}

		private double getTotalTime() {
			return totalTime.getSeconds() + ((double) totalTime.getNano())/1000000000;
		}

		private void start() {
			beginning = Instant.now();
			running = true;
		}

		private void stop() {
			if (running) {
				end = Instant.now();
				Duration d = Duration.between(beginning, end);
				if(totalTime == null){
					totalTime = d;
				}else{
					totalTime = totalTime.plus(d);
				}
				running = false;
			}
		}

		@Override
		public String toString() {
			return name + ": " + (totalTime.getSeconds() + ((double) totalTime.getNano())/1000000000) + "s";
		}
	}
}
