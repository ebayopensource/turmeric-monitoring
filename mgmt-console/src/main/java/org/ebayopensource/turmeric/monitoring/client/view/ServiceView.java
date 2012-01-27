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
import org.ebayopensource.turmeric.monitoring.client.model.Filterable;
import org.ebayopensource.turmeric.monitoring.client.model.MetricData;
import org.ebayopensource.turmeric.monitoring.client.model.MetricGroupData;
import org.ebayopensource.turmeric.monitoring.client.model.MetricsQueryService.Entity;
import org.ebayopensource.turmeric.monitoring.client.model.ObjectType;
import org.ebayopensource.turmeric.monitoring.client.model.ServiceMetric;
import org.ebayopensource.turmeric.monitoring.client.model.TimeSlotData;
import org.ebayopensource.turmeric.monitoring.client.model.TimeSlotValue;
import org.ebayopensource.turmeric.monitoring.client.presenter.ServicePresenter;
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
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * ServiceView
 * 
 * Display the metrics for the Services.
 */
public class ServiceView extends ResizeComposite implements ServicePresenter.Display {

    private SplitLayoutPanel splitPanel;
    private ServiceListWidget serviceListWidget;
    private Label summaryHeading;
    private SummaryPanel topVolumePanel;
    private FlexTable topVolumeTable;
    private SummaryPanel leastPerformancePanel;
    private FlexTable leastPerformanceTable;
    private SummaryPanel topErrorsPanel;
    private FlexTable topErrorsTable;
    private SummaryPanel consumerTrafficPanel;
    private FlexTable consumerTrafficTable;
    private FlexTable consumerErrorsTable;
    private SummaryPanel consumerErrorsPanel;
    private String id;
    private Button filterButton;

    private Dashboard dashboard;
    private FilterWidget filterWidget;
    private DialogBox filterDialog;
    private VerticalPanel customLogArea;

