package com.bbi93.tlog16rs.entities;

import java.util.LinkedList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *
 * @author bbi93
 */
@Getter
@Entity
@NoArgsConstructor
public class TimeLogger {

	@Id
	@GeneratedValue
	int id;

	@Setter
	private String name;

	public TimeLogger(String name) {
		this.name = name;
	}

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<WorkMonth> months = new LinkedList<>();

	public void addMonth(WorkMonth wm) {
		if (isNewMonth(wm)) {
			months.add(wm);
		}
	}

	public void deleteMonth(WorkMonth wm) {
		months.remove(wm);
	}

	public void deleteMonths() {
		months.removeAll(months);
	}

	public void recalculateTimesOfTimeLogger() {
		months.stream().forEach((month) -> {
			month.recalculateTimesOfMonth();
		});
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
