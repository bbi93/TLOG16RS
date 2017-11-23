package com.bbi93.tlog16rs.services;

import com.avaje.ebean.Ebean;
import com.bbi93.tlog16rs.exceptions.UserExistException;
import com.bbi93.tlog16rs.entities.Task;
import com.bbi93.tlog16rs.entities.TimeLogger;
import com.bbi93.tlog16rs.entities.WorkDay;
import com.bbi93.tlog16rs.entities.WorkMonth;
import com.bbi93.tlog16rs.exceptions.NotSeparatedTimesException;
import com.bbi93.tlog16rs.exceptions.WeekendNotEnabledException;
import com.bbi93.tlog16rs.rest.beans.DeleteTaskRB;
import com.bbi93.tlog16rs.rest.beans.FinishingTaskRB;
import com.bbi93.tlog16rs.rest.beans.ModifyTaskRB;
import com.bbi93.tlog16rs.rest.beans.StartTaskRB;
import com.bbi93.tlog16rs.rest.beans.UserRB;
import com.bbi93.tlog16rs.rest.beans.WorkDayRB;
import com.bbi93.tlog16rs.rest.beans.WorkMonthRB;
import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import javax.naming.AuthenticationException;
import javax.ws.rs.NotAuthorizedException;
import lombok.extern.slf4j.Slf4j;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.lang.JoseException;

/**
 *
 * @author bbi93
 */
@Slf4j
public class TimeLoggerService {

	private JwtService jwtService = new JwtService();

	public Collection<WorkMonth> getWorkMonths(TimeLogger timelogger) {
		return timelogger.getMonths();
	}

	public WorkMonth addNewWorkMonth(TimeLogger timelogger, WorkMonthRB monthRB) {
		WorkMonth workMonth = new WorkMonth(monthRB.getYear(), monthRB.getMonth());
		timelogger.addMonth(workMonth);
		timelogger.recalculateTimesOfTimeLogger();
		return workMonth;
	}

	public WorkDay addNewWorkDay(TimeLogger timelogger, WorkDayRB dayRB) throws WeekendNotEnabledException {
		WorkMonth selectedWorkMonth = selectWorkMonthByYearAndMonthNumber(timelogger, dayRB.getYear(), dayRB.getMonth());
		WorkDay workDay = new WorkDay(LocalDate.of(dayRB.getYear(), dayRB.getMonth(), dayRB.getDay()), Math.round(dayRB.getRequiredHours() * TimeUnit.HOURS.toMinutes(1)));
		selectedWorkMonth.addWorkDay(workDay);
		timelogger.recalculateTimesOfTimeLogger();
		return workDay;
	}

	private WorkMonth selectWorkMonthByYearAndMonthNumber(TimeLogger timelogger, int yearToSearch, int monthToSearch) {
		for (WorkMonth month : timelogger.getMonths()) {
			if (month.getDate().equals(YearMonth.of(yearToSearch, monthToSearch))) {
				return month;
			}
		}
		return addNewWorkMonth(timelogger, new WorkMonthRB(yearToSearch, monthToSearch));
	}

	public Task startNewTask(TimeLogger timelogger, StartTaskRB taskRB) throws NotSeparatedTimesException, WeekendNotEnabledException {
		WorkDay selectedWorkDay = selectWorkDayByYearAndMonthAndDayNumber(timelogger, taskRB.getYear(), taskRB.getMonth(), taskRB.getDay());
		Task task = new Task(taskRB.getTaskId(), LocalTime.parse(taskRB.getStartTime()), taskRB.getComment());
		selectedWorkDay.addTask(task);
		timelogger.recalculateTimesOfTimeLogger();
		return task;
	}

	private WorkDay selectWorkDayByYearAndMonthAndDayNumber(TimeLogger timelogger, int yearToSearch, int monthToSearch, int dayToSearch) throws WeekendNotEnabledException {
		WorkMonth selectedWorkMonth = selectWorkMonthByYearAndMonthNumber(timelogger, yearToSearch, monthToSearch);
		for (WorkDay workDay : selectedWorkMonth.getDays()) {
			if (workDay.getActualDay().isEqual(LocalDate.of(yearToSearch, monthToSearch, dayToSearch))) {
				return workDay;
			}
		}
		return addNewWorkDay(timelogger, new WorkDayRB(yearToSearch, monthToSearch, dayToSearch));
	}

	public Collection<WorkDay> getWorkDays(TimeLogger timelogger, int year, int month) {
		WorkMonth selectedWorkMonth = selectWorkMonthByYearAndMonthNumber(timelogger, year, month);
		return selectedWorkMonth.getDays();
	}

	public Collection<Task> getTasks(TimeLogger timelogger, int year, int month, int day) {
		try {
			return selectWorkDayByYearAndMonthAndDayNumber(timelogger, year, month, day).getTasks();
		} catch (WeekendNotEnabledException ex) {
			log.error("Tasks cannot be found because the given day is not exist.", ex);
		}
		return Collections.emptyList();
	}

