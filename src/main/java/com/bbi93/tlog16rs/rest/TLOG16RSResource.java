package com.bbi93.tlog16rs.rest;

import com.avaje.ebean.Ebean;
import com.bbi93.tlog16rs.application.TLOG16RSApplication;
import com.bbi93.tlog16rs.entities.Task;
import com.bbi93.tlog16rs.entities.TimeLogger;
import com.bbi93.tlog16rs.entities.WorkDay;
import com.bbi93.tlog16rs.entities.WorkMonth;
import com.bbi93.tlog16rs.exceptions.NotSeparatedTimesException;
import com.bbi93.tlog16rs.exceptions.UserExistException;
import com.bbi93.tlog16rs.exceptions.WeekendNotEnabledException;
import com.bbi93.tlog16rs.services.TimeLoggerService;
import com.bbi93.tlog16rs.rest.beans.DeleteTaskRB;
import com.bbi93.tlog16rs.rest.beans.FinishingTaskRB;
import com.bbi93.tlog16rs.rest.beans.ModifyTaskRB;
import com.bbi93.tlog16rs.rest.beans.StartTaskRB;
import com.bbi93.tlog16rs.rest.beans.UserRB;
import com.bbi93.tlog16rs.rest.beans.WorkMonthRB;
import com.bbi93.tlog16rs.rest.beans.WorkDayRB;
import com.bbi93.tlog16rs.services.DbService;
import java.util.Collection;
import javax.naming.AuthenticationException;
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

@Path("/timelogger")
@Slf4j
public class TLOG16RSResource {

	private static final String TIMELOGGER_NAME = "bbors";
	private static final int TIMELOGGER_ID = 1;
	private static TimeLoggerService timeloggerService = new TimeLoggerService();
	private DbService dbService;

	public TLOG16RSResource(TLOG16RSApplication application) {
		dbService = application.getDbService();
		if (Ebean.find(TimeLogger.class).findRowCount() == 0) {
			Ebean.save(new TimeLogger(TIMELOGGER_NAME));
		}
	}

	@GET
	@Path("/workmonths")
	@Produces(MediaType.APPLICATION_JSON)
	public Collection<WorkMonth> getWorkMonths() {
		TimeLogger timelogger = Ebean.find(TimeLogger.class, TIMELOGGER_ID);
		return timeloggerService.getWorkMonths(timelogger);
	}

	@POST
	@Path("/workmonths")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public WorkMonth addNewMonth(WorkMonthRB monthRB) {
		TimeLogger timelogger = Ebean.find(TimeLogger.class, TIMELOGGER_ID);
		WorkMonth newWorkMonth = timeloggerService.addNewWorkMonth(timelogger, monthRB);
		Ebean.save(timelogger);
		return newWorkMonth;
	}

	@POST
	@Path("/workmonths/workdays")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response addNewDay(WorkDayRB dayRB) {
		Response response;
		TimeLogger timelogger = Ebean.find(TimeLogger.class, TIMELOGGER_ID);
		try {
			WorkDay newWorkDay = timeloggerService.addNewWorkDay(timelogger, dayRB);
			Ebean.save(timelogger);
			response = Response.ok(newWorkDay).build();
		} catch (WeekendNotEnabledException wneex) {
			log.error("Workday cannot be add to given year-month because the given day is on weekend.", wneex);
			response = Response.status(Response.Status.NOT_ACCEPTABLE).build();
		} catch (Exception ex) {
			response = Response.status(Response.Status.BAD_REQUEST).build();
		}
		return response;
	}

	@POST
	@Path("/workmonths/workdays/tasks/start")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response startTask(StartTaskRB taskRB) {
		Response response;
		TimeLogger timelogger = Ebean.find(TimeLogger.class, TIMELOGGER_ID);
		try {
			Task newTask = timeloggerService.startNewTask(timelogger, taskRB);
			Ebean.save(timelogger);
			response = Response.ok(newTask).build();
		} catch (NotSeparatedTimesException nstex) {
			log.error("Task cannot be add because task has timeconflict with other task.", nstex);
			response = Response.status(Response.Status.CONFLICT).build();
		} catch (WeekendNotEnabledException wneex) {
			log.error("Task cannot be add because the given day is on weekend.", wneex);
			response = Response.status(Response.Status.NOT_ACCEPTABLE).build();
		} catch (Exception ex) {
			response = Response.status(Response.Status.BAD_REQUEST).build();
		}
		return response;
	}

	@GET
	@Path("/workmonths/{year}/{month}")
	@Produces(MediaType.APPLICATION_JSON)
	public Collection<WorkDay> getWorkDays(
		@PathParam(value = "year") @NotNull @Valid int year,
		@PathParam(value = "month") @NotNull @Valid int month) {
		TimeLogger timelogger = Ebean.find(TimeLogger.class, TIMELOGGER_ID);
		return timeloggerService.getWorkDays(timelogger, year, month);
	}

