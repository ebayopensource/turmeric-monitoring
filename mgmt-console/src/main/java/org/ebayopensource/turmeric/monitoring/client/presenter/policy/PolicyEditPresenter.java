/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *******************************************************************************/
package org.ebayopensource.turmeric.monitoring.client.presenter.policy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.ebayopensource.turmeric.monitoring.client.ConsoleUtil;
import org.ebayopensource.turmeric.monitoring.client.SupportedService;
import org.ebayopensource.turmeric.monitoring.client.model.ConsoleService;
import org.ebayopensource.turmeric.monitoring.client.model.GenericPolicy;
import org.ebayopensource.turmeric.monitoring.client.model.HistoryToken;
import org.ebayopensource.turmeric.monitoring.client.model.Resource;
import org.ebayopensource.turmeric.monitoring.client.model.policy.ExtraField;
import org.ebayopensource.turmeric.monitoring.client.model.policy.GenericPolicyImpl;
import org.ebayopensource.turmeric.monitoring.client.model.policy.PolicyKey;
import org.ebayopensource.turmeric.monitoring.client.model.policy.PolicyQueryService;
import org.ebayopensource.turmeric.monitoring.client.model.policy.PolicyQueryService.GetPoliciesResponse;
import org.ebayopensource.turmeric.monitoring.client.model.policy.PolicySubjectAssignment;
import org.ebayopensource.turmeric.monitoring.client.model.policy.QueryCondition;
import org.ebayopensource.turmeric.monitoring.client.model.policy.Rule;
import org.ebayopensource.turmeric.monitoring.client.model.policy.RuleAttribute;
import org.ebayopensource.turmeric.monitoring.client.model.policy.Subject;
import org.ebayopensource.turmeric.monitoring.client.model.policy.SubjectGroup;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasWidgets;

public abstract class PolicyEditPresenter extends PolicyCreatePresenter {

	protected String originalPolicyId;
	protected String originalPolicyType;

	// protected final List<Rule> rules = new ArrayList<Rule>();

	public PolicyEditPresenter(HandlerManager eventBus, PolicyEditDisplay view,
			Map<SupportedService, ConsoleService> serviceMap) {
		super(eventBus, view, serviceMap);

	}

	/*
	 * Interface definitions
	 */
	public interface PolicyEditDisplay extends PolicyCreateDisplay {

	}

	@Override
	public void go(HasWidgets container, final HistoryToken token) {
		super.go(container, token);

		loadAssignments(token);
	}
	

	private void loadAssignments(final HistoryToken token) {

		originalPolicyId = token
				.getValue(HistoryToken.SELECTED_POLICY_TOKEN_ID);
		originalPolicyType = token
				.getValue(HistoryToken.SELECTED_POLICY_TOKEN_TYPE);

		PolicyKey pKey = new PolicyKey();
		pKey.setId(Long.valueOf(originalPolicyId));
		pKey.setType(originalPolicyType);
		ArrayList<PolicyKey> poKeys = new ArrayList<PolicyKey>();
		poKeys.add(pKey);
		QueryCondition condition = new QueryCondition();
		condition.addQuery(new QueryCondition.Query(
				QueryCondition.ActivePoliciesOnlyValue.FALSE));

		service.findPolicies(null, poKeys, null, null, null, null, null,
				condition,
				new AsyncCallback<PolicyQueryService.GetPoliciesResponse>() {

					@Override
					public void onFailure(Throwable caught) {
						PolicyEditPresenter.this.view
								.error(ConsoleUtil.messages.serverError(caught
										.getLocalizedMessage()));
						GWT.log("findPolicies:Fail");
					}

					@Override
					public void onSuccess(GetPoliciesResponse result) {
						GWT.log("findPolicies:Success");

						Collection<GenericPolicy> policies = result
								.getPolicies();

						for (GenericPolicy policy : policies) {
							resourceAssignments = new ArrayList<Resource>();
							resourceAssignments.addAll(policy.getResources());
							
							view.getResourceContentView().setAssignments(resourceAssignments);

							subjectAssignments = new ArrayList<PolicySubjectAssignment>();
							subjectAssignments
									.addAll(fetchSubjectAndSGAssignment(policy));
							view.getSubjectContentView().setAssignments(
									subjectAssignments);
							view.setPolicyName(policy.getName());
							view.setPolicyDesc(policy.getDescription());
							view.setEnabled(policy.getEnabled());
							//TODO improve this
							if("RL".equalsIgnoreCase(policy.getType())){
								setExtraFieldView(policy);	
							}
							
							break;
						}

					}
				});
	}

