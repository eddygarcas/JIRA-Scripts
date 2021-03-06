package com.onresolve.jira.groovy.test.scriptfields.scripts

import com.atlassian.jira.workflow.IssueWorkflowManager;
import com.atlassian.jira.workflow.IssueWorkflowManagerImpl;
import com.atlassian.jira.util.JiraUtils;
import com.atlassian.jira.component.ComponentAccessor
import com.onresolve.jira.groovy.user.FormField
import com.atlassian.jira.issue.history.ChangeItemBean
import com.atlassian.jira.issue.Issue
import java.text.SimpleDateFormat;
import java.text.DateFormat

def startStatus = ["impediment"]
def closedStatus = ["impediment"]

def changeHistoryManager = ComponentAccessor.getChangeHistoryManager()
def changeItems = changeHistoryManager.getChangeItemsForField(issue,"Flagged")

//IssueWorkflowManager flowManager = (IssueWorkflowManager) JiraUtils.loadComponent(IssueWorkflowManagerImpl.class)
//log.warn flowManager.getAvailableActions(issue).toString()
log.warn changeItems.size()


def inProgressTime=0
def doneTime=0
def days = 0
def stillBlocked = false

String holidays = "2018-12-06,2018-12-08,2018-12-24,2018-12-25,2018-12-26,2018-12-31,"+
                  "2019-01-01,2019-04-19,2019-04-22,2019-05-01,2019-06-24,2019-08-15,2019-09-11,2019-09-24,2019-11-01,2019-12-06,2019-12-08,2019-12-24,2019-12-25,2019-12-25,2019-12-26,2019-12-31,"+
    			  "2020-01-01"

changeItems.eachWithIndex { 
    ChangeItemBean item,index ->
    
    if( (startStatus.contains(item.getToString().toLowerCase())) && inProgressTime==0)
    	inProgressTime=item.getCreated().getTime()
    
    if ( (changeItems.size() & 1) !=0) {
    	doneTime=System.currentTimeMillis()
    	stillBlocked = true
    } else 
    	if(item.getFromString() != null && closedStatus.contains(item.getFromString().toLowerCase()))
        	doneTime=item.getCreated().getTime()
}

if (inProgressTime!=0){
    if(doneTime==0){
    	doneTime=System.currentTimeMillis()
	}
    Calendar startCal = Calendar.getInstance()
    startCal.setTime(new Date((long)inProgressTime))
    
    Calendar endCal = Calendar.getInstance()
    endCal.setTime(new Date((long)doneTime))
    
    while (startCal.before(endCal)){
        def weekDay = startCal.get(Calendar.DAY_OF_WEEK)
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd")
    	String dateString = dateFormat.format(startCal.getTime())

        if(Calendar.SATURDAY != weekDay && Calendar.SUNDAY != weekDay && !holidays.contains(dateString)){
            days++;
        }
        startCal.add(Calendar.DATE,1);
    }
}
//return working days
log.warn days
return "<p style='color:" + (stillBlocked ? "red" : "black")  + "'><strong>" + (days as long ?: 0L) + "</strong> day(s) blocked</p>"
