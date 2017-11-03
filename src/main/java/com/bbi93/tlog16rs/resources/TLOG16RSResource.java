package com.bbi93.tlog16rs.resources;

import com.bbi93.tlog16rs.core.beans.DeleteTaskRB;
import com.bbi93.tlog16rs.core.beans.FinishingTaskRB;
import com.bbi93.tlog16rs.core.beans.ModifyTaskRB;
import com.bbi93.tlog16rs.core.beans.StartTaskRB;
import com.bbi93.tlog16rs.core.beans.Task;
import com.bbi93.tlog16rs.core.beans.TimeLogger;
import com.bbi93.tlog16rs.core.beans.WorkDay;
import com.bbi93.tlog16rs.core.beans.WorkMonth;
import com.bbi93.tlog16rs.core.beans.WorkMonthRB;
import com.bbi93.tlog16rs.core.beans.WorkDayRB;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;

@Path("/timelogger")
@Slf4j
public class TLOG16RSResource {

	ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

	private static TimeLogger timelogger = new TimeLogger();

	@Path("/workmonths")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Collection<WorkMonth> getWorkMonths() {
		return timelogger.getMonths();
	}

	@Path("/workmonths")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public WorkMonth addNewMonth(WorkMonthRB month) {
		WorkMonth workMonth = new WorkMonth(month.getYear(), month.getMonth());
		timelogger.addMonth(workMonth);
		return workMonth;
	}

	@Path("/workmonths/workdays")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public WorkDay addNewDay(WorkDayRB day) throws Exception {
		WorkDay workDay = new WorkDay();
		workDay.setRequiredMinPerDay(day.getRequiredHours());
		workDay.setActualDay(day.getYear(), day.getMonth(), day.getDay());
		WorkMonth selectedWorkMonth = selectWorkMonthByYearAndMonthNumber(timelogger.getMonths(), day.getYear(), day.getMonth());
		if (selectedWorkMonth == null) {
			WorkMonthRB newWorkMonth = new WorkMonthRB();
			newWorkMonth.setYear(day.getYear());
			newWorkMonth.setMonth(day.getMonth());
			selectedWorkMonth = this.addNewMonth(newWorkMonth);
		}
		selectedWorkMonth.addWorkDay(workDay);
		return workDay;
	}

	private WorkMonth selectWorkMonthByYearAndMonthNumber(Collection<WorkMonth> months, int yearToSearch, int monthToSearch) {
		WorkMonth selectedWorkMonth = null;
		for (WorkMonth month : months) {
			if (month.getDate().getYear() == yearToSearch && month.getDate().getMonthValue() == monthToSearch) {
				selectedWorkMonth = month;
				break;
			}
		}
		return selectedWorkMonth;
	}

	@Path("/workmonths/workdays/tasks/start")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Task startTask(StartTaskRB taskBean) throws Exception {
		Task task = new Task();
		task.setTaskId(taskBean.getTaskId());
		task.setStartTime(taskBean.getStartTime());
		task.setComment(taskBean.getComment());
		WorkDay selectedWorkDay = selectWorkDayByYearAndMonthAndDayNumber(timelogger.getMonths(), taskBean.getYear(), taskBean.getMonth(), taskBean.getDay());
		if (selectedWorkDay == null) {
			WorkDayRB newWorkDay = new WorkDayRB();
			newWorkDay.setYear(taskBean.getYear());
			newWorkDay.setMonth(taskBean.getMonth());
			newWorkDay.setDay(taskBean.getDay());
			selectedWorkDay = this.addNewDay(newWorkDay);
		}
		selectedWorkDay.addTask(task);
		return task;
	}

	private WorkDay selectWorkDayByYearAndMonthAndDayNumber(List<WorkMonth> months, int yearToSearch, int monthToSearch, int dayToSearch) {
		WorkDay selectedWorkDay = null;
		WorkMonth selectedWorkMonth = selectWorkMonthByYearAndMonthNumber(months, yearToSearch, monthToSearch);

		if (selectedWorkMonth == null) {
			WorkMonthRB newWorkMonth = new WorkMonthRB();
			newWorkMonth.setYear(yearToSearch);
			newWorkMonth.setMonth(monthToSearch);
			selectedWorkMonth = this.addNewMonth(newWorkMonth);
		}

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
		return selectedWorkDay;
	}

	@Path("/workmonths/{year}/{month}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Collection<WorkDay> getWorkDays(@PathParam(value = "year") int year, @PathParam(value = "month") int month) {
		WorkMonth selectedWorkMonth = selectWorkMonthByYearAndMonthNumber(timelogger.getMonths(), year, month);
		if (selectedWorkMonth == null) {
			WorkMonthRB newWorkMonth = new WorkMonthRB();
			newWorkMonth.setYear(year);
			newWorkMonth.setMonth(month);
			selectedWorkMonth = this.addNewMonth(newWorkMonth);
		}
		return selectedWorkMonth.getDays();
	}

