package com.bbi93.tlog16rs.resources;

import com.bbi93.tlog16rs.core.beans.DeleteTaskRB;
import com.bbi93.tlog16rs.core.beans.FinishingTaskRB;
import com.bbi93.tlog16rs.core.beans.ModifyTaskRB;
import com.bbi93.tlog16rs.core.beans.TimeLoggerService;
import com.bbi93.tlog16rs.core.beans.StartTaskRB;
import com.bbi93.tlog16rs.core.beans.Task;
import com.bbi93.tlog16rs.core.beans.TimeLogger;
import com.bbi93.tlog16rs.core.beans.WorkDay;
import com.bbi93.tlog16rs.core.beans.WorkMonth;
import com.bbi93.tlog16rs.core.beans.WorkMonthRB;
import com.bbi93.tlog16rs.core.beans.WorkDayRB;
import java.util.Collection;
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
	public WorkDay addNewDay(WorkDayRB day) throws Exception {
		WorkDay workDay = new WorkDay();
		workDay.setRequiredMinPerDay(day.getRequiredHours());
		workDay.setActualDay(day.getYear(), day.getMonth(), day.getDay());
		WorkMonth selectedWorkMonth = service.selectWorkMonthByYearAndMonthNumber(timelogger.getMonths(), day.getYear(), day.getMonth());
		selectedWorkMonth.addWorkDay(workDay);
		return workDay;
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
		WorkDay selectedWorkDay = service.selectWorkDayByYearAndMonthAndDayNumber(timelogger.getMonths(), taskBean.getYear(), taskBean.getMonth(), taskBean.getDay());
		selectedWorkDay.addTask(task);
		return task;
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
	public Collection<Task> getTasks(@PathParam(value = "year") int year, @PathParam(value = "month") int month, @PathParam(value = "day") int day) throws Exception {
		WorkDay selectedWorkDay = service.selectWorkDayByYearAndMonthAndDayNumber(timelogger.getMonths(), year, month, day);
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
		WorkDay selectedWorkDay = service.selectWorkDayByYearAndMonthAndDayNumber(timelogger.getMonths(), taskBean.getYear(), taskBean.getMonth(), taskBean.getDay());
		Task selectedTask = service.selectTaskByWorkDayAndTaskIdandStartTime(selectedWorkDay, taskBean.getTaskId(), taskBean.getStartTime());
		if (selectedTask == null) {
			selectedWorkDay.addTask(task);
		} else {
			selectedTask.setEndTime(taskBean.getEndTime());
		}
		return selectedTask;
	}


	@Path("/workmonths/workdays/tasks/modify")
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Task modifyTask(ModifyTaskRB taskBean) throws Exception {
		WorkDay selectedWorkDay = service.selectWorkDayByYearAndMonthAndDayNumber(timelogger.getMonths(), taskBean.getYear(), taskBean.getMonth(), taskBean.getDay());
		Task selectedTask = service.selectTaskByWorkDayAndTaskIdandStartTime(selectedWorkDay, taskBean.getTaskId(), taskBean.getStartTime());
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
		WorkDay selectedWorkDay = service.selectWorkDayByYearAndMonthAndDayNumber(timelogger.getMonths(), taskBean.getYear(), taskBean.getMonth(), taskBean.getDay());
		Task selectedTask = service.selectTaskByWorkDayAndTaskIdandStartTime(selectedWorkDay, taskBean.getTaskId(), taskBean.getStartTime());
		if (selectedTask != null) {
			selectedWorkDay.getTasks().remove(selectedTask);
		}
	}

}
