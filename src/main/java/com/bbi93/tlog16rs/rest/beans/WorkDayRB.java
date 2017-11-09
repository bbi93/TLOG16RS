package com.bbi93.tlog16rs.rest.beans;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author bbi93
 */
@NoArgsConstructor
@Data
public class WorkDayRB {

	private int year;
	private int month;
	private int day;
	private int requiredHours;
}
