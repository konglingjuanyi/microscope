package com.vipshop.microscope.collector.analyzer;

import com.vipshop.microscope.report.ReportAnalyzer;
import com.vipshop.microscope.thrift.gen.Span;

/**
 * Analyze span and create report.
 * 
 * @author Xu Fei
 * @version 1.0
 */
public class MessageAnalyzer {
	
	/**
	 * The main analyze executor.
	 */
	private ReportAnalyzer reportAnalyzer = new ReportAnalyzer();
	
	public void analyze(Span span) {
		reportAnalyzer.analyze(span);
	}
}