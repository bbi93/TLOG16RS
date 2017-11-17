package com.bbi93.tlog16rs.entities;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import com.bbi93.tlog16rs.exceptions.NotSeparatedTimesException;
import com.bbi93.tlog16rs.utils.Util;
import java.util.Objects;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *
 * @author bbi93
 */
@Getter
@NoArgsConstructor
@Entity
public class WorkDay {

	private static final long DEFAULT_REQUIRED_MIN_PER_DAY = 450;

	@Id
	@GeneratedValue
	int id;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<Task> tasks = new ArrayList<>();
	private LocalDate actualDay;
	@Setter
	private long requiredMinPerDay;
	private long workedTimeOfDay;
	private long extraMinOfDay;

	public WorkDay(LocalDate actualDay, long requiredMinPerDay) {
		this.actualDay = actualDay;
		this.requiredMinPerDay = requiredMinPerDay;
	}

	public WorkDay(LocalDate actualDay) {
		this(actualDay, DEFAULT_REQUIRED_MIN_PER_DAY);
	}

	/**
	 *
	 * @param t Parameter is the specified task object to required to add to workday.
	 * @throws NotSeparatedTimesException On given task's startTime or endTime is in conflict with already added tasks time fields or its intervals.
	 */
	public void addTask(Task t) throws NotSeparatedTimesException {
		if (Util.isSeparatedTime(t, this.getTasks())) {
			tasks.add(t);
			recalculateTimesOfDay();
		} else {
			throw new NotSeparatedTimesException("Task has time conflict with other task in workday.");
		}
	}

	public void removeTask(Task t) {
		tasks.remove(t);
		recalculateTimesOfDay();
	}

	public void recalculateTimesOfDay() {
		tasks.stream().forEach((task) -> {
			task.recalculateWorkedTime();
		});
		calculateWorkedTimeOfDay();
		calculateExtraMinOfDay();
	}

	private void calculateWorkedTimeOfDay() {
		this.workedTimeOfDay = tasks.stream().map((task) -> task.getWorkedTime()).reduce(0L, (accumulator, item) -> accumulator + item);
	}

	private void calculateExtraMinOfDay() {
		this.extraMinOfDay = this.workedTimeOfDay - this.requiredMinPerDay;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 59 * hash + Objects.hashCode(this.actualDay);
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
		final WorkDay other = (WorkDay) obj;
		if (!Objects.equals(this.actualDay, other.actualDay)) {
			return false;
		}
		return true;
	}

}
