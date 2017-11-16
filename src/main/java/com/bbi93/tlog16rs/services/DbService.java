package com.bbi93.tlog16rs.services;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.EbeanServerFactory;
import com.avaje.ebean.config.DataSourceConfig;
import com.avaje.ebean.config.ServerConfig;
import com.bbi93.tlog16rs.entities.TestEntity;

/**
 *
 * @author bbi93
 */
public class DbService {

	private EbeanServer ebeanServer;
	private DataSourceConfig dataSourceConfig = new DataSourceConfig();
	private ServerConfig serverConfig = new ServerConfig();

	public DbService() {
		dataSourceConfig.setDriver("org.mariadb.jdbc.Driver");
		dataSourceConfig.setUrl("jdbc:mariadb://127.0.0.1:9001/timelogger");
		dataSourceConfig.setUsername("timelogger");
		dataSourceConfig.setPassword("633Ym2aZ5b9Wtzh4EJc4pANx");

		serverConfig.setName("timelogger");
		serverConfig.setDdlGenerate(true);
		serverConfig.setDdlRun(true);
		serverConfig.setRegister(true);
		serverConfig.setDataSourceConfig(dataSourceConfig);
		serverConfig.addClass(TestEntity.class);
		serverConfig.setDefaultServer(true);

		ebeanServer = EbeanServerFactory.create(serverConfig);
	}
}