	@GET
	@Path("/workmonths/{year}/{month}/{day}")
	@Produces(MediaType.APPLICATION_JSON)
	public Collection<Task> getTasks(
		@PathParam(value = "year") @NotNull @Valid int year,
		@PathParam(value = "month") @NotNull @Valid int month,
		@PathParam(value = "day") @NotNull @Valid int day) {
		TimeLogger timelogger = Ebean.find(TimeLogger.class, TIMELOGGER_ID);
		return timeloggerService.getTasks(timelogger, year, month, day);
	}

	@PUT
	@Path("/workmonths/workdays/tasks/finish")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response finishTask(FinishingTaskRB taskRB) {
		Response response;
		TimeLogger timelogger = Ebean.find(TimeLogger.class, TIMELOGGER_ID);
		try {
			timeloggerService.finishSpecificTask(timelogger, taskRB);
			Ebean.save(timelogger);
			return Response.ok().build();
		} catch (NotSeparatedTimesException nstex) {
			log.error("Task cannot be finish because task has timeconflict with other task.", nstex);
			response = Response.status(Response.Status.CONFLICT).build();
		} catch (WeekendNotEnabledException wneex) {
			log.error("Task cannot be add because the given day is on weekend.", wneex);
			response = Response.status(Response.Status.NOT_ACCEPTABLE).build();
		} catch (Exception ex) {
			response = Response.status(Response.Status.BAD_REQUEST).build();
		}
		return response;
	}

	@PUT
	@Path("/workmonths/workdays/tasks/modify")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response modifyTask(ModifyTaskRB taskRB) {
		Response response;
		TimeLogger timelogger = Ebean.find(TimeLogger.class, TIMELOGGER_ID);
		try {
			timeloggerService.modifySpecificTask(timelogger, taskRB);
			Ebean.save(timelogger);
			return Response.ok().build();
		} catch (NotSeparatedTimesException nstex) {
			log.error("Task cannot be modify because task has timeconflict with other task.", nstex);
			response = Response.status(Response.Status.CONFLICT).build();
		} catch (WeekendNotEnabledException wneex) {
			log.error("Task cannot be modify because not exists on the given day.", wneex);
			response = Response.status(Response.Status.NOT_ACCEPTABLE).build();
		} catch (Exception ex) {
			response = Response.status(Response.Status.BAD_REQUEST).build();
		}
		return response;
	}

	@PUT
	@Path("/workmonths/workdays/tasks/delete")
	public Response deleteTask(DeleteTaskRB taskRB) {
		Response response;
		TimeLogger timelogger = Ebean.find(TimeLogger.class, TIMELOGGER_ID);
		try {
			timeloggerService.deleteSpecificTask(timelogger, taskRB);
			Ebean.save(timelogger);
			return Response.ok().build();
		} catch (WeekendNotEnabledException wneex) {
			log.error("Task cannot be delete because not exists on the given day.", wneex);
			response = Response.status(Response.Status.NOT_ACCEPTABLE).build();
		} catch (Exception ex) {
			response = Response.status(Response.Status.BAD_REQUEST).build();
		}
		return response;
	}

	@PUT
	@Path("/workmonths/deleteall")
	public void deleteAll() {
		TimeLogger timelogger = Ebean.find(TimeLogger.class, TIMELOGGER_ID);
		timeloggerService.deleteAll(timelogger);
		Ebean.save(timelogger);
	}

	@POST
	@Path("/register-user")
	public Response registerUser(UserRB user) {
		Response response;
		TimeLogger timelogger = Ebean.find(TimeLogger.class).select("name").where().eq("name", user.getName()).findUnique();
		try {
			Ebean.save(timeloggerService.registerUser(timelogger, user));
			response = Response.ok().build();
		} catch (UserExistException uex) {
			log.error("Cannot register user! ", uex);
			response = Response.status(Response.Status.CONFLICT).build();
		} catch (Exception ex) {
			response = Response.status(Response.Status.BAD_REQUEST).build();
		}
		return response;
	}

	@POST
	@Path("/login-user")
	public Response loginUser(UserRB user) {
		Response response;
		try {
			TimeLogger timelogger = Ebean.find(TimeLogger.class).select("name").where().eq("name", user.getName()).findUnique();
			String token = timeloggerService.loginUser(timelogger, user);
			response = Response.ok().header("Authorization", "Bearer " + token).build();
		} catch (AuthenticationException aex) {
			log.error("Cannot login user! ", aex);
			response = Response.status(Response.Status.UNAUTHORIZED).build();
		} catch (Exception ex) {
			response = Response.status(Response.Status.BAD_REQUEST).build();
		}
		return response;
	}

}
