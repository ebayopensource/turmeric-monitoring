/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *******************************************************************************/
package org.ebayopensource.turmeric.monitoring.client;

import java.util.Map;

import com.google.gwt.i18n.client.Constants;

/**
 * The Interface ConsoleConstants.
 */
public interface ConsoleConstants extends Constants {
	
	/**
	 * Turmeric.
	 *
	 * @return the string
	 */
	@DefaultStringValue("Turmeric")
	public String turmeric();

	/**
	 * Title.
	 *
	 * @return the string
	 */
	@DefaultStringValue("Turmeric Management Console")
	public String title();

	/**
	 * Submit.
	 *
	 * @return the string
	 */
	@DefaultStringValue("Submit")
	public String submit();

	/**
	 * Apply.
	 *
	 * @return the string
	 */
	@DefaultStringValue("Apply")
	public String apply();

	/**
	 * Save.
	 *
	 * @return the string
	 */
	@DefaultStringValue("Save")
	public String save();

	/**
	 * Cancel.
	 *
	 * @return the string
	 */
	@DefaultStringValue("Cancel")
	public String cancel();

	/**
	 * Enter.
	 *
	 * @return the string
	 */
	@DefaultStringValue("Enter")
	public String enter();

	/**
	 * Clear.
	 *
	 * @return the string
	 */
	@DefaultStringValue("Clear")
	public String clear();

	/**
	 * Pools.
	 *
	 * @return the string
	 */
	@DefaultStringValue("Pools")
	public String pools();

	/**
	 * Top volume.
	 *
	 * @return the string
	 */
	@DefaultStringValue("Top Volume")
	public String topVolume();

	/**
	 * Least performance.
	 *
	 * @return the string
	 */
	@DefaultStringValue("Least Performance")
	public String leastPerformance();

	/**
	 * Call trend.
	 *
	 * @return the string
	 */
	@DefaultStringValue("Call Trend")
	public String callTrend();

	/**
	 * Performance trend.
	 *
	 * @return the string
	 */
	@DefaultStringValue("Performance Trend")
	public String performanceTrend();

	/**
	 * Top errors.
	 *
	 * @return the string
	 */
	@DefaultStringValue("Top Errors")
	public String topErrors();

	/**
	 * Consumer traffic.
	 *
	 * @return the string
	 */
	@DefaultStringValue("Consumer Traffic")
	public String consumerTraffic();

	/**
	 * Consumer errors.
	 *
	 * @return the string
	 */
	@DefaultStringValue("Consumer Errors")
	public String consumerErrors();

	/**
	 * Call volume.
	 *
	 * @return the string
	 */
	@DefaultStringValue("Call Volume")
	public String callVolume();

	/**
	 * Performance.
	 *
	 * @return the string
	 */
	@DefaultStringValue("Performance")
	public String performance();

	/**
	 * Top service errors.
	 *
	 * @return the string
	 */
	@DefaultStringValue("Top Service Errors")
	public String topServiceErrors();

	/**
	 * Top consumer errors.
	 *
	 * @return the string
	 */
	@DefaultStringValue("Top Consumer Errors")
	public String topConsumerErrors();

	/**
	 * Summary.
	 *
	 * @return the string
	 */
	@DefaultStringValue("Summary")
	public String summary();

	/**
	 * Services.
	 *
	 * @return the string
	 */
	@DefaultStringValue("Services")
	public String services();

	/**
	 * Service.
	 *
	 * @return the string
	 */
	@DefaultStringValue("Service")
	public String service();

	/**
	 * Operations.
	 *
	 * @return the string
	 */
	@DefaultStringValue("Operations")
	public String operations();

	/**
	 * Operation.
	 *
	 * @return the string
	 */
	@DefaultStringValue("Operation")
	public String operation();

	/**
	 * Errors.
	 *
	 * @return the string
	 */
	@DefaultStringValue("Errors")
	public String errors();

	/**
	 * Error.
	 *
	 * @return the string
	 */
	@DefaultStringValue("Error")
	public String error();

	/**
	 * Consumers.
	 *
	 * @return the string
	 */
	@DefaultStringValue("Consumers")
	public String consumers();

	/**
	 * Count.
	 *
	 * @return the string
	 */
	@DefaultStringValue("Count")
	public String count();

	/**
	 * Change.
	 *
	 * @return the string
	 */
	@DefaultStringValue("Change")
	public String change();

	/**
	 * Average.
	 *
	 * @return the string
	 */
	@DefaultStringValue("Average")
	public String average();

	/**
	 * Total services.
	 *
	 * @return the string
	 */
	@DefaultStringValue("Total Services")
	public String totalServices();

	/**
	 * View metrics for.
	 *
	 * @return the string
	 */
	@DefaultStringValue("View metrics for")
	public String viewMetricsFor();

	/**
	 * First date.
	 *
	 * @return the string
	 */
	@DefaultStringValue("From")
	public String firstDate();

	/**
	 * Second date.
	 *
	 * @return the string
	 */
	@DefaultStringValue("To")
	public String secondDate();

	/**
	 * And.
	 *
	 * @return the string
	 */
	@DefaultStringValue("and")
	public String and();

