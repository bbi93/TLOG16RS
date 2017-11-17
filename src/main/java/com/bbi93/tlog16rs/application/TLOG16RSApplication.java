package com.bbi93.tlog16rs.application;

import com.bbi93.tlog16rs.rest.TLOG16RSResource;
import com.bbi93.tlog16rs.services.DbService;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import lombok.Getter;

public class TLOG16RSApplication extends Application<TLOG16RSConfiguration> {

	@Getter
	private DbService dbService;

	public static void main(final String[] args) throws Exception {
		new TLOG16RSApplication().run(args);
	}

	@Override
	public String getName() {
		return "TLOG16RS";
	}

	@Override
	public void initialize(final Bootstrap<TLOG16RSConfiguration> bootstrap) {
		// TODO: application initialization
	}

	@Override
	public void run(final TLOG16RSConfiguration configuration, final Environment environment) {
		dbService = new DbService(configuration);
		environment.jersey().register(new TLOG16RSResource(this));
	}

}