	public void finishSpecificTask(TimeLogger timelogger, FinishingTaskRB taskRB) throws WeekendNotEnabledException, NotSeparatedTimesException {
		WorkDay selectedWorkDay = selectWorkDayByYearAndMonthAndDayNumber(timelogger, taskRB.getYear(), taskRB.getMonth(), taskRB.getDay());
		Task selectedTask = selectTaskByWorkDayAndTaskIdandStartTime(selectedWorkDay, taskRB.getTaskId(), taskRB.getStartTime());
		if (selectedTask.equals(new Task())) {
			selectedWorkDay.addTask(new Task(taskRB.getTaskId(), LocalTime.parse(taskRB.getStartTime()), LocalTime.parse(taskRB.getEndTime())));
		} else {
			selectedTask.setEndTime(taskRB.getEndTime());
		}
		timelogger.recalculateTimesOfTimeLogger();
	}

	private Task selectTaskByWorkDayAndTaskIdandStartTime(WorkDay workDay, String taskId, String startTime) {
		for (Task task : workDay.getTasks()) {
			if (task.getTaskId().equals(taskId) && task.getStartTime().equals(LocalTime.parse(startTime))) {
				return task;
			}
		}
		return new Task();
	}

	public void modifySpecificTask(TimeLogger timelogger, ModifyTaskRB taskRB) throws WeekendNotEnabledException, NotSeparatedTimesException {
		WorkDay selectedWorkDay = selectWorkDayByYearAndMonthAndDayNumber(timelogger, taskRB.getYear(), taskRB.getMonth(), taskRB.getDay());
		Task selectedTask = selectTaskByWorkDayAndTaskIdandStartTime(selectedWorkDay, taskRB.getTaskId(), taskRB.getStartTime());
		if (selectedTask.equals(new Task())) {
			Task task = new Task(taskRB.getNewTaskId(), LocalTime.parse(taskRB.getNewStartTime()), LocalTime.parse(taskRB.getNewEndTime()), taskRB.getNewComment());
			selectedWorkDay.addTask(task);
		} else {
			selectedTask.setTaskId(taskRB.getNewTaskId());
			selectedTask.setStartTime(taskRB.getNewStartTime());
			selectedTask.setEndTime(taskRB.getNewEndTime());
			selectedTask.setComment(taskRB.getNewComment());
		}
		timelogger.recalculateTimesOfTimeLogger();
	}

	public void deleteSpecificTask(TimeLogger timelogger, DeleteTaskRB taskRB) throws WeekendNotEnabledException {
		WorkDay selectedWorkDay = selectWorkDayByYearAndMonthAndDayNumber(timelogger, taskRB.getYear(), taskRB.getMonth(), taskRB.getDay());
		Task selectedTask = selectTaskByWorkDayAndTaskIdandStartTime(selectedWorkDay, taskRB.getTaskId(), taskRB.getStartTime());
		if (!selectedTask.equals(new Task())) {
			selectedWorkDay.removeTask(selectedTask);
		}
		timelogger.recalculateTimesOfTimeLogger();
	}

	public void deleteAll(TimeLogger timelogger) {
		timelogger.deleteMonths();
	}

	public TimeLogger registerUser(TimeLogger timelogger, UserRB user) throws UserExistException {
		if (timelogger == null) {
			String salt = jwtService.generateSalt();
			String encodedPassword = jwtService.encodePasswordWithSalt(user.getPassword(), salt);
			return new TimeLogger(user.getName(), encodedPassword, salt);
		} else {
			throw new UserExistException("Selected user already exist!");
		}
	}

	public String loginUser(TimeLogger timelogger, UserRB user) throws UnsupportedEncodingException, JoseException, AuthenticationException {
		if (timelogger != null) {
			String password = jwtService.encodePasswordWithSalt(user.getPassword(), timelogger.getSalt());
			if (password.equals(timelogger.getPassword())) {
				return jwtService.generateJwtToken(timelogger);
			} else {
				throw new AuthenticationException("User not exists");
			}
		} else {
			throw new AuthenticationException("User not exists");
		}
	}

	public TimeLogger findTimeLoggerViaToken(String token) throws InvalidJwtException, NotAuthorizedException {
		if (token != null) {
			String jwt = token.split(" ")[1];
			String timeloggerName = jwtService.getNameFromJwtToken(jwt);
			return Ebean.find(TimeLogger.class).where().eq("name", timeloggerName).findUnique();
		} else {
			throw new NotAuthorizedException("Not existing user");
		}
	}

	public String refreshToken(TimeLogger timelogger) throws UnsupportedEncodingException, JoseException {
		return jwtService.generateJwtToken(timelogger);
	}

}