	@Path("/workmonths/{year}/{month}/{day}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Collection<Task> getTasks(@PathParam(value = "year") int year, @PathParam(value = "month") int month, @PathParam(value = "day") int day) throws Exception {
		WorkDay selectedWorkDay = selectWorkDayByYearAndMonthAndDayNumber(timelogger.getMonths(), year, month, day);
		if (selectedWorkDay == null) {
			WorkDayRB newWorkDay = new WorkDayRB();
			newWorkDay.setYear(year);
			newWorkDay.setMonth(month);
			newWorkDay.setDay(day);
			selectedWorkDay = this.addNewDay(newWorkDay);
		}
		return selectedWorkDay.getTasks();
	}

	@Path("/workmonths/workdays/tasks/finish")
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Task finishTask(FinishingTaskRB taskBean) throws Exception {
		Task task = new Task();
		task.setTaskId(taskBean.getTaskId());
		task.setStartTime(taskBean.getStartTime());
		task.setEndTime(taskBean.getEndTime());
		WorkDay selectedWorkDay = selectWorkDayByYearAndMonthAndDayNumber(timelogger.getMonths(), taskBean.getYear(), taskBean.getMonth(), taskBean.getDay());
		if (selectedWorkDay == null) {
			WorkDayRB newWorkDay = new WorkDayRB();
			newWorkDay.setYear(taskBean.getYear());
			newWorkDay.setMonth(taskBean.getMonth());
			newWorkDay.setDay(taskBean.getDay());
			selectedWorkDay = this.addNewDay(newWorkDay);
		}
		Task selectedTask = selectTaskByWorkDayAndTaskIdandStartTime(selectedWorkDay, taskBean.getTaskId(), taskBean.getStartTime());
		if (selectedTask == null) {
			selectedWorkDay.addTask(task);
		} else {
			selectedTask.setEndTime(taskBean.getEndTime());
		}
		return selectedTask;
	}

	private Task selectTaskByWorkDayAndTaskIdandStartTime(WorkDay workDay, String taskId, String startTime) throws Exception {
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

	@Path("/workmonths/workdays/tasks/modify")
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Task modifyTask(ModifyTaskRB taskBean) throws Exception {
		WorkDay selectedWorkDay = selectWorkDayByYearAndMonthAndDayNumber(timelogger.getMonths(), taskBean.getYear(), taskBean.getMonth(), taskBean.getDay());
		if (selectedWorkDay == null) {
			WorkDayRB newWorkDay = new WorkDayRB();
			newWorkDay.setYear(taskBean.getYear());
			newWorkDay.setMonth(taskBean.getMonth());
			newWorkDay.setDay(taskBean.getDay());
			selectedWorkDay = this.addNewDay(newWorkDay);
		}
		Task selectedTask = selectTaskByWorkDayAndTaskIdandStartTime(selectedWorkDay, taskBean.getTaskId(), taskBean.getStartTime());
		if (selectedTask == null) {
			Task task = new Task();
			task.setTaskId(taskBean.getNewTaskId());
			task.setStartTime(taskBean.getNewStartTime());
			task.setEndTime(taskBean.getNewEndTime());
			task.setComment(taskBean.getNewComment());
			selectedWorkDay.addTask(task);
		} else {
			selectedTask.setTaskId(taskBean.getNewTaskId());
			selectedTask.setStartTime(taskBean.getNewStartTime());
			selectedTask.setEndTime(taskBean.getNewEndTime());
			selectedTask.setComment(taskBean.getNewComment());
		}
		return selectedTask;
	}

	@Path("/workmonths/workdays/tasks/delete")
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	public void deleteTask(DeleteTaskRB taskBean) throws Exception {
		WorkDay selectedWorkDay = selectWorkDayByYearAndMonthAndDayNumber(timelogger.getMonths(), taskBean.getYear(), taskBean.getMonth(), taskBean.getDay());
		if (selectedWorkDay == null) {
			WorkDayRB newWorkDay = new WorkDayRB();
			newWorkDay.setYear(taskBean.getYear());
			newWorkDay.setMonth(taskBean.getMonth());
			newWorkDay.setDay(taskBean.getDay());
			selectedWorkDay = this.addNewDay(newWorkDay);
		}
		Task selectedTask = selectTaskByWorkDayAndTaskIdandStartTime(selectedWorkDay, taskBean.getTaskId(), taskBean.getStartTime());
		if (selectedTask != null) {
			selectedWorkDay.getTasks().remove(selectedTask);
		}
	}

}
