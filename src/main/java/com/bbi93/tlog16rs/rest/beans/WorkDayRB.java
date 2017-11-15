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
public class WorkDayRB {

	private int year;
	private int month;
	private int day;
	private double requiredHours;

	public WorkDayRB(int year, int month, int day) {
		this.year = year;
		this.month = month;
		this.day = day;
		this.requiredHours = 7.5;
	}
}
