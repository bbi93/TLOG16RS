package com.bbi93.tlog16rs.rest.beans;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *
 * @author bbi93
 */
@Getter
@Setter
@NoArgsConstructor
public class DeleteTaskRB {

	private int year;
	private int month;
	private int day;
	private String taskId;
	private String startTime;

}
