package com.bbi93.tlog16rs.core.beans;

import java.util.LinkedList;
import java.util.List;
import lombok.Data;

/**
 *
 * @author bbi93
 */
@Data
public class TimeLogger {

	private List<WorkMonth> months = new LinkedList<>();

	public void addMonth(WorkMonth wm) {
		if (isNewMonth(wm)) {
			months.add(wm);
		}
	}

	/**
	 *
	 * @param wm Workmonth to check.
	 * @return boolean Return true, if workmonth list not contains workmonth with same value date field.
	 */
	public boolean isNewMonth(WorkMonth wm) {
		return months.stream().filter(month -> month.getDate().equals(wm.getDate())).count() == 0;
	}
}
