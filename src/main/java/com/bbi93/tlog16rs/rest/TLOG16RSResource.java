package com.bbi93.tlog16rs.rest;

import com.bbi93.tlog16rs.rest.beans.DeleteTaskRB;
import com.bbi93.tlog16rs.rest.beans.FinishingTaskRB;
import com.bbi93.tlog16rs.rest.beans.ModifyTaskRB;
import com.bbi93.tlog16rs.services.TimeLoggerService;
import com.bbi93.tlog16rs.rest.beans.StartTaskRB;
import com.bbi93.tlog16rs.entities.Task;
import com.bbi93.tlog16rs.entities.TimeLogger;
import com.bbi93.tlog16rs.entities.WorkDay;
import com.bbi93.tlog16rs.entities.WorkMonth;
import com.bbi93.tlog16rs.rest.beans.WorkMonthRB;
import com.bbi93.tlog16rs.rest.beans.WorkDayRB;
import com.bbi93.tlog16rs.exceptions.EmptyTimeFieldException;
import com.bbi93.tlog16rs.exceptions.FutureWorkException;
import com.bbi93.tlog16rs.exceptions.InvalidTaskIdException;
import com.bbi93.tlog16rs.exceptions.NegativeMinutesOfWorkException;
import com.bbi93.tlog16rs.exceptions.NoTaskIdException;
import com.bbi93.tlog16rs.exceptions.NotExpectedTimeOrderException;
import com.bbi93.tlog16rs.exceptions.NotSeparatedTimesException;
import com.bbi93.tlog16rs.exceptions.WeekendNotEnabledException;
import java.time.DateTimeException;
import java.util.Collection;
import java.util.LinkedList;
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

	private static TimeLogger timelogger = new TimeLogger();
	private static TimeLoggerService service = new TimeLoggerService();

	public TLOG16RSResource() {
		service.setResource(this);
	}

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
	public WorkDay addNewDay(WorkDayRB day) {
		WorkDay workDay = new WorkDay();
		try {
			workDay.setRequiredMinPerDay(day.getRequiredHours());
			workDay.setActualDay(day.getYear(), day.getMonth(), day.getDay());
			WorkMonth selectedWorkMonth = service.selectWorkMonthByYearAndMonthNumber(timelogger.getMonths(), day.getYear(), day.getMonth());
			selectedWorkMonth.addWorkDay(workDay);
			return workDay;
		} catch (NegativeMinutesOfWorkException | DateTimeException | FutureWorkException ex) {
			log.error("Error during create workday. {0} - {1}", ex.getClass(), ex.getMessage());
		} catch (WeekendNotEnabledException ex) {
			log.error("Error during add workday to workmonth. {0} - {1}", ex.getClass(), ex.getMessage());
		}
		return null;
	}

	@Path("/workmonths/workdays/tasks/start")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Task startTask(StartTaskRB taskBean) {
		Task task = new Task();
		try {
			task.setTaskId(taskBean.getTaskId());
			task.setStartTime(taskBean.getStartTime());
			task.setComment(taskBean.getComment());
			WorkDay selectedWorkDay = service.selectWorkDayByYearAndMonthAndDayNumber(timelogger.getMonths(), taskBean.getYear(), taskBean.getMonth(), taskBean.getDay());
			selectedWorkDay.addTask(task);
			return task;
		} catch (InvalidTaskIdException | NoTaskIdException | EmptyTimeFieldException ex) {
			log.error("Error during start task. {0} - {1}", ex.getClass(), ex.getMessage());
		} catch (NotSeparatedTimesException ex) {
			log.error("Error during add task to workday. {0} - {1}", ex.getClass(), ex.getMessage());
		}
		return null;
	}

	@Path("/workmonths/{year}/{month}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Collection<WorkDay> getWorkDays(@PathParam(value = "year") int year, @PathParam(value = "month") int month) {
		WorkMonth selectedWorkMonth = service.selectWorkMonthByYearAndMonthNumber(timelogger.getMonths(), year, month);
		return selectedWorkMonth.getDays();
	}

	@Path("/workmonths/{year}/{month}/{day}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Collection<Task> getTasks(@PathParam(value = "year") int year, @PathParam(value = "month") int month, @PathParam(value = "day") int day) {
		WorkDay selectedWorkDay = service.selectWorkDayByYearAndMonthAndDayNumber(timelogger.getMonths(), year, month, day);
		return selectedWorkDay.getTasks();
	}

	@Path("/workmonths/workdays/tasks/finish")
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	public void finishTask(FinishingTaskRB taskBean) {
		Task task = new Task();
		try {
			task.setTaskId(taskBean.getTaskId());
			task.setStartTime(taskBean.getStartTime());
			task.setEndTime(taskBean.getEndTime());
			WorkDay selectedWorkDay = service.selectWorkDayByYearAndMonthAndDayNumber(timelogger.getMonths(), taskBean.getYear(), taskBean.getMonth(), taskBean.getDay());
			Task selectedTask = service.selectTaskByWorkDayAndTaskIdandStartTime(selectedWorkDay, taskBean.getTaskId(), taskBean.getStartTime());
			if (selectedTask == null) {
				selectedWorkDay.addTask(task);
			} else {
				selectedTask.setEndTime(taskBean.getEndTime());
			}
		} catch (InvalidTaskIdException | NoTaskIdException | EmptyTimeFieldException | NotExpectedTimeOrderException | NotSeparatedTimesException ex) {
			log.error("Error during finishing task. {0} - {1}", ex.getClass(), ex.getMessage());
		}
	}

	@Path("/workmonths/workdays/tasks/modify")
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	public void modifyTask(ModifyTaskRB taskBean) {
		WorkDay selectedWorkDay = service.selectWorkDayByYearAndMonthAndDayNumber(timelogger.getMonths(), taskBean.getYear(), taskBean.getMonth(), taskBean.getDay());
		Task selectedTask;
		try {
			selectedTask = service.selectTaskByWorkDayAndTaskIdandStartTime(selectedWorkDay, taskBean.getTaskId(), taskBean.getStartTime());
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
		} catch (EmptyTimeFieldException | InvalidTaskIdException | NoTaskIdException | NotExpectedTimeOrderException | NotSeparatedTimesException ex) {
			log.error("Error during modifying task. {0} - {1}", ex.getClass(), ex.getMessage());
		}
	}

	@Path("/workmonths/workdays/tasks/delete")
	@PUT
	public void deleteTask(DeleteTaskRB taskBean) {
		WorkDay selectedWorkDay = service.selectWorkDayByYearAndMonthAndDayNumber(timelogger.getMonths(), taskBean.getYear(), taskBean.getMonth(), taskBean.getDay());
		Task selectedTask;
		selectedTask = service.selectTaskByWorkDayAndTaskIdandStartTime(selectedWorkDay, taskBean.getTaskId(), taskBean.getStartTime());
		if (selectedTask != null) {
			selectedWorkDay.getTasks().remove(selectedTask);
		}
	}

	@Path("/workmonths/deleteall")
	@PUT
	public void deleteAll() {
		timelogger.setMonths(new LinkedList<>());
	}

}
