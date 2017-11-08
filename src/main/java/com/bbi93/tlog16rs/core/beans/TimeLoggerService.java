/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bbi93.tlog16rs.core.beans;

import com.bbi93.tlog16rs.core.exceptions.EmptyTimeFieldException;
import com.bbi93.tlog16rs.resources.TLOG16RSResource;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import lombok.NoArgsConstructor;

/**
 *
 * @author bbi93
 */
@NoArgsConstructor
public class TimeLoggerService {

	private static TLOG16RSResource resource;

	public static void setResource(TLOG16RSResource resource) {
		TimeLoggerService.resource = resource;
	}

	public WorkMonth selectWorkMonthByYearAndMonthNumber(Collection<WorkMonth> months, int yearToSearch, int monthToSearch) {
		WorkMonth selectedWorkMonth = null;
		for (WorkMonth month : months) {
			if (month.getDate().getYear() == yearToSearch && month.getDate().getMonthValue() == monthToSearch) {
				selectedWorkMonth = month;
				break;
			}
		}
		if (selectedWorkMonth == null) {
			selectedWorkMonth = resource.addNewMonth(createWorkMonthRB(yearToSearch, monthToSearch));
		}
		return selectedWorkMonth;
	}

	private WorkMonthRB createWorkMonthRB(int year, int month) {
		WorkMonthRB newWorkMonth = new WorkMonthRB();
		newWorkMonth.setYear(year);
		newWorkMonth.setMonth(month);
		return newWorkMonth;
	}

	public WorkDay selectWorkDayByYearAndMonthAndDayNumber(List<WorkMonth> months, int yearToSearch, int monthToSearch, int dayToSearch) {
		WorkDay selectedWorkDay = null;
		WorkMonth selectedWorkMonth = selectWorkMonthByYearAndMonthNumber(months, yearToSearch, monthToSearch);

		for (WorkDay workDay : selectedWorkMonth.getDays()) {
			if (workDay.getActualDay().getYear() == yearToSearch) {
				if (workDay.getActualDay().getMonthValue() == monthToSearch) {
					if (workDay.getActualDay().getDayOfMonth() == dayToSearch) {
						selectedWorkDay = workDay;
						break;
					}
				}
			}
		}

		if (selectedWorkDay == null) {
			selectedWorkDay = resource.addNewDay(createWorkDayRB(yearToSearch, monthToSearch, dayToSearch));
		}
		return selectedWorkDay;
	}

	private WorkDayRB createWorkDayRB(int year, int month, int day) {
		WorkDayRB newWorkDay = new WorkDayRB();
		newWorkDay.setYear(year);
		newWorkDay.setMonth(month);
		newWorkDay.setDay(day);
		return newWorkDay;
	}

	public Task selectTaskByWorkDayAndTaskIdandStartTime(WorkDay workDay, String taskId, String startTime) throws EmptyTimeFieldException {
		Task selectedTask = null;
		for (Task task : workDay.getTasks()) {
			if (task.getTaskId().equals(taskId)) {
				if (task.getStartTime().equals(LocalTime.parse(startTime))) {
					selectedTask = task;
					break;
				}
			}
		}
		return selectedTask;
	}

}
