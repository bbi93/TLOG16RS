package com.bbi93.tlog16rs.core;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Greeting {

	@JsonProperty
	private String greeting;

	public Greeting() {
	}

	public Greeting(String greeting) {
		this.greeting = greeting;
	}

	public String getGreeting() {
		return greeting;
	}
}