    /**
     * Instantiates a new service view.
     * 
     * @param dashboard
     *            the dashboard
     */
    public ServiceView(Dashboard dashboard) {
        customLogArea = new VerticalPanel();

        // make the panel
        DockLayoutPanel contentPanel = new DockLayoutPanel(Unit.EM);
        initWidget(contentPanel);

        // heading
        Panel topPanel = new FlowPanel();
        topPanel.addStyleName("summary-heading-panel");
        Grid g = new Grid(1, 2);
        g.setHeight("100%");
        g.setWidth("100%");
        summaryHeading = new Label(ConsoleUtil.constants.summary());
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
        // scrolling panel for right hand side
        ScrollPanel rhs = new ScrollPanel();
        rhs.setAlwaysShowScrollBars(true);
        rhs.addStyleName("summary-panel");

        FlowPanel panel = new FlowPanel();

        configureTopVolume();
        panel.add(topVolumePanel);

        configureLeastPerformance();
        panel.add(leastPerformancePanel);

        configureTopErrors();
        panel.add(topErrorsPanel);

        configureConsumerTraffic();
        panel.add(consumerTrafficPanel);

        configureConsumerErrors();
        panel.add(consumerErrorsPanel);

        rhs.add(panel);

        serviceListWidget = new ServiceListWidget();
        splitPanel.addWest(serviceListWidget, 200);
        splitPanel.add(rhs);

        // this sucks - I want to make the size of the north element
        // just big enough to contain its contents, but I cannot
        // obtain the size of them until they are actually added to their
        // container, which is too late!
        contentPanel.addNorth(topPanel, 2.5);
        contentPanel.add(splitPanel);

        disable();
        hide(topVolumePanel);
        hide(leastPerformancePanel);
        hide(topErrorsPanel);
        hide(consumerTrafficPanel);
        hide(consumerErrorsPanel);

        this.dashboard = dashboard;
        this.dashboard.addView(this, ConsoleUtil.constants.services());
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
     * Disable.
     */
    public void disable() {
        filterWidget.disable();
    }

    /**
     * Enable.
     */
    public void enable() {
        filterWidget.enable();
    }

    /**
     * Sets the services map.
     * 
     * @param map
     *            the map
     * @see org.ebayopensource.turmeric.monitoring.client.presenter.ServicePresenter.Display#setServicesMap(java.util.Map)
     */
    public void setServicesMap(Map<String, Set<String>> map) {
        serviceListWidget.setServicesMap(map);
    }

    /**
     * Sets the metric.
     * 
     * @param metric
     *            the metric
     * @param data
     *            the data
     * @see org.ebayopensource.turmeric.monitoring.client.presenter.ServicePresenter.Display#setMetric(org.ebayopensource
     *      .turmeric.monitoring.client.model.ServiceMetric,
     *      org.ebayopensource.turmeric.monitoring.client.model.MetricData)
     */
    public void setMetric(ServiceMetric metric, MetricData data) {
        SummaryPanel panel = null;
        String date1Header = "";
        String date2Header = "";
        if (data != null) {
            String d1 = ConsoleUtil.shotTimeFormat.format(new Date(data.getMetricCriteria().date1));
            String d2 = ConsoleUtil.shotTimeFormat.format(new Date(data.getMetricCriteria().date2));
            date1Header = d1 + " + " + (data.getMetricCriteria().durationSec / (60 * 60)) + ConsoleUtil.constants.hr();
            date2Header = d2 + " + " + (data.getMetricCriteria().durationSec / (60 * 60)) + ConsoleUtil.constants.hr();
        }
        switch (metric) {
            case TopVolume: {
                String count = ConsoleUtil.constants.count();
                String[] columns = { ConsoleUtil.constants.operations(),
                        (data == null ? count : date1Header + " " + count),
                        (data == null ? count : date2Header + " " + count), "% " + ConsoleUtil.constants.change() };
                List<String[]> rows = new ArrayList<String[]>();
                if (data != null) {
                    for (int i = 0; i < data.getReturnData().size(); i++) {
                        MetricGroupData rd = data.getReturnData().get(i);
                        String[] rowData = new String[4];
                        rowData[0] = getEntityName(metric, data.getMetricResourceCriteria().resourceEntityResponseType,
                                        rd);
                        rowData[1] = rd.getCount1();
                        rowData[2] = rd.getCount2();
                        rowData[3] = rd.getDiff();
                        rows.add(rowData);
                    }
                }
                setTabularData(topVolumeTable, columns, rows, null);
                panel = topVolumePanel;
                break;
            }
            case LeastPerformance: {
                String avg = ConsoleUtil.constants.average();
                String[] columns = { ConsoleUtil.constants.operations(),
                        (data == null ? avg : date1Header + " " + avg + " \u00B5s"),
                        (data == null ? avg : date2Header + " " + avg + " \u00B5s"),
                        "% " + ConsoleUtil.constants.change() };

                // convert from nanosec to microsec
                // consider moving this from the view to the presenter to do
                List<String[]> rows = new ArrayList<String[]>();

                if (data != null) {
                    for (int i = 0; i < data.getReturnData().size(); i++) {
                        MetricGroupData rd = data.getReturnData().get(i);
                        String[] rowData = new String[4];
                        rowData[0] = getEntityName(metric, data.getMetricResourceCriteria().resourceEntityResponseType,
                                        rd);
                        double d1 = Double.parseDouble(rd.getCount1());// / 1000.0;
                        double d2 = Double.parseDouble(rd.getCount2());// / 1000.0;
                        rowData[1] = String.valueOf(d1);
                        rowData[2] = String.valueOf(d2);
                        rowData[3] = rd.getDiff();
                        rows.add(rowData);
                    }
                }
                setTabularData(leastPerformanceTable, columns, rows, null);
                panel = leastPerformancePanel;
                break;
            }
            case TopErrors: {
                String count = ConsoleUtil.constants.count();
                String[] columns = { ConsoleUtil.constants.errors(),
                        (data == null ? count : date1Header + " " + count),
                        (data == null ? count : date2Header + " " + count), "% " + ConsoleUtil.constants.change() };
                List<String[]> rows = new ArrayList<String[]>();
                if (data != null) {
                    for (int i = 0; i < data.getReturnData().size(); i++) {
                        MetricGroupData rd = data.getReturnData().get(i);
                        String[] rowData = new String[4];
                        rowData[0] = getEntityName(metric, data.getMetricResourceCriteria().resourceEntityResponseType,
                                        rd);
                        rowData[1] = rd.getCount1();
                        rowData[2] = rd.getCount2();
                        rowData[3] = rd.getDiff();
                        rows.add(rowData);
                    }
                }
                setTabularData(topErrorsTable, columns, rows, null);
                // style the column with the names of the consumers in it

                panel = topErrorsPanel;
                break;
            }
            case ConsumerTraffic: {
                String count = ConsoleUtil.constants.count();
                String[] columns = { ConsoleUtil.constants.consumers(),
                        (data == null ? count : date1Header + " " + count),
                        (data == null ? count : date2Header + " " + count), "% " + ConsoleUtil.constants.change() };
                List<String[]> rows = new ArrayList<String[]>();
                if (data != null) {
                    for (int i = 0; i < data.getReturnData().size(); i++) {
                        MetricGroupData rd = data.getReturnData().get(i);
                        String[] rowData = new String[4];
                        rowData[0] = ConsoleUtil.convertConsumerFromMissing(rd.getCriteriaInfo().getConsumerName());
                        rowData[1] = rd.getCount1();
                        rowData[2] = rd.getCount2();
                        rowData[3] = rd.getDiff();
                        rows.add(rowData);
                    }
                }
                setTabularData(consumerTrafficTable, columns, rows, new String[] { "clickable", null, null });
                // style the column with the names of the consumers in it
                panel = consumerTrafficPanel;
                break;
            }
            case ConsumerErrors: {
                String count = ConsoleUtil.constants.count();
                String[] columns = { ConsoleUtil.constants.consumers(),
                        (data == null ? count : date1Header + " " + count),
                        (data == null ? count : date2Header + " " + count), "% " + ConsoleUtil.constants.change() };
                List<String[]> rows = new ArrayList<String[]>();
                if (data != null) {
                    for (int i = 0; i < data.getReturnData().size(); i++) {
                        MetricGroupData rd = data.getReturnData().get(i);
                        String[] rowData = new String[4];
                        rowData[0] = getEntityName(metric, data.getMetricResourceCriteria().resourceEntityResponseType,
                                        rd);
                        rowData[1] = rd.getCount1();
                        rowData[2] = rd.getCount2();
                        rowData[3] = rd.getDiff();
                        rows.add(rowData);
                    }
                }
                // style the column with the names of the consumers in it
                setTabularData(consumerErrorsTable, columns, rows, new String[] { "clickable", null, null });
                panel = consumerErrorsPanel;
                break;
            }
        }
        if (data != null && data.getReturnData() != null && panel != null) {
            int rows = data.getReturnData().size() + 1;
            double height = 0;
            if (rows > 10)
                height = 10 * 2.5;
            else
                height = rows * 2.5;
            panel.setContentContainerHeight(String.valueOf(height) + "em");
        }

        if (panel != null) {
            if (data != null)
                panel.setInfo(data.getRestUrl());
            show(panel);
        }
    }

    /**
     * Gets the filter.
     * 
     * @return the filter
     * @see org.ebayopensource.turmeric.monitoring.client.presenter.ServicePresenter.Display#getFilter()
     */
    public Filterable getFilter() {
        return filterWidget;
    }

    /**
     * Sets the selection.
     * 
     * @param selection
     *            the selection
     * @see org.ebayopensource.turmeric.monitoring.client.presenter.ServicePresenter.Display#setSelection(java.util.Map)
     */
    public void setSelection(Map<ObjectType, String> selection) {
        String s = "";
        if (selection != null) {
            String service = selection.get(ObjectType.ServiceName);
            String operation = selection.get(ObjectType.OperationName);
            if (service != null)
                s = s + " " + selection.get(ObjectType.ServiceName);
            if (operation != null)
                s = s + " : " + selection.get(ObjectType.OperationName);

            serviceListWidget.setSelection(service, operation);
        }
        else
            serviceListWidget.setSelection(null, null);
        if ("".equals(s))
            s = ConsoleUtil.constants.summary();
        summaryHeading.setText(s);
        enable();
    }

    /**
     * Gets the selector.
     * 
     * @return the selector
     * @see org.ebayopensource.turmeric.monitoring.client.presenter.ServicePresenter.Display#getSelector()
     */
    public HasSelectionHandlers<TreeItem> getSelector() {
        return serviceListWidget.getServiceTree();
    }

    /**
     * Error.
     * 
     * @param msg
     *            the msg
     * @see org.ebayopensource.turmeric.monitoring.client.presenter.ServicePresenter.Display#error(java.lang.String)
     */
    public void error(String msg) {
        ErrorDialog popup = new ErrorDialog(true);
        popup.setMessage(msg);
        popup.getDialog().center();
        popup.show();
    }

    /**
     * Reset.
     * 
     * @see org.ebayopensource.turmeric.monitoring.client.presenter.ServicePresenter.Display#reset()
     */
    public void reset() {
        // Window.alert("calling ServiceView.reset");
        hide(topVolumePanel);
        hide(topErrorsPanel);
        hide(leastPerformancePanel);
        hide(consumerErrorsPanel);
        hide(consumerTrafficPanel);
    }

    /**
     * Sets the download url.
     * 
     * @param m
     *            the m
     * @param url
     *            the url
     * @see org.ebayopensource.turmeric.monitoring.client.presenter.ServicePresenter.Display#setDownloadUrl(org.ebayopensource
     *      .turmeric.monitoring.client.model.ServiceMetric, java.lang.String)
     */
    public void setDownloadUrl(ServiceMetric m, String url) {
        SummaryPanel panel = null;
        switch (m) {
            case TopVolume: {
                panel = topVolumePanel;
                break;
            }
            case LeastPerformance: {
                panel = leastPerformancePanel;
                break;
            }
            case TopErrors: {
                panel = topErrorsPanel;
                break;
            }
            case ConsumerTraffic: {
                panel = consumerTrafficPanel;
                break;
            }
            case ConsumerErrors: {
                panel = consumerErrorsPanel;
                break;
            }
        }
        if (panel != null)
            panel.setDownloadUrl(url);
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
     * @see org.ebayopensource.turmeric.monitoring.client.presenter.ServicePresenter.Display#getTableColumn(org.ebayopensource
     *      .turmeric.monitoring.client.model.ServiceMetric, int, int)
     */
    public List<HasClickHandlers> getTableColumn(ServiceMetric metric, int startRow, int col) {
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
     * Gets the table row.
     * 
     * @param metric
     *            the metric
     * @param row
     *            the row
     * @param startCol
     *            the start col
     * @return the table row
     * @see org.ebayopensource.turmeric.monitoring.client.presenter.ServicePresenter.Display#getTableRow(org.ebayopensource
     *      .turmeric.monitoring.client.model.ServiceMetric, int, int)
     */
    public List<HasClickHandlers> getTableRow(ServiceMetric metric, int row, int startCol) {
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
     * Gets the table.
     * 
     * @param m
     *            the m
     * @return the table
     */
    @Override
    public FlexTable getTable(ServiceMetric m) {
        FlexTable table = null;
        switch (m) {
            case TopVolume: {
                table = topVolumeTable;
                break;
            }
            case LeastPerformance: {
                table = leastPerformanceTable;
                break;
            }
            case TopErrors: {
                table = topErrorsTable;
                break;
            }
            case ConsumerTraffic: {
                table = consumerTrafficTable;
                break;
            }
            case ConsumerErrors: {
                table = consumerErrorsTable;
                break;
            }
        }
        return table;
    }

    /**
     * Configure least performance.
     */
    protected void configureLeastPerformance() {
        leastPerformancePanel = new SummaryPanel();
        leastPerformancePanel.setHeading(ConsoleUtil.constants.leastPerformance());
        leastPerformanceTable = makeSummaryTable();
        setMetric(ServiceMetric.LeastPerformance, null);
        leastPerformancePanel.setContents(leastPerformanceTable);
    }

    /**
     * Configure top volume.
     */
    protected void configureTopVolume() {
        topVolumePanel = new SummaryPanel();
        topVolumePanel.setHeading(ConsoleUtil.constants.topVolume());
        topVolumeTable = makeSummaryTable();
        topVolumePanel.setContents(topVolumeTable);
        // topVolumePanel.setHeight("360px");
        setMetric(ServiceMetric.TopVolume, null);
    }

    /**
     * Configure consumer traffic.
     */
    protected void configureConsumerTraffic() {
        consumerTrafficPanel = new SummaryPanel();
        consumerTrafficPanel.setHeading(ConsoleUtil.constants.consumerTraffic());
        consumerTrafficTable = makeSummaryTable();
        setMetric(ServiceMetric.ConsumerTraffic, null);
        consumerTrafficPanel.setContents(consumerTrafficTable);
    }

    /**
     * Configure consumer errors.
     */
    protected void configureConsumerErrors() {
        consumerErrorsPanel = new SummaryPanel();
        consumerErrorsPanel.setHeading(ConsoleUtil.constants.consumerErrors());
        consumerErrorsTable = makeSummaryTable();
        setMetric(ServiceMetric.ConsumerErrors, null);
        consumerErrorsPanel.setContents(consumerErrorsTable);
    }

    /**
     * Configure top errors.
     */
    protected void configureTopErrors() {
        topErrorsPanel = new SummaryPanel();
        topErrorsPanel.setHeading(ConsoleUtil.constants.topErrors());
        topErrorsTable = makeSummaryTable();
        setMetric(ServiceMetric.TopErrors, null);
        topErrorsPanel.setContents(topErrorsTable);
    }

    /**
     * Make summary table.
     * 
     * @return the flex table
     */
    protected FlexTable makeSummaryTable() {
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
     * @param rowStyles
     *            the row styles
     */
    protected void setTabularData(FlexTable table, String[] cols, List<String[]> rows, String[] rowStyles) {
        table.removeAllRows();
        table.setText(0, 0, cols[0]);
        table.setText(0, 1, cols[1]);// flip flop dates - this is needed to fix
                                     // the % change sign error!!!
        table.setText(0, 2, cols[2]);// flip flop dates - this is needed to fix
                                     // the % change sign error!!!
        table.setText(0, 3, cols[3]);
        table.getRowFormatter().addStyleName(0, "tbl-header1");

        if (rows != null) {
            int i = 0;
            for (String[] row : rows) {
                i++;
                Label l = new Label(row[0]);
                table.setWidget(i, 0, l);
                // table.getCellFormatter().setWidth(i, 0, "30%");
                if (rowStyles != null && 0 < rowStyles.length && rowStyles[0] != null)
                    l.addStyleName(rowStyles[0]);

                l = new Label(NumberFormat.getDecimalFormat().format(new Double(row[1])));
                table.setWidget(i, 1, l);
                table.getCellFormatter().setWidth(i, 1, "25%");
                if (rowStyles != null && 1 < rowStyles.length && rowStyles[1] != null)
                    l.addStyleName(rowStyles[1]);

                l = new Label(NumberFormat.getDecimalFormat().format(new Double(row[2])));
                table.setWidget(i, 2, l);
                table.getCellFormatter().setWidth(i, 2, "25%");
                if (rowStyles != null && 2 < rowStyles.length && rowStyles[2] != null)
                    l.addStyleName(rowStyles[2]);
                l = new Label(row[3]);
                table.setWidget(i, 3, l);
                table.getCellFormatter().setWidth(i, 3, "15%");
                if (rowStyles != null && 3 < rowStyles.length && rowStyles[3] != null)
                    l.addStyleName(rowStyles[3]);
            }

        }
    }

    /**
     * Gets the entity name.
     * 
     * @param metric
     *            the metric
     * @param responseEntityType
     *            the response entity type
     * @param rd
     *            the rd
     * @return the entity name
     */
    protected String getEntityName(ServiceMetric metric, Entity responseEntityType, MetricGroupData rd) {
        String entityName = "";
        switch (responseEntityType) {
            case Service: {
                entityName = rd.getCriteriaInfo().getServiceName();
                break;
            }
            case Operation: {
                entityName = rd.getCriteriaInfo().getOperationName();
                break;
            }
            case Consumer: {
                entityName = ConsoleUtil.convertConsumerFromMissing(rd.getCriteriaInfo().getConsumerName());
                break;
            }
            case Machine: {
                entityName = rd.getCriteriaInfo().getMachineName();
                break;
            }
            case Pool: {
                entityName = rd.getCriteriaInfo().getPoolName();
                break;
            }
            case Error: {
        	    String label = "undefined";
                label = (String) ConsoleUtil.constants.metricDefNamesMap().get(rd.getCriteriaInfo().getMetricName());
                entityName= label;
                break;
            }
            default: {
                entityName = "";
                break;
            }
        }
        return entityName;
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
     * Sets the filter label.
     * 
     * @param s
     *            the new filter label
     * @see org.ebayopensource.turmeric.monitoring.client.presenter.ServicePresenter.Display#setFilterLabel(java.lang.String)
     */
    public void setFilterLabel(String s) {
        filterButton.setText(s);
    }

    /**
     * Sets the service call trend data.
     * 
     * @param graphData
     *            the graph data
     * @param aggregationPeriod
     *            the aggregation period
     * @param hourSpan
     *            the hour span
     * @param graphTitle
     *            the graph title
     * @see org.ebayopensource.turmeric.monitoring.client.presenter.ServicePresenter.Display#setServiceCallTrendData(java
     *      .util.List)
     */
    @Override
    public void setServiceCallTrendData(List<TimeSlotData> graphData, long aggregationPeriod, int hourSpan,
                    String graphTitle) {
        if (graphData.get(0).getReturnData() != null && graphData.get(1).getReturnData() != null) {
            // GraphUtil.createLineChart(topVolumePanel, graphData, aggregationPeriod, hourSpan, graphTitle);
            GraphRenderer renderer = new LineChartGraphRenderer(new SumGraphDataAggregator(), graphTitle,
                            this.topVolumePanel, graphData, aggregationPeriod, hourSpan);
            renderer.render();
        }
        else {
            GWT.log("empty graphData");
        }
    }

    /*
     * (non-Javadoc)
     * @see org.ebayopensource.turmeric.monitoring.client.presenter.ServicePresenter
     * .Display#setServicePerformanceTrendData (java.util.List)
     */
    @Override
    public void setServicePerformanceTrendData(List<TimeSlotData> graphData, long aggregationPeriod, int hourSpan,
                    String graphTitle) {
        if (graphData.get(0).getReturnData() != null && graphData.get(1).getReturnData() != null) {
            GraphRenderer renderer = new LineChartGraphRenderer(new SumGraphDataAggregator(), graphTitle,
                            this.leastPerformancePanel, graphData, aggregationPeriod, hourSpan);
            renderer.render();
            // GraphUtil.createLineChart(this.leastPerformancePanel, graphData, aggregationPeriod, hourSpan,
            // graphTitle);
        }
        else {
            GWT.log("empty graphData");
        }
    }

    private void printGraphDataValues(TimeSlotData timeSlotData) {
        for (TimeSlotValue value : timeSlotData.getReturnData()) {
            System.err.println("value.criteria=" + value.getCriteria());
            System.err.println("value.getTimeSlot=" + value.getTimeSlot());
            System.err.println("value.getValue=" + value.getValue());
        }
    }

    /*
     * (non-Javadoc)
     * @see org.ebayopensource.turmeric.monitoring.client.presenter.ServicePresenter
     * .Display#setServiceErrorTrendData(java .util.List)
     */
    @Override
    public void setServiceErrorTrendData(List<TimeSlotData> graphData, long aggregationPeriod, int hourSpan,
                    String graphTitle) {
        if (graphData.get(0).getReturnData() != null && graphData.get(1).getReturnData() != null) {
            // GraphUtil.createLineChart(this.topErrorsPanel, graphData, aggregationPeriod, hourSpan, graphTitle);
            GraphRenderer renderer = new LineChartGraphRenderer(new SumGraphDataAggregator(), graphTitle,
                            this.topErrorsPanel, graphData, aggregationPeriod, hourSpan);
            renderer.render();
        }
        else {
            GWT.log("empty graphData");
        }
    }

    /*
     * (non-Javadoc)
     * @see org.ebayopensource.turmeric.monitoring.client.presenter.ServicePresenter .Display#getErrorWidget()
     */
    @Override
    public HasWidgets getErrorWidget() {
        return customLogArea;
    }

    @Override
    public Widget getServiceCallCountTrendPanel() {
        return this.topVolumePanel;
    }

    @Override
    public Widget getServicePerformanceTrendPanel() {
        return this.leastPerformancePanel;
    }

    @Override
    public Widget getServiceErrorTrendPanel() {
        return this.topErrorsPanel;
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

}
