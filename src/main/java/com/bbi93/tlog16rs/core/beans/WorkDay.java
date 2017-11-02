package com.bbi93.tlog16rs.core.beans;

import com.bbi93.tlog16rs.core.exceptions.FutureWorkException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import com.bbi93.tlog16rs.core.exceptions.EmptyTimeFieldException;
import com.bbi93.tlog16rs.core.exceptions.NegativeMinutesOfWorkException;
import com.bbi93.tlog16rs.core.exceptions.NotSeparatedTimesException;
import com.bbi93.tlog16rs.core.utils.Util;
import lombok.NoArgsConstructor;

/**
 *
 * @author bbi93
 */
@Getter
@NoArgsConstructor
public class WorkDay {

	private static final long DEFAULT_REQUIRED_MIN_PER_DAY = 450;

	private List<Task> tasks = new ArrayList<>();
	private long requiredMinPerDay=DEFAULT_REQUIRED_MIN_PER_DAY;
	private LocalDate actualDay;
	private long sumPerDay;

	/**
	 *
	 * @return long Returns with sum of all tasks elapsed time in minutes.
	 * @throws EmptyTimeFieldException On some task one or both time field is empty.
	 */
	public long getSumPerDay() throws EmptyTimeFieldException {
		long daySum = 0;
		for (Task task : tasks) {
			try {
				daySum += task.getMinPerTask();
			} catch (EmptyTimeFieldException ex) {
				throw new EmptyTimeFieldException(task.getTaskId() + " (" + task.getStartTime() + ") task has unsetted time fields.");
			}
		}
		return daySum;
	}

	public void setRequiredMinPerDay(long requiredMinPerDay) throws NegativeMinutesOfWorkException {
		if (requiredMinPerDay >= 0) {
			this.requiredMinPerDay = requiredMinPerDay;
		} else {
			throw new NegativeMinutesOfWorkException("Required minutes cannot be negative.");
		}
	}

	public void setActualDay(LocalDate actualDay) throws FutureWorkException {
		if (LocalDate.now().isBefore(actualDay)) {
			throw new FutureWorkException("");
		} else {
			this.actualDay = actualDay;
		}
	}

	public void setActualDay(int year, int month, int day) throws DateTimeException, FutureWorkException {
		this.setActualDay(LocalDate.of(year, month, day));
	}

	public long getExtraMinPerDay() throws EmptyTimeFieldException {
		return getSumPerDay() - getRequiredMinPerDay();
	}

	/**
	 *
	 * @param t Parameter is the specified task object to required to add to workday.
	 * @throws NotSeparatedTimesException On given task's startTime or endTime is in conflict with already added tasks time fields or its intervals.
	 * @throws EmptyTimeFieldException On given task's one of both time field is not setted.
	 */
	public void addTask(Task t) throws NotSeparatedTimesException, EmptyTimeFieldException {
		if (Util.isSeparatedTime(t, this.getTasks())) {
			tasks.add(t);
		} else {
			throw new NotSeparatedTimesException("Task has time conflict with other task in workday.");
		}
	}

	public String toStatistics() throws EmptyTimeFieldException {
		return actualDay + " Task number:" + tasks.size() + " Logged time:" + getSumPerDay() + " Extra time:" + getExtraMinPerDay() + ".";
	}

}
