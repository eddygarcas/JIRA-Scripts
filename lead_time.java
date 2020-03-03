package com.onresolve.jira.groovy.test.scriptfields.scripts

import com.atlassian.jira.component.ComponentAccessor
import com.onresolve.jira.groovy.user.FormField
import com.atlassian.jira.issue.history.ChangeItemBean
import com.atlassian.jira.issue.changehistory.ChangeHistoryItem
import com.atlassian.jira.issue.Issue
import java.text.SimpleDateFormat;
import java.text.DateFormat
import org.apache.log4j.Logger
import com.atlassian.jira.issue.status.category.StatusCategory

def log = Logger.getLogger("com.onresolve.scriptrunner.runner.ScriptRunnerImpl")
def stillWip = (issue.getStatus().getStatusCategory().getKey() == StatusCategory.IN_PROGRESS) ? true : false

String holidays = "2019-12-06,2019-12-08,2019-12-24,2019-12-25,2019-12-26,2019-12-31,"+
                  "2020-01-01,2020-04-19,2020-04-22,2020-05-01,2020-06-24,2020-08-15," +
    			  "2020-09-11,2020-09-24,2020-11-01,2020-12-06,2020-12-08,2020-12-24," +
    			  "2020-12-25,2020-12-25,2020-12-26,2020-12-31,"+
    			  "2021-01-01"


def workflow = ComponentAccessor.getWorkflowManager().getWorkflow(issue)
def closedStatus = []
workflow.getLinkedStatusObjects().each{ status ->
    if (status.getStatusCategory().getKey() == StatusCategory.COMPLETE) {
        closedStatus << status.getName().toLowerCase()
    }
}


def inProgressTime=0
def doneTime=0
def days = 0


def changeHistoryManager = ComponentAccessor.getChangeHistoryManager()
def initialStatus = changeHistoryManager.getAllChangeItems(issue).get(0)
def changeItems = changeHistoryManager.getChangeItemsForField(issue,"status")

inProgressTime = initialStatus.getCreated().getTime()
changeItems.each{
    ChangeItemBean item->
    if(closedStatus.contains(item.getToString().toLowerCase()))
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
return "<p style='color:" + (stillWip ? "green" : "blue-gray")  + "'><strong>" + (days as long ?: 0L) + "</strong>d to " + issue.getStatus().getStatusCategory().getTranslatedName() +"</p>"
