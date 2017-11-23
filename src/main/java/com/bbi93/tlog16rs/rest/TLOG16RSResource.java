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
import com.bbi93.tlog16rs.services.JwtService;
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
import javax.ws.rs.HeaderParam;
import javax.ws.rs.NotAuthorizedException;
import lombok.extern.slf4j.Slf4j;
import org.jose4j.jwt.consumer.InvalidJwtException;

@Path("/timelogger")
@Slf4j
public class TLOG16RSResource {

	private static final String TIMELOGGER_NAME = "bbors";
	private static TimeLoggerService timeloggerService = new TimeLoggerService();
	private DbService dbService;

	public TLOG16RSResource(TLOG16RSApplication application) {
		dbService = application.getDbService();
		if (Ebean.find(TimeLogger.class).findRowCount() == 0) {
			Ebean.save(new TimeLogger(TIMELOGGER_NAME));
		}
	}

	@POST
	@Path("/register-user")
	public Response registerUser(UserRB user) {
		Response response;
		try {
			TimeLogger timelogger = Ebean.find(TimeLogger.class).select("name").where().eq("name", user.getName()).findUnique();
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

	@POST
	@Path("/refresh-token")
	public Response refreshToken(@HeaderParam(value = "Authorization") String token) {
		Response response;
		try {
			TimeLogger timelogger = timeloggerService.findTimeLoggerViaToken(token);
			String newToken = timeloggerService.refreshToken(timelogger);
			response = Response.ok().header("Authorization", "Bearer " + newToken).build();
		} catch (NotAuthorizedException naex) {
			log.error("Not authorized user! ", naex);
			response = Response.status(Response.Status.UNAUTHORIZED).build();
		} catch (Exception ex) {
			response = Response.status(Response.Status.BAD_REQUEST).build();
		}
		return response;
	}

	@GET
	@Path("/workmonths")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getWorkMonths(@HeaderParam("Authorization") String token) {
		Response response;
		try {
			TimeLogger timelogger = timeloggerService.findTimeLoggerViaToken(token);
			response = Response.ok(timeloggerService.getWorkMonths(timelogger)).build();
		} catch (InvalidJwtException | NotAuthorizedException ex) {
			log.error("Unauthorized!", ex);
			response = Response.status(Response.Status.UNAUTHORIZED).build();
		}
		return response;
	}

	@POST
	@Path("/workmonths")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response addNewMonth(WorkMonthRB monthRB, @HeaderParam("Authorization") String token) {
		Response response;
		try {
			TimeLogger timelogger = timeloggerService.findTimeLoggerViaToken(token);
			WorkMonth newWorkMonth = timeloggerService.addNewWorkMonth(timelogger, monthRB);
			Ebean.save(timelogger);
			response = Response.ok(newWorkMonth).build();
		} catch (InvalidJwtException | NotAuthorizedException ex) {
			log.error("Not authorized user! ", ex);
			response = Response.status(Response.Status.UNAUTHORIZED).build();
		} catch (Exception ex) {
			response = Response.status(Response.Status.BAD_REQUEST).build();
		}
		return response;
	}

	@POST
	@Path("/workmonths/workdays")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response addNewDay(WorkDayRB dayRB, @HeaderParam("Authorization") String token) {
		Response response;
		try {
			TimeLogger timelogger = timeloggerService.findTimeLoggerViaToken(token);
			WorkDay newWorkDay = timeloggerService.addNewWorkDay(timelogger, dayRB);
			Ebean.save(timelogger);
			response = Response.ok(newWorkDay).build();
		} catch (WeekendNotEnabledException wneex) {
			log.error("Workday cannot be add to given year-month because the given day is on weekend.", wneex);
			response = Response.status(Response.Status.NOT_ACCEPTABLE).build();
		} catch (InvalidJwtException | NotAuthorizedException ex) {
			log.error("Not authorized user! ", ex);
			response = Response.status(Response.Status.UNAUTHORIZED).build();
		} catch (Exception ex) {
			response = Response.status(Response.Status.BAD_REQUEST).build();
		}
		return response;
	}

	@POST
	@Path("/workmonths/workdays/tasks/start")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response startTask(StartTaskRB taskRB, @HeaderParam("Authorization") String token) {
		Response response;
		try {
			TimeLogger timelogger = timeloggerService.findTimeLoggerViaToken(token);
			Task newTask = timeloggerService.startNewTask(timelogger, taskRB);
			Ebean.save(timelogger);
			response = Response.ok(newTask).build();
		} catch (NotSeparatedTimesException nstex) {
			log.error("Task cannot be add because task has timeconflict with other task.", nstex);
			response = Response.status(Response.Status.CONFLICT).build();
		} catch (WeekendNotEnabledException wneex) {
			log.error("Task cannot be add because the given day is on weekend.", wneex);
			response = Response.status(Response.Status.NOT_ACCEPTABLE).build();
		} catch (InvalidJwtException | NotAuthorizedException ex) {
			log.error("Not authorized user! ", ex);
			response = Response.status(Response.Status.UNAUTHORIZED).build();
		} catch (Exception ex) {
			response = Response.status(Response.Status.BAD_REQUEST).build();
		}
		return response;
	}

	@GET
	@Path("/workmonths/{year}/{month}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getWorkDays(
		@PathParam(value = "year") @NotNull @Valid int year,
		@PathParam(value = "month") @NotNull @Valid int month,
		@HeaderParam("Authorization") String token) {
		Response response;
		try {
			TimeLogger timelogger = timeloggerService.findTimeLoggerViaToken(token);
			response = Response.ok(timeloggerService.getWorkDays(timelogger, year, month)).build();
		} catch (InvalidJwtException | NotAuthorizedException ex) {
			log.error("Not authorized user! ", ex);
			response = Response.status(Response.Status.UNAUTHORIZED).build();
		} catch (Exception ex) {
			response = Response.status(Response.Status.BAD_REQUEST).build();
		}
		return response;
	}

	@GET
	@Path("/workmonths/{year}/{month}/{day}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getTasks(
		@PathParam(value = "year") @NotNull @Valid int year,
		@PathParam(value = "month") @NotNull @Valid int month,
		@PathParam(value = "day") @NotNull @Valid int day,
		@HeaderParam("Authorization") String token) {
		Response response;
		try {
			TimeLogger timelogger = timeloggerService.findTimeLoggerViaToken(token);
			response = Response.ok(timeloggerService.getTasks(timelogger, year, month, day)).build();
		} catch (InvalidJwtException | NotAuthorizedException ex) {
			log.error("Not authorized user! ", ex);
			response = Response.status(Response.Status.UNAUTHORIZED).build();
		}
		return response;
	}

	@PUT
	@Path("/workmonths/workdays/tasks/finish")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response finishTask(FinishingTaskRB taskRB, @HeaderParam("Authorization") String token) {
		Response response;
		try {
			TimeLogger timelogger = timeloggerService.findTimeLoggerViaToken(token);
			timeloggerService.finishSpecificTask(timelogger, taskRB);
			Ebean.save(timelogger);
			return Response.ok().build();
		} catch (NotSeparatedTimesException nstex) {
			log.error("Task cannot be finish because task has timeconflict with other task.", nstex);
			response = Response.status(Response.Status.CONFLICT).build();
		} catch (WeekendNotEnabledException wneex) {
			log.error("Task cannot be add because the given day is on weekend.", wneex);
			response = Response.status(Response.Status.NOT_ACCEPTABLE).build();
		} catch (InvalidJwtException | NotAuthorizedException ex) {
			log.error("Not authorized user! ", ex);
			response = Response.status(Response.Status.UNAUTHORIZED).build();
		} catch (Exception ex) {
			response = Response.status(Response.Status.BAD_REQUEST).build();
		}
		return response;
	}

	@PUT
	@Path("/workmonths/workdays/tasks/modify")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response modifyTask(ModifyTaskRB taskRB, @HeaderParam("Authorization") String token) {
		Response response;
		try {
			TimeLogger timelogger = timeloggerService.findTimeLoggerViaToken(token);
			timeloggerService.modifySpecificTask(timelogger, taskRB);
			Ebean.save(timelogger);
			return Response.ok().build();
		} catch (NotSeparatedTimesException nstex) {
			log.error("Task cannot be modify because task has timeconflict with other task.", nstex);
			response = Response.status(Response.Status.CONFLICT).build();
		} catch (WeekendNotEnabledException wneex) {
			log.error("Task cannot be modify because not exists on the given day.", wneex);
			response = Response.status(Response.Status.NOT_ACCEPTABLE).build();
		} catch (InvalidJwtException | NotAuthorizedException ex) {
			log.error("Not authorized user! ", ex);
			response = Response.status(Response.Status.UNAUTHORIZED).build();
		} catch (Exception ex) {
			response = Response.status(Response.Status.BAD_REQUEST).build();
		}
		return response;
	}

	@PUT
	@Path("/workmonths/workdays/tasks/delete")
	public Response deleteTask(DeleteTaskRB taskRB, @HeaderParam("Authorization") String token) {
		Response response;
		try {
			TimeLogger timelogger = timeloggerService.findTimeLoggerViaToken(token);
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
	public Response deleteAll(@HeaderParam("Authorization") String token) {
		Response response;
		try {
			TimeLogger timelogger = timeloggerService.findTimeLoggerViaToken(token);
			timeloggerService.deleteAll(timelogger);
			response = Response.ok().build();
			Ebean.save(timelogger);
		} catch (InvalidJwtException | NotAuthorizedException ex) {
			log.error("Unauthorized!", ex);
			response = Response.status(Response.Status.UNAUTHORIZED).build();
		}
		return response;
	}

}
