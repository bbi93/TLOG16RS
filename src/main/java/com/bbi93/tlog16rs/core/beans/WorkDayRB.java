/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bbi93.tlog16rs.core.beans;

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
