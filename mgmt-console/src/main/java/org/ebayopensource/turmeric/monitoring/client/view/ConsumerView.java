/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *******************************************************************************/
package org.ebayopensource.turmeric.monitoring.client.view;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ebayopensource.turmeric.monitoring.client.ConsoleUtil;
import org.ebayopensource.turmeric.monitoring.client.Dashboard;
import org.ebayopensource.turmeric.monitoring.client.model.ConsumerMetric;
import org.ebayopensource.turmeric.monitoring.client.model.Filterable;
import org.ebayopensource.turmeric.monitoring.client.model.MetricData;
import org.ebayopensource.turmeric.monitoring.client.model.MetricGroupData;
import org.ebayopensource.turmeric.monitoring.client.model.MetricsQueryService.Entity;
import org.ebayopensource.turmeric.monitoring.client.model.ObjectType;
import org.ebayopensource.turmeric.monitoring.client.model.ResourceEntityRequest;
import org.ebayopensource.turmeric.monitoring.client.model.TimeSlotData;
import org.ebayopensource.turmeric.monitoring.client.presenter.ConsumerPresenter;
import org.ebayopensource.turmeric.monitoring.client.view.graph.ColumnChartGraphRenderer;
import org.ebayopensource.turmeric.monitoring.client.view.graph.GraphRenderer;
import org.ebayopensource.turmeric.monitoring.client.view.graph.LineChartGraphRenderer;
import org.ebayopensource.turmeric.monitoring.client.view.graph.SumGraphDataAggregator;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;

/**
 * ConsumerView.
 * 
 * Depends on the context that the tab is accessed with:
 * 
 * <ol>
 * <li>a Service only selected
 * <li>a Service + Operation selected
 * <li>a Service + Operation + Consumer selected
 * </ol>
 * 
 * There are 2 different views:
 * 
 * <ol>
 * <li>Initial view: (graphs) Call Volume, Least(?) Performance, Errors (Consumer Errors or Top Errors?)
 * <li>Drilldown view (requires selection of a consumer): (tables) same as Service tab, but oriented to single consumer
 * </ol>
 * .
 */
public class ConsumerView extends Composite implements ConsumerPresenter.Display {

    private String id;
    private Label summaryHeading;
    private FilterWidget filterWidget;
    private SplitLayoutPanel splitPanel;
    private ServiceListWidget serviceListWidget;
    private Dashboard dashboard;
    private SummaryPanel callVolumePanel;
    private FlexTable callVolumeTable;
    private SummaryPanel performancePanel;
    private FlexTable performanceTable;
    private SummaryPanel errorsPanel;
    private FlexTable errorsTable;
    private SummaryPanel topVolumePanel;
    private FlexTable topVolumeTable;
    private SummaryPanel leastPerformancePanel;
    private FlexTable leastPerformanceTable;
    private SummaryPanel topServiceErrorsPanel;
    private FlexTable topServiceErrorsTable;
    private SummaryPanel topConsumerErrorsPanel;
    private FlexTable topConsumerErrorsTable;
    private Button filterButton;
    private DialogBox filterDialog;

    /**
     * Instantiates a new consumer view.
     * 
     * @param dashboard
     *            the dashboard
     */
    public ConsumerView(Dashboard dashboard) {
        // make the panel for the contents of the tab
        DockLayoutPanel contentPanel = new DockLayoutPanel(Unit.EM);
        initWidget(contentPanel);

        // heading
        Panel topPanel = new FlowPanel();
        topPanel.addStyleName("summary-heading-panel");
        Grid g = new Grid(1, 2);
        g.setHeight("100%");
        g.setWidth("100%");
        summaryHeading = new Label("&nbsp");
        summaryHeading.setWidth("50em");
        g.setWidget(0, 0, summaryHeading);
        g.getCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_MIDDLE);
        topPanel.add(g);

        // filters: dates, times and metrics
        filterWidget = new FilterWidget();
        filterWidget.setDateFormat("dd MMM yyyy");

