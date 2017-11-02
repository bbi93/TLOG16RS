package com.bbi93.tlog16rs.resources;

import com.bbi93.tlog16rs.core.beans.TimeLogger;
import com.bbi93.tlog16rs.core.beans.WorkDay;
import com.bbi93.tlog16rs.core.beans.WorkMonth;
import com.bbi93.tlog16rs.core.beans.WorkMonthRB;
import com.bbi93.tlog16rs.core.beans.WorkDayRB;
import com.bbi93.tlog16rs.core.exceptions.FutureWorkException;
import com.bbi93.tlog16rs.core.exceptions.NegativeMinutesOfWorkException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import java.time.DateTimeException;
import java.util.Collection;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
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

}
