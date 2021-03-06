/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *******************************************************************************/
package org.ebayopensource.turmeric.monitoring.client.view.common;

import java.util.Iterator;

import org.ebayopensource.turmeric.monitoring.client.presenter.MenuController.MenuControllerDisplay;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.user.client.ui.Widget;

/**
 * The Class ApplicationMenuView.
 */
public class ApplicationMenuView extends ResizeComposite implements HasWidgets, MenuControllerDisplay {

	private DockLayoutPanel mainPanel;
	private HeaderWidget headerWidget;
	private Widget contentWidget;
	
	/**
	 * Instantiates a new application menu view.
	 */
	public ApplicationMenuView() {
	    initialize();
	    initWidget(mainPanel);
	}

	/**
	 * Initialize.
	 */
	public void initialize() {
	    
	    mainPanel = new DockLayoutPanel(Unit.PX);
        mainPanel.setWidth("100%");

        headerWidget = new HeaderWidget("58");
        //footerWidget = new FooterWidget();

        mainPanel.addNorth(headerWidget,58);
        //mainPanel.addSouth(footerWidget,40);
	}

	
	

    /**
     * Adds the.
     *
     * @param arg0 the arg0
     * @see com.google.gwt.user.client.ui.HasWidgets#add(com.google.gwt.user.client.ui.Widget)
     */
    @Override
    public void add(Widget arg0) {
        if (contentWidget != null)
            mainPanel.remove(contentWidget);
        contentWidget = arg0;
        mainPanel.add(contentWidget);
    }

    /**
     * Clear.
     *
     * @see com.google.gwt.user.client.ui.HasWidgets#clear()
     */
    @Override
    public void clear() {
        if (contentWidget != null)
            mainPanel.remove(contentWidget);
        contentWidget = null;
    }

    /**
     * Iterator.
     *
     * @return the iterator
     * @see com.google.gwt.user.client.ui.HasWidgets#iterator()
     */
    @Override
    public Iterator<Widget> iterator() {
        return mainPanel.iterator();
    }

    /**
     * Removes the.
     *
     * @param arg0 the arg0
     * @return true, if successful
     * @see com.google.gwt.user.client.ui.HasWidgets#remove(com.google.gwt.user.client.ui.Widget)
     */
    @Override
    public boolean remove(Widget arg0) {
        boolean result = false;
        if (contentWidget != null) {
            result = mainPanel.remove(contentWidget);
        }
        contentWidget = null;
        return result;
    }

    /**
     * As widget.
     *
     * @return the widget
     * @see org.ebayopensource.turmeric.monitoring.client.Container#asWidget()
     */
    @Override
    public Widget asWidget() {
        return this;
    }


    

}
    