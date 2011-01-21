/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *******************************************************************************/
package org.ebayopensource.turmeric.monitoring.client.model;

import java.util.List;

/**
 * MetricData
 * 
 * Results for metrics gathered.
 *
 */
public class MetricData {
    
    String restUrl;
   

    MetricCriteria metricCriteria;
    MetricResourceCriteria metricResourceCriteria;
    List<MetricGroupData> returnData;
    

    public MetricCriteria getMetricCriteria() {
        return metricCriteria;
    }

    public void setMetricCriteria(MetricCriteria metricCriteria) {
        this.metricCriteria = metricCriteria;
    }

    public MetricResourceCriteria getMetricResourceCriteria() {
        return metricResourceCriteria;
    }

    public void setMetricResourceCriteria(MetricResourceCriteria metricResourceCriteria) {
        this.metricResourceCriteria = metricResourceCriteria;
    }
	
	public List<MetricGroupData> getReturnData() {
        return returnData;
    }

    public void setReturnData(List<MetricGroupData> returnData) {
        this.returnData = returnData;
    }
    public String getRestUrl() {
        return restUrl;
    }

    public void setRestUrl(String restUrl) {
        this.restUrl = restUrl;
    }
}