package com.bbi93.tlog16rs.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Setter;
import com.bbi93.tlog16rs.utils.Util;
import java.time.LocalTime;
import lombok.EqualsAndHashCode;

/**
 *
 * @author bbi93
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Task {

	private static final String DEFAULT_COMMENT = "";

	@Setter
	private String taskId;
	private LocalTime startTime = LocalTime.MIDNIGHT;
	private LocalTime endTime = LocalTime.MIDNIGHT;
	@Setter
	private String comment;

	public Task(String taskId, LocalTime startTime) {
		this(taskId, startTime, DEFAULT_COMMENT);
	}

	public Task(String taskId, LocalTime startTime, String comment) {
		this(taskId, startTime, LocalTime.MIDNIGHT, comment);
	}
	public Task(String taskId, LocalTime startTime, LocalTime endTime) {
		this(taskId, startTime, endTime, DEFAULT_COMMENT);
	}

	public void setStartTime(LocalTime startTime) {
		this.startTime = startTime;
	}

	public void setStartTime(int hour, int min) {
		this.setStartTime(LocalTime.of(hour, min));
	}

	public void setStartTime(String startTimeString) {
		this.setStartTime(LocalTime.parse(startTimeString));
	}

	public void setEndTime(LocalTime endTime) {
		this.endTime = Util.roundToMultipleQuarterHour(this.getStartTime(), endTime);
	}

	public void setEndTime(int hour, int min) {
		this.setEndTime(LocalTime.of(hour, min));
	}

	public void setEndTime(String endTimeString) {
		this.setEndTime(LocalTime.parse(endTimeString));
	}

}
