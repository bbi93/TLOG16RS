package com.bbi93.tlog16rs.rest;

import com.bbi93.tlog16rs.entities.Task;
import com.bbi93.tlog16rs.entities.TimeLogger;
import com.bbi93.tlog16rs.entities.WorkDay;
import com.bbi93.tlog16rs.entities.WorkMonth;
import com.bbi93.tlog16rs.exceptions.NotSeparatedTimesException;
import com.bbi93.tlog16rs.exceptions.WeekendNotEnabledException;
import com.bbi93.tlog16rs.services.TimeLoggerService;
import com.bbi93.tlog16rs.rest.beans.DeleteTaskRB;
import com.bbi93.tlog16rs.rest.beans.FinishingTaskRB;
import com.bbi93.tlog16rs.rest.beans.ModifyTaskRB;
import com.bbi93.tlog16rs.rest.beans.StartTaskRB;
import com.bbi93.tlog16rs.rest.beans.WorkMonthRB;
import com.bbi93.tlog16rs.rest.beans.WorkDayRB;
import java.util.Collection;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;

@Path("/")
@Slf4j
public class TLOG16RSResource {

	private static TimeLogger timelogger = new TimeLogger();
	private static TimeLoggerService timeloggerService = new TimeLoggerService();

	@GET
	@Path("/workmonths")
	@Produces(MediaType.APPLICATION_JSON)
	public Collection<WorkMonth> getWorkMonths() {
		return timeloggerService.getWorkMonths(timelogger);
	}

	@POST
	@Path("/workmonths")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public WorkMonth addNewMonth(WorkMonthRB monthRB) {
		return timeloggerService.addNewWorkMonth(timelogger, monthRB);
	}

	@POST
	@Path("/workmonths/workdays")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response addNewDay(WorkDayRB dayRB) {
		try {
			return Response.ok(timeloggerService.addNewWorkDay(timelogger, dayRB)).build();
		} catch (WeekendNotEnabledException ex) {
			log.error("Workday cannot be add to given year-month.", ex);
		}
		return Response.serverError().build();
	}

	@POST
	@Path("/workmonths/workdays/tasks/start")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response startTask(StartTaskRB taskRB) {
		try {
			return Response.ok(timeloggerService.startNewTask(timelogger, taskRB)).build();
		} catch (NotSeparatedTimesException ex) {
			log.error("Task cannot be add because task has timeconflict with other task.", ex);
		} catch (WeekendNotEnabledException ex) {
			log.error("Task cannot be add because the given day is on weekend", ex);
		}
		return Response.serverError().build();
	}

	@GET
	@Path("/workmonths/{year}/{month}")
	@Produces(MediaType.APPLICATION_JSON)
	public Collection<WorkDay> getWorkDays(
		@PathParam(value = "year") @NotNull @Valid int year,
		@PathParam(value = "month") @NotNull @Valid int month) {
		return timeloggerService.getWorkDays(timelogger, year, month);
	}

	@GET
	@Path("/workmonths/{year}/{month}/{day}")
	@Produces(MediaType.APPLICATION_JSON)
	public Collection<Task> getTasks(
		@PathParam(value = "year") @NotNull @Valid int year,
		@PathParam(value = "month") @NotNull @Valid int month,
		@PathParam(value = "day") @NotNull @Valid int day) {
		return timeloggerService.getTasks(timelogger, year, month, day);
	}

	@PUT
	@Path("/workmonths/workdays/tasks/finish")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response finishTask(FinishingTaskRB taskRB) {
		try {
			timeloggerService.finishSpecificTask(timelogger, taskRB);
			return Response.ok().build();
		} catch (NotSeparatedTimesException ex) {
			log.error("Task cannot be finish because task has timeconflict with other task.", ex);
		} catch (WeekendNotEnabledException ex) {
			log.error("Task cannot be add because the given day is on weekend", ex);
		}
		return Response.serverError().build();
	}

	@PUT
	@Path("/workmonths/workdays/tasks/modify")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response modifyTask(ModifyTaskRB taskRB) {
		try {
			timeloggerService.modifySpecificTask(timelogger, taskRB);
			return Response.ok().build();
		} catch (NotSeparatedTimesException ex) {
			log.error("Task cannot be modify because task has timeconflict with other task.", ex);
		} catch (WeekendNotEnabledException ex) {
			log.error("Task cannot be modify because not exists on the given day.", ex);
		}
		return Response.serverError().build();
	}

	@PUT
	@Path("/workmonths/workdays/tasks/delete")
	public Response deleteTask(DeleteTaskRB taskRB) {
		try {
			timeloggerService.deleteSpecificTask(timelogger, taskRB);
			return Response.ok().build();
		} catch (WeekendNotEnabledException ex) {
			log.error("Task cannot be delete because not exists on the given day.", ex);
		}
		return Response.serverError().build();
	}

	@PUT
	@Path("/workmonths/deleteall")
	public Response deleteAll() {
		timeloggerService.deleteAll(timelogger);
		return Response.ok().build();
	}

}
