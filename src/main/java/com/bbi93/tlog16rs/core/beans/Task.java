package com.bbi93.tlog16rs.core.beans;

import com.bbi93.tlog16rs.core.exceptions.NoTaskIdException;
import com.bbi93.tlog16rs.core.exceptions.InvalidTaskIdException;
import com.bbi93.tlog16rs.core.exceptions.ImpossibleTimeException;
import com.bbi93.tlog16rs.core.exceptions.NotExpectedTimeOrderException;
import java.time.LocalTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.bbi93.tlog16rs.core.exceptions.EmptyTimeFieldException;
import com.bbi93.tlog16rs.core.utils.Util;
import lombok.Setter;

/**
 *
 * @author bbi93
 */
@Getter
@Setter
@NoArgsConstructor
public class Task {

	private String taskId;
	private LocalTime startTime;
	private LocalTime endTime;
	private String comment;

	@Override
	public String toString() {
		return "taskId=" + taskId + ", startTime=" + startTime + ", endTime=" + endTime + ", comment=" + comment;
	}

	/**
	 *
	 * @return long Elapsed time in minutes between startTime and endTime
	 * @throws EmptyTimeFieldException On task's one of both time field is not setted.
	 */
	public long calculateMinPerTask() throws EmptyTimeFieldException {
		long startTimeInMinutes = (this.getStartTime().getHour() * 60) + (this.getStartTime().getMinute());
		long endTimeInMinutes = (this.getEndTime().getHour() * 60) + (this.getEndTime().getMinute());
		return endTimeInMinutes - startTimeInMinutes;
	}

	/**
	 *
	 * @return boolean Returns true, if taskId matches pattern.
	 */
	private boolean isValidLTTaskId() {
		Matcher matcher = Pattern.compile("LT-\\d{4}").matcher(taskId);
		if (matcher.find()) {
			if ((matcher.start() == 0) && (matcher.end() == taskId.length())) {
				return true;
			}
		}
		return false;
	}

	/**
	 *
	 * @return boolean Returns true, if taskId matches pattern.
	 */
	private boolean isValidRedmineTaskId() {
		Matcher matcher = Pattern.compile("\\d{4}").matcher(taskId);
		if (matcher.find()) {
			if ((matcher.start() == 0) && (matcher.end() == taskId.length())) {
				return true;
			}
		}
		return false;
	}

	/**
	 *
	 * @return boolean Returns true, if isValidLTTaskId or isValidRedmineTaskId is true.
	 */
	public boolean isValidTaskId() {
		if (isValidLTTaskId() || isValidRedmineTaskId()) {
			return true;
		}
		return false;
	}

	public void setTaskId(String taskId) throws InvalidTaskIdException, NoTaskIdException {
		this.taskId = taskId;
		if (this.taskId != null) {
			if (this.taskId.isEmpty()) {
				throw new NoTaskIdException("Task's id cannot be empty.");
			} else if (isValidTaskId()) {
			} else {
				throw new InvalidTaskIdException("Task id is invalid.");
			}
		} else {
			throw new NoTaskIdException("Task's id cannot be null.");
		}

	}

	private void setStartTime(LocalTime startTime) {
		this.startTime = startTime;
		this.endTime=startTime;
	}

	public void setStartTime(int hour, int min) throws EmptyTimeFieldException, ImpossibleTimeException {
		String startTimeString = formatStringFromIntegerTime(hour, min);
		this.setStartTime(startTimeString);
	}

	public void setStartTime(String startTime) throws EmptyTimeFieldException {
		if (!startTime.isEmpty()) {
			this.setStartTime(LocalTime.parse(startTime));
		} else {
			throw new EmptyTimeFieldException("Start time field cannot be empty.");
		}
	}

	private void setEndTime(LocalTime endTime) throws EmptyTimeFieldException {
		this.endTime = Util.roundToMultipleQuarterHour(this.getStartTime(), endTime);
	}

	public void setEndTime(int hour, int min) throws NotExpectedTimeOrderException, EmptyTimeFieldException, ImpossibleTimeException {
		String endTimeString = formatStringFromIntegerTime(hour, min);
		this.setEndTime(endTimeString);
	}

	public void setEndTime(String endTimeString) throws NotExpectedTimeOrderException, EmptyTimeFieldException {
		if (!endTimeString.isEmpty()) {
			LocalTime endTimeLocal = LocalTime.parse(endTimeString);
			if (endTimeLocal.isAfter(this.getStartTime())) {
				this.setEndTime(endTimeLocal);
			} else {
				throw new NotExpectedTimeOrderException("Task cannot be ended before it starts.");
			}
		} else {
			throw new EmptyTimeFieldException("End time field cannot be empty.");
		}
	}

	public void setComment(String comment) {
		if (comment == null || comment.isEmpty()) {
			this.comment = "";
		} else {
			this.comment = comment;
		}
	}

	/**
	 *
	 * @param hour
	 * @param min
	 * @return String Returns "hh:mm" formatted string from hour and minute integer
	 * @throws ImpossibleTimeException Throws exception, when hour is not 0-23, or minute is not 0-59
	 */
	private String formatStringFromIntegerTime(int hour, int min) throws ImpossibleTimeException {
		if (hour >= 0 && hour < 24 && min >= 0 && min < 60) {
			StringBuilder sb = new StringBuilder(5);
			if (hour < 10) {
				sb.append("0");
			}
			sb.append(hour);
			sb.append(":");
			if (min < 10) {
				sb.append("0");
			}
			sb.append(min);
			return sb.toString();
		} else {
			throw new ImpossibleTimeException("Bad integer value for hour or min.");
		}
	}

	public LocalTime getStartTime() throws EmptyTimeFieldException {
		if (this.startTime != null) {
			return this.startTime;
		} else {
			throw new EmptyTimeFieldException("Starttime not setted yet.");
		}
	}

	public LocalTime getEndTime(){
		return this.endTime;
	}

}