        filterButton = new Button("Filter Criteria >>");
        filterButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                pickFilter();
            }
        });

        g.setWidget(0, 1, filterButton);
        g.getCellFormatter().setVerticalAlignment(0, 1, HasVerticalAlignment.ALIGN_MIDDLE);

        filterDialog = new DialogBox(true);
        filterDialog.setText("Select Filter Criteria");
        FlowPanel contents = new FlowPanel();
        filterDialog.setWidget(contents);
        contents.add(filterWidget);

        filterWidget.getApplyButton().addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                filterDialog.hide(true);
            }
        });

        filterWidget.getCancelButton().addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                filterDialog.hide(true);
            }
        });

        splitPanel = new SplitLayoutPanel();
        contentPanel.addNorth(topPanel, 2.5);
        contentPanel.add(splitPanel);

        // scrolling panel for right hand side
        ScrollPanel rhs = new ScrollPanel();
        rhs.setAlwaysShowScrollBars(true);
        rhs.addStyleName("summary-panel");

        FlowPanel panel = new FlowPanel();

        // Tables (which will be graphs in future releases) for when no Consumer
        // is selected:
        // Call Volume (isn't this just the same as Consumer Traffic from
        // service tab? CallCount for Service(/Operation)
        // by Consumer?)
        callVolumeTable = makeTable();
        callVolumePanel = makePanel(ConsoleUtil.constants.callVolume(), callVolumeTable);
        panel.add(callVolumePanel);
        hide(callVolumePanel);

        // Performance (analogous to Least Performance for Service tab, but
        // grouped by Consumer)
        performanceTable = makeTable();
        performancePanel = makePanel(ConsoleUtil.constants.performance(), performanceTable);
        panel.add(performancePanel);
        hide(performancePanel);

        // Errors (isn't this the same as Consumer Errors from service tab?
        // Errors for Service(/Operation) by Consumer?
        errorsTable = makeTable();
        errorsPanel = makePanel(ConsoleUtil.constants.errors(), errorsTable);
        panel.add(errorsPanel);
        hide(errorsPanel);

        // Tables for when a Consumer has been selected:
        // Top Volume (CallCount for Consumer by Service)
        topVolumeTable = makeTable();
        topVolumePanel = makePanel(ConsoleUtil.constants.topVolume(), topVolumeTable);
        panel.add(topVolumePanel);
        hide(topVolumePanel);

        // Least Performance (ResponseTime for Consumer by Service)
        leastPerformanceTable = makeTable();
        leastPerformancePanel = makePanel(ConsoleUtil.constants.leastPerformance(), leastPerformanceTable);
        panel.add(leastPerformancePanel);
        hide(leastPerformancePanel);

        // Top Errors (Errors for Consumer by Service(NOTE: 5 columns: service +
        // error is returned!)
        topServiceErrorsTable = makeTable();
        topServiceErrorsPanel = makePanel(ConsoleUtil.constants.topServiceErrors(), topServiceErrorsTable);
        panel.add(topServiceErrorsPanel);
        hide(topServiceErrorsPanel);

        // Consumer Errors (Errors for Consumer by ...?)
        topConsumerErrorsTable = makeTable();
        topConsumerErrorsPanel = makePanel(ConsoleUtil.constants.topConsumerErrors(), topConsumerErrorsTable);
        panel.add(topConsumerErrorsPanel);
        hide(topConsumerErrorsPanel);

        rhs.add(panel);

        serviceListWidget = new ServiceListWidget();

        splitPanel.addWest(serviceListWidget, 200);
        splitPanel.add(rhs);

        this.dashboard = dashboard;
        this.dashboard.addView(this, ConsoleUtil.constants.consumers());
    }

    /**
     * As widget.
     * 
     * @return the widget
     * @see com.google.gwt.user.client.ui.Widget#asWidget()
     */
    public Widget asWidget() {
        return this;
    }

    /**
     * View about to be shown, do any clean up necessary.
     * 
     * @see org.ebayopensource.turmeric.monitoring.client.Display#activate()
     */
    public void activate() {
        dashboard.activate(this);
    }

    /**
     * Sets the associated id.
     * 
     * @param id
     *            the new associated id
     * @see org.ebayopensource.turmeric.monitoring.client.Display#setAssociatedId(java.lang.String)
     */
    public void setAssociatedId(String id) {
        this.id = id;
    }

    /**
     * Gets the associated id.
     * 
     * @return the associated id
     * @see org.ebayopensource.turmeric.monitoring.client.Display#getAssociatedId()
     */
    public String getAssociatedId() {
        return this.id;
    }

    /**
     * Sets the selection.
     * 
     * @param selections
     *            the selections
     * @see org.ebayopensource.turmeric.monitoring.client.presenter.ConsumerPresenter.Display#setSelection(java.util.Map)
     */
    public void setSelection(Map<ObjectType, String> selections) {
        String s = "";
        if (selections != null) {
            if (selections.get(ObjectType.ServiceName) != null)
                s = s + " " + selections.get(ObjectType.ServiceName);
            if (selections.get(ObjectType.OperationName) != null)
                s = s + " : " + selections.get(ObjectType.OperationName);
            if (selections.get(ObjectType.ConsumerName) != null)
                s = s + " : " + selections.get(ObjectType.ConsumerName);

            serviceListWidget.setSelection(selections.get(ObjectType.ServiceName),
                            selections.get(ObjectType.OperationName));
        }
        else
            serviceListWidget.setSelection(null, null);

        if ("".equals(s))
            s = ConsoleUtil.constants.summary();
        summaryHeading.setText(s);
    }

    /**
     * Gets the selector.
     * 
     * @return the selector
     * @see org.ebayopensource.turmeric.monitoring.client.presenter.ConsumerPresenter.Display#getSelector()
     */
    public HasSelectionHandlers<TreeItem> getSelector() {
        return serviceListWidget.getServiceTree();
    }

    /**
     * Sets the services map.
     * 
     * @param map
     *            the map
     * @see org.ebayopensource.turmeric.monitoring.client.presenter.ConsumerPresenter.Display#setServicesMap(java.util.Map)
     */
    public void setServicesMap(Map<String, Set<String>> map) {
        serviceListWidget.setServicesMap(map);
    }

    /**
     * Error.
     * 
     * @param msg
     *            the msg
     * @see org.ebayopensource.turmeric.monitoring.client.presenter.ConsumerPresenter.Display#error(java.lang.String)
     */
    public void error(String msg) {
        ErrorDialog dialog = new ErrorDialog(true);
        dialog.setMessage(msg);
        dialog.getDialog().center();
        dialog.show();
    }

    /**
     * Hide.
     * 
     * @param o
     *            the o
     */
    public void hide(UIObject o) {
        o.setVisible(false);
    }

    /**
     * Show.
     * 
     * @param o
     *            the o
     */
    public void show(UIObject o) {
        o.setVisible(true);
    }

    /**
     * Gets the filter.
     * 
     * @return the filter
     * @see org.ebayopensource.turmeric.monitoring.client.presenter.ConsumerPresenter.Display#getFilter()
     */
    public Filterable getFilter() {
        return filterWidget;
    }

    /**
     * Sets the download url.
     * 
     * @param m
     *            the m
     * @param url
     *            the url
     * @see org.ebayopensource.turmeric.monitoring.client.presenter.ConsumerPresenter.Display#setDownloadUrl(org.ebayopensource.turmeric.monitoring.client.model.ConsumerMetric,
     *      java.lang.String)
     */
    public void setDownloadUrl(ConsumerMetric m, String url) {
        SummaryPanel panel = null;
        switch (m) {
            case CallVolume: {
                panel = callVolumePanel;
                break;
            }
            case Performance: {
                panel = performancePanel;
                break;
            }
            case Errors: {
                panel = errorsPanel;
                break;
            }
            case TopVolume: {
                panel = topVolumePanel;
                break;
            }
            case LeastPerformance: {
                panel = leastPerformancePanel;
                break;
            }
            case TopServiceErrors: {
                panel = topServiceErrorsPanel;
                break;
            }
            case TopConsumerErrors: {
                panel = topConsumerErrorsPanel;
                break;
            }
        }
        if (panel != null)
            panel.setDownloadUrl(url);
    }

    /**
     * Sets the metric.
     * 
     * @param m
     *            the m
     * @param result
     *            the result
     * @see org.ebayopensource.turmeric.monitoring.client.presenter.ConsumerPresenter.Display#setMetric(org.ebayopensource.turmeric.monitoring.client.model.ConsumerMetric,
     *      org.ebayopensource.turmeric.monitoring.client.model.MetricData)
     */
    public void setMetric(ConsumerMetric m, MetricData result) {

        // Fill in the appropriate table with the results
        SummaryPanel panel = null;
        String date1Header = "";
        String date2Header = "";

        if (result != null) {
            String d1 = ConsoleUtil.timeFormat.format(new Date(result.getMetricCriteria().date1));
            String d2 = ConsoleUtil.timeFormat.format(new Date(result.getMetricCriteria().date2));
            date1Header = d1 + " + " + (result.getMetricCriteria().durationSec / (60 * 60))
                            + ConsoleUtil.constants.hr();
            date2Header = d2 + " + " + (result.getMetricCriteria().durationSec / (60 * 60))
                            + ConsoleUtil.constants.hr();
        }

        switch (m) {
            /* Start tables for all consumers */
            case CallVolume: {
                String[] columns = {
                        ConsoleUtil.constants.consumers(),
                        (result == null ? ConsoleUtil.constants.count() : date1Header + " "
                                        + ConsoleUtil.constants.count()),
                        (result == null ? ConsoleUtil.constants.count() : date2Header + " "
                                        + ConsoleUtil.constants.count()), "% " + ConsoleUtil.constants.change() };

                List<Widget[]> rows = new ArrayList<Widget[]>();
                if (result != null) {
                    for (int i = 0; i < result.getReturnData().size(); i++) {
                        MetricGroupData rd = result.getReturnData().get(i);
                        Widget[] rowData = new Widget[4];
                        rowData[0] = new Label(rd.getCriteriaInfo().getConsumerName());
                        rowData[1] = new Label(NumberFormat.getDecimalFormat().format(
                                        Double.parseDouble(rd.getCount1())));
                        rowData[2] = new Label(NumberFormat.getDecimalFormat().format(
                                        Double.parseDouble(rd.getCount2())));
                        rowData[3] = new Label(rd.getDiff());
                        rows.add(rowData);
                    }
                }

                setTabularData(callVolumeTable, columns, rows);
                for (int i = 1; i < callVolumeTable.getRowCount(); i++) {
                    Widget w = callVolumeTable.getWidget(i, 0);
                    w.addStyleName("clickable");
                }
                panel = callVolumePanel;
                break;
            }
            case Performance: {
                String[] columns = {
                        ConsoleUtil.constants.consumers(),
                        (result == null ? ConsoleUtil.constants.average() : date1Header + " "
                                        + ConsoleUtil.constants.average() + " \u00B5s"),
                        (result == null ? ConsoleUtil.constants.average() : date2Header + " "
                                        + ConsoleUtil.constants.average() + " \u00B5s"),
                        "% " + ConsoleUtil.constants.change() };
                List<Widget[]> rows = new ArrayList<Widget[]>();
                if (result != null) {
                    for (int i = 0; i < result.getReturnData().size(); i++) {
                        MetricGroupData rd = result.getReturnData().get(i);
                        Widget[] rowData = new Widget[4];
                        rowData[0] = new Label(rd.getCriteriaInfo().getConsumerName());
                        rowData[1] = new Label(NumberFormat.getDecimalFormat().format(
                                        Double.parseDouble(rd.getCount1())));
                        rowData[2] = new Label(NumberFormat.getDecimalFormat().format(
                                        Double.parseDouble(rd.getCount2())));
                        rowData[3] = new Label(rd.getDiff());
                        rows.add(rowData);
                    }
                }

                setTabularData(performanceTable, columns, rows);
                for (int i = 1; i < performanceTable.getRowCount(); i++) {
                    Widget w = performanceTable.getWidget(i, 0);
                    w.addStyleName("clickable");
                }
                panel = performancePanel;
                break;
            }
            case Errors: {
                String[] columns = {
                        ConsoleUtil.constants.consumers(),
                        (result == null ? ConsoleUtil.constants.count() : date1Header + " "
                                        + ConsoleUtil.constants.count()),
                        (result == null ? ConsoleUtil.constants.count() : date2Header + " "
                                        + ConsoleUtil.constants.count()), "% " + ConsoleUtil.constants.change() };

                List<Widget[]> rows = new ArrayList<Widget[]>();
                if (result != null) {
                    for (int i = 0; i < result.getReturnData().size(); i++) {
                        MetricGroupData rd = result.getReturnData().get(i);
                        Widget[] rowData = new Widget[4];
                        rowData[0] = new Label(rd.getCriteriaInfo().getConsumerName());
                        rowData[1] = new Label(NumberFormat.getDecimalFormat().format(
                                        Double.parseDouble(rd.getCount1())));
                        rowData[2] = new Label(NumberFormat.getDecimalFormat().format(
                                        Double.parseDouble(rd.getCount2())));
                        rowData[3] = new Label(rd.getDiff());
                        rows.add(rowData);
                    }
                }

                setTabularData(errorsTable, columns, rows);
                for (int i = 1; i < errorsTable.getRowCount(); i++) {
                    Widget w = errorsTable.getWidget(i, 0);
                    w.addStyleName("clickable");
                }
                panel = errorsPanel;
                break;
            }
                /* End tables for all consumers */
                /* Start tables for a particular consumer */
            case TopVolume: {
                // column 0 will either be Services or Operations of a Service
                String col0 = ConsoleUtil.constants.services();
                if (result != null
                                && result.getMetricResourceCriteria().resourceEntityResponseType
                                                .equals(Entity.Operation))
                    col0 = ConsoleUtil.constants.operations();
                String[] columns = {
                        col0,
                        (result == null ? ConsoleUtil.constants.count() : date1Header + " "
                                        + ConsoleUtil.constants.count()),
                        (result == null ? ConsoleUtil.constants.count() : date2Header + " "
                                        + ConsoleUtil.constants.count()), "% " + ConsoleUtil.constants.change() };

                List<Widget[]> rows = new ArrayList<Widget[]>();
                if (result != null) {
                    for (int i = 0; i < result.getReturnData().size(); i++) {
                        MetricGroupData rd = result.getReturnData().get(i);
                        Widget[] rowData = new Widget[4];
                        if (result.getMetricResourceCriteria().resourceEntityResponseType.equals(Entity.Service))
                            rowData[0] = new Label(rd.getCriteriaInfo().getServiceName());
                        else if (result.getMetricResourceCriteria().resourceEntityResponseType.equals(Entity.Operation))
                            rowData[0] = new Label(rd.getCriteriaInfo().getOperationName());

                        rowData[1] = new Label(NumberFormat.getDecimalFormat().format(
                                        Double.parseDouble(rd.getCount1())));
                        rowData[2] = new Label(NumberFormat.getDecimalFormat().format(
                                        Double.parseDouble(rd.getCount2())));
                        rowData[3] = new Label(rd.getDiff());
                        rows.add(rowData);
                    }
                }

                setTabularData(topVolumeTable, columns, rows);
                // style the column with the names of the consumers in it
                for (int i = 1; i < topVolumeTable.getRowCount(); i++) {
                    Widget w = topVolumeTable.getWidget(i, 0);
                    w.addStyleName("clickable");
                }
                panel = topVolumePanel;
                break;
            }
            case LeastPerformance: {
                // column 0 will either be Services or Operations of a Service
                String col0 = ConsoleUtil.constants.services();
                if (result != null
                                && result.getMetricResourceCriteria().resourceEntityResponseType
                                                .equals(Entity.Operation))
                    col0 = ConsoleUtil.constants.operations();
                String[] columns = {
                        col0,
                        (result == null ? ConsoleUtil.constants.average() : date1Header + " "
                                        + ConsoleUtil.constants.average() + " \u00B5s"),
                        (result == null ? ConsoleUtil.constants.average() : date2Header + " "
                                        + ConsoleUtil.constants.average() + " \u00B5s"),
                        "% " + ConsoleUtil.constants.change() };
                List<Widget[]> rows = new ArrayList<Widget[]>();
                if (result != null) {
                    for (int i = 0; i < result.getReturnData().size(); i++) {
                        MetricGroupData rd = result.getReturnData().get(i);
                        Widget[] rowData = new Widget[4];
                        if (result.getMetricResourceCriteria().resourceEntityResponseType.equals(Entity.Service))
                            rowData[0] = new Label(rd.getCriteriaInfo().getServiceName());
                        else if (result.getMetricResourceCriteria().resourceEntityResponseType.equals(Entity.Operation))
                            rowData[0] = new Label(rd.getCriteriaInfo().getOperationName());

                        rowData[1] = new Label(NumberFormat.getDecimalFormat().format(
                                        Double.parseDouble(rd.getCount1())));
                        rowData[2] = new Label(NumberFormat.getDecimalFormat().format(
                                        Double.parseDouble(rd.getCount2())));
                        rowData[3] = new Label(rd.getDiff());
                        rows.add(rowData);
                    }
                }

                setTabularData(leastPerformanceTable, columns, rows);
                // style the column with the names of the consumers in it
                for (int i = 1; i < leastPerformanceTable.getRowCount(); i++) {
                    Widget w = leastPerformanceTable.getWidget(i, 0);
                    w.addStyleName("clickable");
                }
                panel = leastPerformancePanel;
                break;
            }
            case TopServiceErrors: {
                boolean isOperation = false;
                if (result != null && result.getMetricResourceCriteria() != null
                                && result.getMetricResourceCriteria().resourceEntityRequests != null) {
                    for (ResourceEntityRequest r : result.getMetricResourceCriteria().resourceEntityRequests) {
                        if (r.resourceEntityType == Entity.Operation)
                            isOperation = true;
                    }
                }
                String col0 = ConsoleUtil.constants.services();
                if (isOperation)
                    col0 = ConsoleUtil.constants.operations();
                String[] columns = {
                        col0,
                        ConsoleUtil.constants.errors(),
                        (result == null ? ConsoleUtil.constants.count() : date1Header + " "
                                        + ConsoleUtil.constants.count()),
                        (result == null ? ConsoleUtil.constants.count() : date2Header + " "
                                        + ConsoleUtil.constants.count()), "% " + ConsoleUtil.constants.change() };
                List<Widget[]> rows = new ArrayList<Widget[]>();
                if (result != null) {
                    for (int i = 0; i < result.getReturnData().size(); i++) {
                        MetricGroupData rd = result.getReturnData().get(i);

                        Widget[] rowData = new Widget[5];

                        if (isOperation) {
                            rowData[0] = new Label(rd.getCriteriaInfo().getOperationName());
                        }  else {
                            rowData[0] = new Label(rd.getCriteriaInfo().getServiceName());
                        }
                        String label = "undefined";
                        label = (String) ConsoleUtil.constants.metricDefNamesMap().get(rd.getCriteriaInfo().getMetricName());
                        rowData[1] = new Label(label);
                        
                        rowData[2] = new Label(NumberFormat.getDecimalFormat().format(
                                        Double.parseDouble(rd.getCount1())));
                        rowData[3] = new Label(NumberFormat.getDecimalFormat().format(
                                        Double.parseDouble(rd.getCount2())));
                        rowData[4] = new Label(rd.getDiff());
                        rows.add(rowData);
                    }
                }

                setTabularData(topServiceErrorsTable, columns, rows);
                // style the column with the names of the consumers in it
                for (int i = 1; i < topServiceErrorsTable.getRowCount(); i++) {
                    Widget w = topServiceErrorsTable.getWidget(i, 0);
                    w.addStyleName("clickable");
                }
                panel = topServiceErrorsPanel;
                break;
            }
            case TopConsumerErrors: {
                String[] columns = {
                        ConsoleUtil.constants.errors(),
                        (result == null ? ConsoleUtil.constants.count() : date1Header + " "
                                        + ConsoleUtil.constants.count()),
                        (result == null ? ConsoleUtil.constants.count() : date2Header + " "
                                        + ConsoleUtil.constants.count()), "% " + ConsoleUtil.constants.change() };

                List<Widget[]> rows = new ArrayList<Widget[]>();
                if (result != null) {
                    for (int i = 0; i < result.getReturnData().size(); i++) {
                        MetricGroupData rd = result.getReturnData().get(i);

                        Widget[] rowData = new Widget[4];
                        String label = "undefined";
                        label = (String) ConsoleUtil.constants.metricDefNamesMap().get(rd.getCriteriaInfo().getMetricName());
                        
                        rowData[0] = new Label(label);
                        rowData[1] = new Label(NumberFormat.getDecimalFormat().format(
                                        Double.parseDouble(rd.getCount1())));
                        rowData[2] = new Label(NumberFormat.getDecimalFormat().format(
                                        Double.parseDouble(rd.getCount2())));
                        rowData[3] = new Label(rd.getDiff());
                        rows.add(rowData);
                    }
                }

                setTabularData(topConsumerErrorsTable, columns, rows);
                panel = topConsumerErrorsPanel;
                break;
            }
                /* End tables for particular Consumer */
        }
        if (result != null && result.getReturnData() != null && panel != null) {
            int rows = result.getReturnData().size() + 1;
            double height = 0;
            if (rows > 10)
                height = 10 * 2.5;
            else
                height = rows * 2.5;
            panel.setContentContainerHeight(String.valueOf(height) + "em");
        }

        if (panel != null) {
            if (result != null)
                panel.setInfo(result.getRestUrl());
            show(panel);
        }
    }

    /**
     * Reset.
     * 
     * @see org.ebayopensource.turmeric.monitoring.client.presenter.ConsumerPresenter.Display#reset()
     */
    public void reset() {
        hide(callVolumePanel);
        hide(performancePanel);
        hide(errorsPanel);
        hide(topVolumePanel);
        hide(topServiceErrorsPanel);
        hide(leastPerformancePanel);
        hide(topConsumerErrorsPanel);
    }

    /**
     * Gets the table column.
     * 
     * @param metric
     *            the metric
     * @param startRow
     *            the start row
     * @param col
     *            the col
     * @return the table column
     * @see org.ebayopensource.turmeric.monitoring.client.presenter.ConsumerPresenter.Display#getTableColumn(org.ebayopensource.turmeric.monitoring.client.model.ConsumerMetric,
     *      int, int)
     */
    public List<HasClickHandlers> getTableColumn(ConsumerMetric metric, int startRow, int col) {
        FlexTable table = getTable(metric);
        if (table == null)
            return null;
        List<HasClickHandlers> list = new ArrayList<HasClickHandlers>();
        for (int i = startRow; i < table.getRowCount(); i++) {
            Widget w = table.getWidget(i, col);
            if (w instanceof HasClickHandlers)
                list.add((HasClickHandlers) w);
        }
        return list;
    }

    /**
     * Gets the data table object from the view, based on the metric displayed by the table.
     * 
     * @param m
     *            the m
     * @return the table
     */
    public FlexTable getTable(ConsumerMetric m) {
        FlexTable table = null;
        switch (m) {
            case CallVolume: {
                table = callVolumeTable;
                break;
            }
            case Performance: {
                table = performanceTable;
                break;
            }
            case Errors: {
                table = errorsTable;
                break;
            }
            case TopVolume: {
                table = topVolumeTable;
                break;
            }
            case TopServiceErrors: {
                table = topServiceErrorsTable;
                break;
            }
            case TopConsumerErrors: {
                table = topConsumerErrorsTable;
                break;
            }
            case LeastPerformance: {
                table = leastPerformanceTable;
                break;
            }
        }
        return table;
    }

    /**
     * Gets the table row.
     * 
     * @param metric
     *            the metric
     * @param row
     *            the row
     * @param startCol
     *            the start col
     * @return the table row
     */
    public List<HasClickHandlers> getTableRow(ConsumerMetric metric, int row, int startCol) {
        FlexTable table = getTable(metric);
        if (table == null)
            return null;
        List<HasClickHandlers> list = new ArrayList<HasClickHandlers>();
        for (int i = startCol; i < table.getCellCount(startCol); i++) {
            Widget w = table.getWidget(row, i);
            if (w instanceof HasClickHandlers)
                list.add((HasClickHandlers) w);
        }
        return list;
    }

    /**
     * Make panel.
     * 
     * @param heading
     *            the heading
     * @param contents
     *            the contents
     * @return the summary panel
     */
    protected SummaryPanel makePanel(String heading, FlexTable contents) {
        SummaryPanel panel = new SummaryPanel();
        panel.setHeading(heading);
        panel.setContents(contents);
        return panel;
    }

    /**
     * Make table.
     * 
     * @return the flex table
     */
    protected FlexTable makeTable() {
        FlexTable table = new FlexTable();
        table.addStyleName("tbl");
        table.setWidth("100%");
        return table;
    }

    /**
     * Sets the tabular data.
     * 
     * @param table
     *            the table
     * @param cols
     *            the cols
     * @param rows
     *            the rows
     */
    protected void setTabularData(FlexTable table, String[] cols, List<Widget[]> rows) {
        table.removeAllRows();
        if (cols != null) {
            for (int i = 0; i < cols.length; i++)
                table.setText(0, i, cols[i]);
        }
        table.getRowFormatter().addStyleName(0, "tbl-header1");

        if (rows != null) {
            int i = 0;
            for (Widget[] row : rows) {
                i++;
                for (int j = 0; j < row.length; j++) {
                    table.setWidget(i, j, row[j]);

                }
            }
        }
    }

    /**
     * Sets the filter label.
     * 
     * @param str
     *            the new filter label
     * @see org.ebayopensource.turmeric.monitoring.client.presenter.ConsumerPresenter.Display#setFilterLabel(java.lang.String)
     */
    public void setFilterLabel(String str) {
        filterButton.setText(str);
    }

    /**
     * Pick filter.
     */
    public void pickFilter() {
        int x = filterButton.getAbsoluteLeft();
        int y = filterButton.getAbsoluteTop() + filterButton.getOffsetHeight();
        filterDialog.setPopupPosition(x, y);
        filterDialog.setAnimationEnabled(true);
        filterDialog.setGlassEnabled(true);
        filterDialog.show();
    }

    /**
     * Sets the consumer call trend data.
     * 
     * @param graphData
     *            the graph data
     * @param aggregationPeriod
     *            the aggregation period
     * @param hourSpan
     *            the hour span
     * @param graphTitle
     *            the graph title
     * @see org.ebayopensource.turmeric.monitoring.client.presenter.ConsumerPresenter.Display#setConsumerCallTrendData(java.util.List,
     *      long, int, java.lang.String)
     */
    @Override
    public void setConsumerCallTrendData(List<TimeSlotData> graphData, long aggregationPeriod, int hourSpan,
                    String graphTitle) {
        if (graphData.get(0).getReturnData() != null && graphData.get(1).getReturnData() != null) {
            GraphRenderer renderer = new LineChartGraphRenderer(new SumGraphDataAggregator(), graphTitle,
                            this.topVolumePanel, graphData, aggregationPeriod, hourSpan);
            renderer.render();
        }
        else {
            GWT.log("empty graphData");
        }
    }

    /**
     * Sets the consumer performance trend data.
     * 
     * @param dataRanges
     *            the data ranges
     * @param aggregationPeriod
     *            the aggregation period
     * @param hourSpan
     *            the hour span
     * @param graphTitle
     *            the graph title
     * @see org.ebayopensource.turmeric.monitoring.client.presenter.ConsumerPresenter.Display#setConsumerPerformanceTrendData(java.util.List,
     *      long, int, java.lang.String)
     */
    @Override
    public void setConsumerPerformanceTrendData(List<TimeSlotData> dataRanges, long aggregationPeriod, int hourSpan,
                    String graphTitle) {
        if (dataRanges.get(0).getReturnData() != null && dataRanges.get(1).getReturnData() != null) {
            GraphRenderer renderer = new LineChartGraphRenderer(new SumGraphDataAggregator(), graphTitle,
                            this.leastPerformancePanel, dataRanges, aggregationPeriod, hourSpan);
            renderer.render();
        }
        else {
            GWT.log("empty graphData");
        }
    }

    /**
     * Sets the consumer error trend data.
     * 
     * @param dataRanges
     *            the data ranges
     * @param aggregationPeriod
     *            the aggregation period
     * @param hourSpan
     *            the hour span
     * @param graphTitle
     *            the graph title
     * @see org.ebayopensource.turmeric.monitoring.client.presenter.ConsumerPresenter.Display#setConsumerErrorTrendData(java.util.List,
     *      long, int, java.lang.String)
     */
    @Override
    public void setConsumerErrorTrendData(List<TimeSlotData> dataRanges, long aggregationPeriod, int hourSpan,
                    String graphTitle) {
        if (dataRanges.get(0).getReturnData() != null && dataRanges.get(1).getReturnData() != null) {
            GraphRenderer renderer = new LineChartGraphRenderer(new SumGraphDataAggregator(), graphTitle,
                            this.topServiceErrorsPanel, dataRanges, aggregationPeriod, hourSpan);
            renderer.render();
        }
        else {
            GWT.log("empty graphData");
        }
    }

    /**
     * Sets the consumer service call trend data.
     * 
     * @param dataRange
     *            the data range
     * @param aggregationPeriod
     *            the aggregation period
     * @param hourSpan
     *            the hour span
     * @param graphTitle
     *            the graph title
     * @see org.ebayopensource.turmeric.monitoring.client.presenter.ConsumerPresenter.Display#setConsumerServiceCallTrendData(java.util.Map,
     *      long, int, java.lang.String)
     */
    @Override
    public void setConsumerServiceCallTrendData(Map<String, List<TimeSlotData>> dataRange, long aggregationPeriod,
                    int hourSpan, String graphTitle) {

        GraphRenderer renderer = new ColumnChartGraphRenderer(new SumGraphDataAggregator(), graphTitle,
                        this.callVolumePanel, dataRange, aggregationPeriod, hourSpan);
        renderer.render();

    }

    /**
     * Sets the consumer service performance trend data.
     * 
     * @param graphData
     *            the graph data
     * @param aggregationPeriod
     *            the aggregation period
     * @param hourSpan
     *            the hour span
     * @param graphTitle
     *            the graph title
     * @see org.ebayopensource.turmeric.monitoring.client.presenter.ConsumerPresenter.Display#setConsumerServicePerformanceTrendData(java.util.Map,
     *      long, int, java.lang.String)
     */
    @Override
    public void setConsumerServicePerformanceTrendData(Map<String, List<TimeSlotData>> graphData,
                    long aggregationPeriod, int hourSpan, String graphTitle) {
        if (graphData != null) {
            ColumnChartGraphRenderer renderer = new ColumnChartGraphRenderer(new SumGraphDataAggregator(), graphTitle,
                            this.performancePanel, graphData, aggregationPeriod, hourSpan);
            renderer.render();
        }
        else {
            GWT.log("empty graphData");
        }
    }

    /**
     * Sets the consumer error count trend data.
     * 
     * @param graphData
     *            the graph data
     * @param aggregationPeriod
     *            the aggregation period
     * @param hourSpan
     *            the hour span
     * @param graphTitle
     *            the graph title
     * @see org.ebayopensource.turmeric.monitoring.client.presenter.ConsumerPresenter.Display#setConsumerErrorCountTrendData(java.util.Map,
     *      long, int, java.lang.String)
     */
    @Override
    public void setConsumerErrorCountTrendData(Map<String, List<TimeSlotData>> graphData, long aggregationPeriod,
                    int hourSpan, String graphTitle) {
        if (graphData != null) {
            GraphRenderer renderer = new ColumnChartGraphRenderer(new SumGraphDataAggregator(), graphTitle,
                            this.errorsPanel, graphData, aggregationPeriod, hourSpan);
            renderer.render();
        }
        else {
            GWT.log("empty graphData");
        }
    }

    /**
     * Clear consumer service call trend graph.
     * 
     * @see org.ebayopensource.turmeric.monitoring.client.presenter.ConsumerPresenter.Display#clearConsumerServiceCallTrendGraph()
     */
    @Override
    public void clearConsumerServiceCallTrendGraph() {
        this.callVolumePanel.clearChart();
    }

    @Override
    public void addValueChangeHandlerForDate1(ValueChangeHandler<Date> listener) {
        getFilter().getDate1().addValueChangeHandler(listener);
    }

    @Override
    public void addFilterOptionsApplyClickHandler(ClickHandler handler) {
        getFilter().getApplyButton().addClickHandler(handler);
    }

    @Override
    public void addTreeElementSelectionHandler(SelectionHandler<TreeItem> handler) {
        getSelector().addSelectionHandler(handler);
    }

    @Override
    public void clearConsumerServicePerformanceTrendsGraph() {
        this.performancePanel.clearChart();
    }

    @Override
    public void clearConsumerServiceErrorTrendsGraph() {
        this.errorsPanel.clearChart();
    }

}