	/**
	 * Go.
	 *
	 * @return the string
	 */
	@DefaultStringValue("Go")
	public String go();

	/**
	 * At.
	 *
	 * @return the string
	 */
	@DefaultStringValue("at")
	public String at();

	/**
	 * Over.
	 *
	 * @return the string
	 */
	@DefaultStringValue("Over")
	public String over();

	/**
	 * Hr.
	 *
	 * @return the string
	 */
	@DefaultStringValue("hr")
	public String hr();

	/**
	 * Ok.
	 *
	 * @return the string
	 */
	@DefaultStringValue("Ok")
	public String ok();

	/**
	 * Download.
	 *
	 * @return the string
	 */
	@DefaultStringValue("Download")
	public String download();

	/**
	 * Results.
	 *
	 * @return the string
	 */
	@DefaultStringValue("Results")
	public String results();

	/**
	 * Server error.
	 *
	 * @return the string
	 */
	@DefaultStringValue("A server error has occurred")
	public String serverError();

	/**
	 * Select metrics.
	 *
	 * @return the string
	 */
	@DefaultStringValue("Select Metrics")
	public String selectMetrics();

	/**
	 * View errors by.
	 *
	 * @return the string
	 */
	@DefaultStringValue("View Errors By")
	public String viewErrorsBy();

	/**
	 * More.
	 *
	 * @return the string
	 */
	@DefaultStringValue("More")
	public String more();

	/**
	 * Italian.
	 *
	 * @return the string
	 */
	@DefaultStringValue("Italiano")
	public String italian();

	/**
	 * English.
	 *
	 * @return the string
	 */
	@DefaultStringValue("English")
	public String english();

	/**
	 * German.
	 *
	 * @return the string
	 */
	@DefaultStringValue("Deutsch")
	public String german();

	/**
	 * French.
	 *
	 * @return the string
	 */
	@DefaultStringValue("Fran\u00E7ais")
	public String french();

	/**
	 * Category.
	 *
	 * @return the string
	 */
	@DefaultStringValue("Category")
	public String category();

	/**
	 * Severity.
	 *
	 * @return the string
	 */
	@DefaultStringValue("Severity")
	public String severity();

	/**
	 * Top application errors.
	 *
	 * @return the string
	 */
	@DefaultStringValue("Top Application Errors")
	public String topApplicationErrors();

	/**
	 * Top request errors.
	 *
	 * @return the string
	 */
	@DefaultStringValue("Top Request Errors")
	public String topRequestErrors();

	/**
	 * Top system errors.
	 *
	 * @return the string
	 */
	@DefaultStringValue("Top System Errors")
	public String topSystemErrors();

	/**
	 * Top criticals.
	 *
	 * @return the string
	 */
	@DefaultStringValue("Top Critical Errors")
	public String topCriticals();

	/**
	 * Top warnings.
	 *
	 * @return the string
	 */
	@DefaultStringValue("Top Warnings")
	public String topWarnings();

	/**
	 * Consumer error.
	 *
	 * @return the string
	 */
	@DefaultStringValue("Consumer Error")
	public String consumerError();

	/**
	 * Top severity errors.
	 *
	 * @return the string
	 */
	@DefaultStringValue("Top Severity Errors")
	public String topSeverityErrors();

	/**
	 * Top category errors.
	 *
	 * @return the string
	 */
	@DefaultStringValue("Top Category Errors")
	public String topCategoryErrors();

	/**
	 * Errors to calls.
	 *
	 * @return the string
	 */
	@DefaultStringValue("Errors to Calls")
	public String errorsToCalls();

	/**
	 * Name.
	 *
	 * @return the string
	 */
	@DefaultStringValue("Name")
	public String name();

	/**
	 * Domain.
	 *
	 * @return the string
	 */
	@DefaultStringValue("Domain")
	public String domain();

	/**
	 * Subdomain.
	 *
	 * @return the string
	 */
	@DefaultStringValue("Subdomain")
	public String subdomain();

	/**
	 * Select filter criteria.
	 *
	 * @return the string
	 */
	@DefaultStringValue("Select Filter Criteria")
	public String selectFilterCriteria();

	/**
	 * Information.
	 *
	 * @return the string
	 */
	@DefaultStringValue("Information")
	public String information();

	/**
	 * Creates the.
	 *
	 * @return the string
	 */
	@DefaultStringValue("Create")
	public String create();

	/**
	 * Delete.
	 *
	 * @return the string
	 */
	@DefaultStringValue("Delete")
	public String delete();

	/**
	 * Metric name map.
	 *
	 * @return the map
	 */
	public Map metricNameMap();

	/**
	 * Error view type map.
	 *
	 * @return the map
	 */
	public Map errorViewTypeMap();

	/**
	 * Metrics monitoring.
	 *
	 * @return the string
	 */
	@DefaultStringValue("Metrics Monitoring")
	public String metricsMonitoring();

	/**
	 * Unknown.
	 *
	 * @return the string
	 */
	@DefaultStringValue("Unknown")
	public String unknown();
	
	/**
	 * Confirm.
	 *
	 * @return the string
	 */
	@DefaultStringValue("Confirm")
	public String confirm();
	
	/*
	 * Metric Definitions
	 */
	public Map metricDefNamesMap();
}
