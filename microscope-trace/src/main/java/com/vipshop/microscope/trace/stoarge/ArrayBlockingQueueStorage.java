package com.vipshop.microscope.trace.stoarge;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;
import com.vipshop.microscope.common.logentry.Codec;
import com.vipshop.microscope.common.logentry.LogEntry;
import com.vipshop.microscope.common.trace.Span;
import com.vipshop.microscope.trace.Tracer;

/**
 * Storge metrics in client use {@code ArrayBlockingQueue}.
 *  
 * @author Xu Fei
 * @version 1.0
 */
public class ArrayBlockingQueueStorage implements Storage {
	
	private static final Logger logger = LoggerFactory.getLogger(ArrayBlockingQueueStorage.class);
	
	@SuppressWarnings("rawtypes")
	private static final BlockingQueue queue = new ArrayBlockingQueue(Tracer.QUEUE_SIZE);
	
	/**
	 * Package access construct
	 */
	ArrayBlockingQueueStorage() {}

	/**
	 * Add span metrics.
	 * 
	 * @param span
	 */
	@SuppressWarnings("unchecked")
	public void addSpan(Span span) {
		boolean isFull = !queue.offer(span);
		
		if (isFull) {
			queue.clear();
			logger.info("client queue is full, clean queue now");
		}

	}
	
	/**
	 * Add exception metrics.
	 * 
	 */
	public void addException(HashMap<String, Object> map) {
		StringBuilder builder = new StringBuilder();
		builder.append("exception:");
		builder.append("stack=").append(Codec.encodeToString(map));
		add(builder.toString());
	}
	
	/**
	 * Add counter metrics.
	 * 
	 * @param counters
	 * @param date
	 */
	public void addCounter(SortedMap<String, Counter> counters, long date) {
		StringBuilder builder = new StringBuilder();
		builder.append("counter:");
		builder.append("date=").append(date).append(":");
		for (Entry<String, Counter> entry : counters.entrySet()) {
			builder.append(entry.getKey()).append("=");
			builder.append(entry.getValue().getCount()).append(":");
		}
		
		String counter = builder.toString();
		add(counter.substring(0, counter.length() - 1));
			   
	}
	
	/**
	 * Add guage metrics.
	 */
	@SuppressWarnings("rawtypes")
	public void addGauge(SortedMap<String, Gauge> gauges, long date) {
		StringBuilder builder = new StringBuilder();
		builder.append("gauge:");
		builder.append("date=").append(date).append(":");
		for (Entry<String, Gauge> entry : gauges.entrySet()) {
			builder.append(entry.getKey()).append("=");
			builder.append(entry.getValue().getValue()).append(":");
		}
		
		String gauge = builder.toString();
		add(gauge.substring(0, gauge.length() - 1));
			   
	}
	
	/**
	 * Add histogram metrics
	 */
	public void addHistogram(SortedMap<String, Histogram> histograms, long date) {
		StringBuilder builder = new StringBuilder();
		builder.append("histogram:");
		builder.append("date=").append(date).append(":");
		for (Map.Entry<String, Histogram> entry : histograms.entrySet()) {
			builder.append(entry.getKey()).append("=");
			
			Histogram histogram = entry.getValue();
			Snapshot snapshot = histogram.getSnapshot();
			StringBuilder hBuilder = new StringBuilder();
			hBuilder.append("count;").append(histogram.getCount()).append(";");
			hBuilder.append("min;").append(snapshot.getMin()).append(";");
			hBuilder.append("max;").append(snapshot.getMax()).append(";");
			hBuilder.append("mean;").append(snapshot.getMean()).append(";");
			hBuilder.append("stddev;").append(snapshot.getStdDev()).append(";");
			hBuilder.append("median;").append(snapshot.getMedian()).append(";");
			hBuilder.append("75%%;").append(snapshot.get75thPercentile()).append(";");
			hBuilder.append("95%%;").append(snapshot.get95thPercentile()).append(";");
			hBuilder.append("98%%;").append(snapshot.get98thPercentile()).append(";");
			hBuilder.append("99%%;").append(snapshot.get99thPercentile()).append(";");
			hBuilder.append("99.9%%;").append(snapshot.get999thPercentile()).append(";");
			builder.append(hBuilder.toString()).append(":");
		}
		
		String histogram = builder.toString();
		add(histogram.substring(0, histogram.length() - 1));
	}
	
	/**
	 * Add meter metrics.
	 */
	public void addMeter(SortedMap<String, Meter> meters, long date) {
		StringBuilder builder = new StringBuilder();
		builder.append("meter:");
		builder.append("date=").append(date).append(":");
		for (Map.Entry<String, Meter> entry : meters.entrySet()) {
			builder.append(entry.getKey()).append("=");
		}
		
		String meter = builder.toString();
		add(meter.substring(0, meter.length() - 1));
	}
	
	/**
	 * Add timer metrics
	 */
	public void addTimer(SortedMap<String, Timer> timers, long date) {
		StringBuilder builder = new StringBuilder();
		builder.append("timer:");
		
		String timer = builder.toString();
		add(timer.substring(0, timer.length() - 1));
	}
	
	/**
	 * Add string metrics.
	 * 
	 * @param msg
	 */
	@SuppressWarnings("unchecked")
	public void add(String msg) {
		boolean isFull = !queue.offer(msg);
		
		if (isFull) {
			queue.clear();
			logger.info("client queue is full, clean queue now");
		}

	}

	/**
	 * Add LogEntry to queue.
	 * 
	 * If client queue is full, empty queue.
	 * 
	 * @param span {@link Span}
	 */
	@Deprecated
	public void add(LogEntry logEntry) { 
		if (logEntry == null) {
			return;
		}
		
		@SuppressWarnings("unchecked")
		boolean isFull = !queue.offer(logEntry);
		
		if (isFull) {
			queue.clear();
			logger.info("client queue is full, clean queue now");
		}
	}
	
	/**
	 * Get logEntry from queue.
	 * 
	 * @return {@link Span}
	 */
	@Override
	public LogEntry poll() {
		Object object = queue.poll();
		
		if (object instanceof Span) {
			LogEntry logEntry = Codec.encodeToLogEntry((Span)object);
			return logEntry;
		}
		
		if (object instanceof String) {
			LogEntry logEntry = Codec.encodeToLogEntry((String)object);
			return logEntry;
		}
		
		return null;
	}
	
	/**
	 * Get size of queue.
	 */
	@Override
	public int size() {
		return queue.size();
	}


}