	// TODO make it abstract and move its content to an specific RL policy
	// View Presenter extends from PolicyViewPresenter
	protected void setExtraFieldView(GenericPolicy policy) {
		List<ExtraField> rlExtraFields = new ArrayList<ExtraField>();
		// TODO JOSE load from xml file from an Util class

		if (policy.getRules() != null && policy.getRules().size() > 0) {
			Rule rule = policy.getRules().get(0);

			
			if(rule.getAttributeList()!= null && rule.getAttributeList().size()>0){
				for (RuleAttribute attribute : rule.getAttributeList()) {
					if(RuleAttribute.NotifyKeys.NotifyEmails.name().equals(attribute.getKey())){
						// Policy Based Email Address
						view.setExtraFieldValue(1, attribute.getValue(),false);
					}
					if(RuleAttribute.NotifyKeys.NotifyActive.name().equals(attribute.getKey())){
						// Subject Based Email Address
						view.setExtraFieldValue(2, attribute.getValue(),false);
					}
				}
			}
		
			// Effect Duration
			view.setExtraFieldValue(3, rule.getEffectDuration().toString(),
					false);
			// Rollover period
			view.setExtraFieldValue(4, rule.getRolloverPeriod().toString(),
					false);
			// Priority
			view.setExtraFieldValue(5, rule.getPriority().toString(), false);
			// Priority
			view.setExtraFieldValue(6, rule.getEffect().toString(), false);

			// Condition
			try {

				view.setExtraFieldValue(7, rule.getCondition().getExpression()
						.getPrimitiveValue().getValue(), false);

			} catch (NullPointerException ex) {
				// do nothing...no condition value
				GWT.log("No condition Values ");
			}

		}

	}

	private List<PolicySubjectAssignment> fetchSubjectAndSGAssignment(
			GenericPolicy policy) {
		HashMap<String, List<Subject>> sAssignMap = new HashMap<String, List<Subject>>();

		for (Subject subject : policy.getSubjects()) {
			String type = subject.getType();

			if (!sAssignMap.containsKey(type)) {
				List list = new ArrayList();
				list.add(subject);
				sAssignMap.put(type, list);
			} else {
				List list = (List) sAssignMap.get(type);
				list.add(subject);
			}
		}

		HashMap<String, List<SubjectGroup>> sgAssignMap = new HashMap<String, List<SubjectGroup>>();

		for (SubjectGroup subjectGroup : policy.getSubjectGroups()) {
			String type = subjectGroup.getType();

			if (!sgAssignMap.containsKey(type)) {
				List list = new ArrayList();
				list.add(subjectGroup);
				sgAssignMap.put(type, list);
			} else {
				List list = (List) sgAssignMap.get(type);
				list.add(subjectGroup);
			}
		}

		// Generates the PolicySubjectAssignment objects
		List<PolicySubjectAssignment> polSubAssignmentList = new ArrayList<PolicySubjectAssignment>();
		PolicySubjectAssignment polSubAssignment = null;

		Iterator it = sAssignMap.keySet().iterator();
		while (it.hasNext()) {
			polSubAssignment = new PolicySubjectAssignment();

			String subjectType = (String) it.next();
			List<Subject> sList = (List<Subject>) sAssignMap.get(subjectType);

			polSubAssignment.setSubjectType(subjectType);
			polSubAssignment.setSubjects(sList);

			List<SubjectGroup> sgList = (List<SubjectGroup>) sgAssignMap
					.get(subjectType);

			if (null != sgList) {
				polSubAssignment.setSubjectGroups(sgList);
				sgAssignMap.remove(subjectType);
			}

			polSubAssignmentList.add(polSubAssignment);
		}

		// load remaining SG

		if (sgAssignMap.size() > 0) {
			Iterator itSg = sgAssignMap.keySet().iterator();
			while (itSg.hasNext()) {
				polSubAssignment = new PolicySubjectAssignment();

				String subjectGroupType = (String) it.next();
				List<SubjectGroup> sgList = (List<SubjectGroup>) sgAssignMap
						.get(subjectGroupType);

				polSubAssignment.setSubjectType(subjectGroupType);
				polSubAssignment.setSubjects(null);
				polSubAssignment.setSubjectGroups(sgList);

				polSubAssignmentList.add(polSubAssignment);

			}
		}

		return polSubAssignmentList;

	}

	public GenericPolicy getPolicy(String name, String type,
			String description, List<Resource> resources,
			List<PolicySubjectAssignment> subjectAssignments, boolean enabled,
			long id,  List<Rule> rules) {
		GenericPolicyImpl p = new GenericPolicyImpl();
		p.setName(name);
		p.setType(type);
		p.setDescription(description);

		// update existing one
		p.setId(id);
		p.setEnabled(enabled);
		
		if(rules != null){
			p.setRules(rules);
		}

		if (resources != null)
			p.setResources(new ArrayList<Resource>(resources));

		if (subjectAssignments != null) {
			List<Subject> subjects = new ArrayList<Subject>();
			List<SubjectGroup> groups = new ArrayList<SubjectGroup>();

			for (PolicySubjectAssignment a : subjectAssignments) {
				if (a.getSubjects() != null)
					subjects.addAll(a.getSubjects());
				if (a.getSubjectGroups() != null)
					groups.addAll(a.getSubjectGroups());
			}
			p.setSubjects(subjects);
			p.setSubjectGroups(groups);
		}
		return p;
	}

}