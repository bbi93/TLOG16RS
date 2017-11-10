package com.bbi93.tlog16rs.utils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import com.bbi93.tlog16rs.entities.Task;
import com.bbi93.tlog16rs.entities.WorkDay;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Comparator;

/**
 *
 * @author bbi93
 */
public class Util {

	private static final int QUARTER_HOUR_IN_MINUTES = 15;

	/**
	 * Calculate elapsed time in given TemporalUnit between two LocalTime object
	 *
	 * @param startTime
	 * @param endTime
	 * @param temporalUnit
	 * @return long value of time difference in given temporal unit
	 */
	public static long calculateTimeDifference(LocalTime startTime, LocalTime endTime, TemporalUnit temporalUnit) {
		return startTime.until(endTime, temporalUnit);
	}

	/**
	 * Calculate elapsed time in given TemporalUnit between two LocalDate object
	 *
	 * @param startDate
	 * @param endDate
	 * @param temporalUnit
	 * @return long value of date difference in given temporal unit
	 */
	public static long calculateDateDifference(LocalDate startDate, LocalDate endDate, TemporalUnit temporalUnit) {
		return startDate.until(endDate, temporalUnit);
	}

	/**
	 * This method rounds given endtime to quarter hour.
	 *
	 * @param startTime LocalTime of task's start time.
	 * @param endTime LocalTime of task's end time. This will be rounded.
	 * @return LocalTime Returns the new rounded endTime.
	 */
	public static LocalTime roundToMultipleQuarterHour(LocalTime startTime, LocalTime endTime) {
		long mod = calculateTimeDifference(startTime, endTime, ChronoUnit.MINUTES) % QUARTER_HOUR_IN_MINUTES;
		return mod < (Math.round(QUARTER_HOUR_IN_MINUTES / 2)) ? endTime.minusMinutes(mod) : endTime.plusMinutes(QUARTER_HOUR_IN_MINUTES - mod);
	}

	/**
	 * This method check t task param has unique and conflict-free time values.
	 *
	 * @param t Task to check.
	 * @param tasks Task list where search for same values like t task.
	 * @return boolean Returns true if t task is conflict-free.
	 */
	public static boolean isSeparatedTime(Task t, Collection<Task> tasks) {
		for (Task task : tasks) {
			//if task starts when other task starts
			if (t.getStartTime().equals(task.getStartTime())) {
				return false;
			}
			//if task ends when other task ends
			if (t.getEndTime().equals(task.getEndTime()) && (!task.getStartTime().equals(task.getEndTime()) && !t.getStartTime().equals(t.getEndTime()))) {
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
	 * Check given localdate parameter is a weekday.
	 *
	 * @param actualDay The date to check.
	 * @return boolean Returns true if localdate DAY_OF_WEEK value not equals DayOfWeek.SATURDAY or DayOfWeek.SUNDAY values.
	 */
	public static boolean isWeekday(LocalDate actualDay) {
		return actualDay.get(ChronoField.DAY_OF_WEEK) <= DayOfWeek.FRIDAY.getValue();
	}

	/**
	 *
	 * @param taskMinutes
	 * @return boolean Returns true, if long parameter MOD QUARTER_HOUR_IN_MINUTES is zero.
	 */
	public static boolean isMultipleQuarterHour(long taskMinutes) {
		return taskMinutes % QUARTER_HOUR_IN_MINUTES == 0;
	}

	/**
	 * Calculate the interval of the params in minutes and check the long value can be divide with 15 without remainder.
	 *
	 * @param startTime
	 * @param endTime
	 * @return Returns true, if long parameter MOD QUARTER_HOUR_IN_MINUTES is zero.
	 */
	public static boolean isMultipleQuarterHour(LocalTime startTime, LocalTime endTime) {
		return isMultipleQuarterHour(calculateTimeDifference(startTime, endTime, ChronoUnit.MINUTES));
	}

	/**
	 *
	 * @param day
	 * @return Task This method returns the task which is the last of the workday.
	 */
	public static Task getLatestTaskOfDay(WorkDay day) {
		Comparator<Task> endTimeComparator = Comparator.comparing(Task::getEndTime);
		return day.getTasks().stream().max(endTimeComparator).get();
	}

	/**
	 *
	 * @param day
	 * @return LocalTime Returns the finish time of the last task of workday.
	 */
	public LocalTime endTimeOfTheLastTask(WorkDay day) {
		return getLatestTaskOfDay(day).getEndTime();
	}

}
