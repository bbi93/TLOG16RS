package com.bbi93.tlog16rs.application;

import io.dropwizard.Configuration;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.NotEmpty;

@Getter
@Setter
@ToString
public class TLOG16RSConfiguration extends Configuration {

	@NotEmpty
	protected String dbDriver;
	@NotEmpty
	protected String dbUrl;
	@NotEmpty
	protected String dbUsername;
	@NotEmpty
	protected String dbPassword;
	@NotEmpty
	protected String dbName;
}
