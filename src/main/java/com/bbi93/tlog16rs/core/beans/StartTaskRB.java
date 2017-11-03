/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bbi93.tlog16rs.core.beans;

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
public class StartTaskRB {

	private int year;
	private int month;
	private int day;
	private String taskId;
	private String startTime;
	private String comment;

}
