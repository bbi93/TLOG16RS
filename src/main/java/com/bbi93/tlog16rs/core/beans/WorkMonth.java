package com.bbi93.tlog16rs.core.beans;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import com.bbi93.tlog16rs.core.exceptions.EmptyTimeFieldException;
import com.bbi93.tlog16rs.core.exceptions.WeekendNotEnabledException;
import com.bbi93.tlog16rs.core.utils.Util;

/**
 *
 * @author bbi93
 */
@Getter
public class WorkMonth {

	private List<WorkDay> days = new ArrayList<>();
	private YearMonth date;
	private long sumPerMonth;
	private long requiredMinPerMonth=450;
	private boolean isWeekendEnabled;

	public WorkMonth(int year, int month) {
		this.date = YearMonth.of(year, month);
		this.isWeekendEnabled = false;
	}

	public WorkMonth(int year, int month, boolean isWeekendEnabled) {
		this.date = YearMonth.of(year, month);
		this.isWeekendEnabled = isWeekendEnabled;
	}

	public long getExtraMinPerMonth() throws EmptyTimeFieldException {
		long extramin = 0;
		for (WorkDay day : days) {
			extramin += day.getExtraMinPerDay();
		}
		return extramin;
	}

	/**
	 *
	 * @return long Returns with sum of all task's elapsed time in minutes in all workday.
	 * @throws EmptyTimeFieldException On actual workDay's task list has task which has unsetted time field.
	 */
	public long getSumPerMonth() throws EmptyTimeFieldException {
		sumPerMonth = 0;
		for (WorkDay day : days) {
			sumPerMonth += day.getSumPerDay();
		}
		return sumPerMonth;
	}

	public long getRequiredMinPerMonth() {
		requiredMinPerMonth = 0;
		for (WorkDay day : days) {
			requiredMinPerMonth += day.getRequiredMinPerDay();
		}
		return requiredMinPerMonth;
	}

	/**
	 *
	 * @param wd Workday to check.
	 * @return boolean Return true if workday list not contains workday which has conflict date conflict.
	 */
	public boolean isNewDate(WorkDay wd) {
		return days.stream().filter(d -> d.getActualDay().isEqual(wd.getActualDay())).count() == 0;
	}

	/**
	 *
	 * @param wd Workday to check.
	 * @return boolean Return true if workday month value equals with the workmonth's month value.
	 */
	public boolean isSameMonth(WorkDay wd) {
		return date.getMonth() == wd.getActualDay().getMonth();
	}

	/**
	 *
	 * @param wd Workday to add.
	 * @throws WeekendNotEnabledException If isWeekendEnabled is false and the given workday is on weekend.
	 */
	public void addWorkDay(WorkDay wd) throws WeekendNotEnabledException {
		if (!days.contains(wd)) {
			if (isSameMonth(wd)) {
				if ((!Util.isWeekday(wd.getActualDay()) && isWeekendEnabled) || Util.isWeekday(wd.getActualDay())) {
					days.add(wd);
				} else {
					throw new WeekendNotEnabledException("Given workday is on weekend, but it's not");
				}
			}
		}
	}
}
