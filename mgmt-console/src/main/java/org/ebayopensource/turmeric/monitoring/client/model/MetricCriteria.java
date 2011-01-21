/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *******************************************************************************/

package org.ebayopensource.turmeric.monitoring.client.model;

import org.ebayopensource.turmeric.monitoring.client.model.MetricsQueryService.Ordering;
import org.ebayopensource.turmeric.monitoring.client.model.MetricsQueryService.Perspective;



/**
 * MetricCriteria
 *
 */
public class MetricCriteria {    
    final public long date1;
    final public long date2;
    final public int durationSec;
    final public int aggregationPeriod;
    final public Ordering sortOrder;
    final public int rows;
    final public String metricName;
    final public String roleType;
    final public String autoDelay;
    public static int maxAggregationPeriod = 86400;
    public static int medAggregationPeriod = 3600;
    public static int minAggregationPeriod = 60;
    

    public static MetricCriteria newMetricCriteria(String metricName,
                                                   final long date1,
                                                   final long date2,
                                                   final int durationHrs,
                                                   final Ordering ordering,
                                                   final int rows,
                                                   final Perspective perspective,
                                                   final boolean autoDelay) {
        MetricCriteria mc = new MetricCriteria(metricName, date1, date2, durationHrs, ordering, rows, perspective, autoDelay);
        return mc;
    }
    
    private MetricCriteria (String metricName,
                            long date1,
                            long date2,
                            int durationHrs,
                            Ordering ordering,
                            int rows,
                            Perspective perspective,
                            boolean autoDelay) {
        this.date1 = date1;
        this.date2 = date2;
        this.durationSec = (durationHrs * 60 * 60);
        this.aggregationPeriod = calculateAggregationPeriod(this.durationSec);
        this.sortOrder = ordering;
        this.rows = rows;
        this.metricName = metricName;
        
        if (perspective != null) {
            this.roleType = perspective.toString().toLowerCase();
        } else
            this.roleType = Perspective.Server.toString().toLowerCase();
        
        this.autoDelay = Boolean.toString(autoDelay);
    }
    
    private static int calculateAggregationPeriod (int durationSec) {
        if (durationSec > maxAggregationPeriod)  //if duration is over 1 day, use 24hr tables
            return maxAggregationPeriod;
        else if (durationSec > 7200)//if duration is more than 2 hrs, use hourly tables
            return medAggregationPeriod;
        else
            return minAggregationPeriod; //if duration is <=2hrs use the minute tables (see GlobalServiceConfig.xml
    }
    
  

    public String asRestUrl () {
        String url = "";

        url +="&ns:metricCriteria.ns:firstStartTime="+date1;
        url +="&ns:metricCriteria.ns:secondStartTime="+date2;
        url +="&ns:metricCriteria.ns:duration="+durationSec;
        url +="&ns:metricCriteria.ns:aggregationPeriod="+aggregationPeriod;

        if (sortOrder != null) {
            url +="&ns:metricCriteria.ns:sortOrder=";
            switch (sortOrder) {
                case Ascending: {
                    url+= "ascending";
                    break;
                }
                case Descending: {
                    url+= "descending";
                    break;
                }
            } 
        }
    
        url +="&ns:metricCriteria.ns:numRows="+rows;
        if (metricName != null)
            url +="&ns:metricCriteria.ns:metricName="+metricName;
        if (roleType != null)
            url +="&ns:metricCriteria.ns:roleType="+roleType;
        if (autoDelay != null)
            url +="&ns:metricCriteria.ns:autoDelay="+autoDelay;

        return url;
    }
}