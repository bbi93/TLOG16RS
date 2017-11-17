package com.bbi93.tlog16rs.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.bbi93.tlog16rs.utils.Util;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.EqualsAndHashCode;

/**
 *
 * @author bbi93
 */
@Getter
@NoArgsConstructor
@EqualsAndHashCode
@Entity
public class Task {

	private static final String DEFAULT_COMMENT = "";

	@Id
	@GeneratedValue
	int id;

	@Setter
	private String taskId;
	private LocalTime startTime = LocalTime.MIN;
	private LocalTime endTime = LocalTime.MAX;
	@Setter
	private String comment;
	private long workedTime;

	public Task(String taskId, LocalTime startTime) {
		this(taskId, startTime, DEFAULT_COMMENT);
	}

	public Task(String taskId, LocalTime startTime, String comment) {
		this(taskId, startTime, LocalTime.MAX, comment);
	}

	public Task(String taskId, LocalTime startTime, LocalTime endTime) {
		this(taskId, startTime, endTime, DEFAULT_COMMENT);
	}

	public Task(String taskId, LocalTime startTime, LocalTime endTime, String comment) {
		this.taskId = taskId;
		this.startTime = startTime;
		this.endTime = endTime;
		this.comment = comment;
	}

	public void setStartTime(LocalTime startTime) {
		this.startTime = startTime;
		recalculateWorkedTime();
	}

	public void setStartTime(int hour, int min) {
		this.setStartTime(LocalTime.of(hour, min));
	}

	public void setStartTime(String startTimeString) {
		this.setStartTime(LocalTime.parse(startTimeString));
	}

	public void setEndTime(LocalTime endTime) {
		this.endTime = Util.roundToMultipleQuarterHour(this.getStartTime(), endTime);
		recalculateWorkedTime();
	}

	public void setEndTime(int hour, int min) {
		this.setEndTime(LocalTime.of(hour, min));
	}

	public void setEndTime(String endTimeString) {
		this.setEndTime(LocalTime.parse(endTimeString));
	}

	public void recalculateWorkedTime() {
		workedTime = Util.calculateTimeDifference(startTime, endTime, ChronoUnit.MINUTES);
	}

}
