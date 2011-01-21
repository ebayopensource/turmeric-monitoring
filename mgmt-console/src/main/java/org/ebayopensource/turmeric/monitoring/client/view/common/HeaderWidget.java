/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *******************************************************************************/
package org.ebayopensource.turmeric.monitoring.client.view.common;

import java.util.List;
import java.util.Map;

import org.ebayopensource.turmeric.monitoring.client.ConsoleUtil;
import org.ebayopensource.turmeric.monitoring.client.model.UserAction;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

public class HeaderWidget extends Composite {
	
	private HasClickHandlers logo;
	private Button logoutButton;
	private ListBox apps;
	private boolean hasSelected = false;	

	public HeaderWidget(String width) {
		Panel panel = new FlowPanel();
	    panel.addStyleName("header");
		initWidget(panel);

		panel.setWidth(width);
		Grid headerGrid = new Grid(1,2);
		headerGrid.setWidth("100%");

		
		logo = new Image("images/turmeric-small.png");
		headerGrid.setWidget(0, 0, (Widget) logo);
		headerGrid.getCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_LEFT);
		
		apps = new ListBox(false);
		apps.addChangeHandler(new ChangeHandler() {

            public void onChange(ChangeEvent event) {
                //at least 1 app has been selected, don't show the selection message any more
                hasSelected = true;
            }		    
		});
		
		Grid actionGrid = new Grid(1,2);
		headerGrid.setWidget(0,1, actionGrid);
		logoutButton = new Button(ConsoleUtil.constants.logout());	
		actionGrid.setWidget(0, 0, apps);
		actionGrid.setWidget(0, 1, logoutButton);
		headerGrid.getCellFormatter().setHorizontalAlignment(0, 1, HasHorizontalAlignment.ALIGN_RIGHT);
		panel.add(headerGrid);
	}

	public void setAvailableApps (Map<String, String> avail) {
	    apps.clear();
	    if (apps == null)
	        return;

	        apps.addItem(ConsoleUtil.policyAdminMessages.selectAnApplication()+" ....");

	    for (String key: avail.keySet()) {
	        apps.addItem(key, avail.get(key));
	    }
	}

	public void setSelectedApp (String app) {
	    if (apps == null)
	        return;
	    
	    for (int i=0;i<apps.getItemCount();i++) {
	        if (app.equals(apps.getValue(i)))
	            apps.setItemSelected(i, true);
	    }
	}

	public void setUserName (String username) {
	    if (username == null  || "".equals(username))
	        username = "";
	    logoutButton.setText(ConsoleUtil.constants.logout()+":"+username);
	}

	public HasChangeHandlers getAppSelectionChange () {
	    return apps;
	}
	
	public String getSelectedApp () {
	    int i =  apps.getSelectedIndex();
	    if (i < 0)
	        return null;
	    return apps.getValue(i);
	}
	
	public HasClickHandlers getLogoComponent() {
		return logo;
	}
	
	public HasClickHandlers getLogoutComponent() {
		return logoutButton;
	}
	
}