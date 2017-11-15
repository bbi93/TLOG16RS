package com.bbi93.tlog16rs.entities;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import com.bbi93.tlog16rs.exceptions.WeekendNotEnabledException;
import com.bbi93.tlog16rs.utils.Util;
import java.util.Objects;
import lombok.Setter;

/**
 *
 * @author bbi93
 */
@Getter
public class WorkMonth {

	private List<WorkDay> days = new ArrayList<>();
	private YearMonth date;
	private long requiredMinPerMonth;
	private long workedTimeOfMonth;
	private long extraMinOfMonth;
	@Setter
	private boolean isWeekendEnabled;

	public WorkMonth(int year, int month) {
		this.date = YearMonth.of(year, month);
		this.isWeekendEnabled = false;
	}

	public WorkMonth(int year, int month, boolean isWeekendEnabled) {
		this.date = YearMonth.of(year, month);
		this.isWeekendEnabled = isWeekendEnabled;
	}

	/**
	 *
	 * @param wd Workday to add.
	 * @throws WeekendNotEnabledException If isWeekendEnabled is false and the given workday is on weekend.
	 */
	public void addWorkDay(WorkDay wd) throws WeekendNotEnabledException {
		if (!days.contains(wd)) {
			if (date.equals(YearMonth.from(wd.getActualDay()))) {
				if ((!Util.isWeekday(wd.getActualDay()) && isWeekendEnabled) || Util.isWeekday(wd.getActualDay())) {
					days.add(wd);
					recalculateTimesOfMonth();
				} else {
					throw new WeekendNotEnabledException("Given workday is on weekend, but it's not enabled.");
				}
			}
		}
	}

	public void removeDay(WorkDay wd) {
		days.remove(wd);
		recalculateTimesOfMonth();
	}

	public void recalculateTimesOfMonth() {
		days.stream().forEach((day) -> {
			day.recalculateTimesOfDay();
		});
		calculateRequiredMinPerMonth();
		calculateWorkedTimeOfMonth();
		calculateExtraMinOfMonth();
	}

	private void calculateRequiredMinPerMonth() {
		this.requiredMinPerMonth = days.stream().map((day) -> day.getRequiredMinPerDay()).reduce(0L, (accumulator, item) -> accumulator + item);
	}

	private void calculateWorkedTimeOfMonth() {
		this.workedTimeOfMonth = days.stream().map((day) -> day.getWorkedTimeOfDay()).reduce(0L, (accumulator, item) -> accumulator + item);
	}

	private void calculateExtraMinOfMonth() {
		this.extraMinOfMonth = days.stream().map((day) -> day.getExtraMinOfDay()).reduce(0L, (accumulator, item) -> accumulator + item);
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 37 * hash + Objects.hashCode(this.date);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final WorkMonth other = (WorkMonth) obj;
		if (!Objects.equals(this.date, other.date)) {
			return false;
		}
		return true;
	}

}
