package com.bbi93.tlog16rs.core.utils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import com.bbi93.tlog16rs.core.beans.Task;
import com.bbi93.tlog16rs.core.beans.WorkDay;
import com.bbi93.tlog16rs.core.exceptions.EmptyTimeFieldException;
import com.bbi93.tlog16rs.core.exceptions.NoTaskDeclaredException;
import com.bbi93.tlog16rs.core.exceptions.NotExpectedTimeOrderException;
import java.util.List;

/**
 *
 * @author bbi93
 */
public class Util {

	/**
	 * This method rounds given endtine to quarter hour.
	 *
	 * @param startTime LocalTime of task's start time.
	 * @param endTime LocalTime of task's end time. This will be rounded.
	 * @return LocalTime Returns the new rounded endTime.
	 */
	public static LocalTime roundToMultipleQuarterHour(LocalTime startTime, LocalTime endTime) {
		int timeDiffInMinutes = (endTime.toSecondOfDay() / 60) - (startTime.toSecondOfDay() / 60);
		if (!isMultipleQuarterHour(timeDiffInMinutes)) {
			long mod = timeDiffInMinutes % 15;
			if (mod < 8) {
				endTime = endTime.minusMinutes(mod);
			} else {
				endTime = endTime.plusMinutes(15 - mod);
			}
		}
		return endTime;
	}

	/**
	 * This method check t task param has unique and conflict-free time values.
	 *
	 * @param t Task to check.
	 * @param tasks Task list where search for same values like t task.
	 * @return boolean Returns true if t task is conflict-free.
	 * @throws EmptyTimeFieldException On any task has unsetted time field.
	 */
	public static boolean isSeparatedTime(Task t, Collection<Task> tasks) throws EmptyTimeFieldException {
		for (Task task : tasks) {
			//if task starts when other task starts
			if (t.getStartTime().equals(task.getStartTime())) {
				return false;
			}
			//if task ends when other task ends
			if (t.getEndTime().equals(task.getEndTime())) {
				return false;
			}
			//if starttime inside other task
			if (t.getStartTime().isAfter(task.getStartTime()) && t.getStartTime().isBefore(task.getEndTime())) {
				return false;
			}
			//if endtime inside other task
			if (t.getEndTime().isAfter(task.getStartTime()) && t.getEndTime().isBefore(task.getEndTime())) {
				return false;
			}
			//if task is around of other task
			if (t.getStartTime().isBefore(task.getStartTime()) && t.getEndTime().isAfter(task.getEndTime())) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Check given localdate parameter is a weekend.
	 *
	 * @param actualDay The date to check.
	 * @return boolean Returns true if localdate parameter equals DayOfWeek.SATURDAY or DayOfWeek.SUNDAY enums.
	 */
	public static boolean isWeekday(LocalDate actualDay) {
		boolean notSaturday = actualDay.getDayOfWeek() != DayOfWeek.SATURDAY;
		boolean notSunday = actualDay.getDayOfWeek() != DayOfWeek.SUNDAY;
		return notSaturday && notSunday;
	}

	/**
	 *
	 * @param taskMinutes
	 * @return boolean Returns true, if long parameter MOD 15 is zero.
	 */
	public static boolean isMultipleQuarterHour(long taskMinutes) {
		return taskMinutes % 15 == 0;
	}

	/**
	 * Calculate the interval of the params in minutes and check the long value can be divide with 15 without remainder.
	 *
	 * @param startTime
	 * @param endTime
	 * @return Returns true, if long parameter MOD 15 is zero.
	 * @throws NotExpectedTimeOrderException On end time is before start time.
	 * @throws EmptyTimeFieldException On some of time fields is unsetted.
	 */
	public static boolean isMultipleQuarterHour(LocalTime startTime, LocalTime endTime) throws NotExpectedTimeOrderException, EmptyTimeFieldException {
		if (startTime == null || endTime == null) {
			throw new EmptyTimeFieldException("Some time field is not setted.");
		} else if (startTime.isBefore(endTime)) {
			int taskMinutes = (endTime.toSecondOfDay() / 60) - (startTime.toSecondOfDay() / 60);
			return Util.isMultipleQuarterHour(taskMinutes);
		} else {
			throw new NotExpectedTimeOrderException("Bad time order.");
		}
	}

	/**
	 *
	 * @return Task This method returns the task which is the last of the workday.
	 * @throws NoTaskDeclaredException On task list is empty.
	 */
	public static Task getLatestTaskOfDay(WorkDay day) throws NoTaskDeclaredException, EmptyTimeFieldException {
		List<Task> tasksInDay = day.getTasks();
		if (tasksInDay.size() > 0) {
			Task lastTask = tasksInDay.get(0);
			for (Task task : tasksInDay) {
				if (task.getEndTime().isAfter(lastTask.getEndTime())) {
					lastTask = task;
				}
			}
			return lastTask;
		}
		throw new NoTaskDeclaredException("No task in this workday.");
	}

	/**
	 *
	 * @return LocalTime Returns the finish time of the last task of workday.
	 * @throws EmptyTimeFieldException On task list has task which has unsetted time field.
	 * @throws NoTaskDeclaredException On task list is empty.
	 */
	public LocalTime endTimeOfTheLastTask(WorkDay day) throws EmptyTimeFieldException, NoTaskDeclaredException {
		Task task = getLatestTaskOfDay(day);
		return task.getEndTime();
	}

}
