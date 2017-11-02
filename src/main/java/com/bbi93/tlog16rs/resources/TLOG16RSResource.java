package com.bbi93.tlog16rs.resources;

import com.bbi93.tlog16rs.core.beans.TimeLogger;
import com.bbi93.tlog16rs.core.beans.WorkMonth;
import com.bbi93.tlog16rs.core.beans.WorkMonthRB;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import java.util.Collection;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Path("/timelogger")
public class TLOG16RSResource {

	ObjectMapper mapper = new ObjectMapper()
		.registerModule(new ParameterNamesModule())
		.registerModule(new Jdk8Module())
		.registerModule(new JavaTimeModule());

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
		WorkMonth workMonth=new WorkMonth(month.getYear(), month.getMonth());
		timelogger.addMonth(workMonth);
		return workMonth;
	}

}
