package com.bbi93.tlog16rs.services;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.EbeanServerFactory;
import com.avaje.ebean.config.DataSourceConfig;
import com.avaje.ebean.config.ServerConfig;
import com.bbi93.tlog16rs.application.TLOG16RSConfiguration;
import com.bbi93.tlog16rs.entities.Task;
import com.bbi93.tlog16rs.entities.TimeLogger;
import com.bbi93.tlog16rs.entities.WorkDay;
import com.bbi93.tlog16rs.entities.WorkMonth;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.resource.FileSystemResourceAccessor;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author bbi93
 */
@Slf4j
public class DbService {

	private EbeanServer ebeanServer;
	private DataSourceConfig dataSourceConfig = new DataSourceConfig();
	private ServerConfig serverConfig = new ServerConfig();
	private final String LIQUIBASE_MIGRATION_FILE_PATH = "/liquibaseMigrations/migrations.xml";

	public DbService(TLOG16RSConfiguration configuration) {
		initDataSourceConfig(configuration);
		initServerConfig(configuration);
		ebeanServer = EbeanServerFactory.create(serverConfig);
		updateSchema();
	}

	private void updateSchema() {
		try {
			Connection connection = DriverManager.getConnection(dataSourceConfig.getUrl(), dataSourceConfig.getUsername(), dataSourceConfig.getPassword());
			Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
			Liquibase liquibase = new Liquibase(this.getClass().getResource(LIQUIBASE_MIGRATION_FILE_PATH).getPath(), new FileSystemResourceAccessor(), database);
			liquibase.update(new Contexts());
		} catch (SQLException ex) {
			Logger.getLogger(DbService.class.getName()).log(Level.SEVERE, null, ex);
		} catch (DatabaseException ex) {
			Logger.getLogger(DbService.class.getName()).log(Level.SEVERE, null, ex);
		} catch (LiquibaseException ex) {
			Logger.getLogger(DbService.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private void initDataSourceConfig(TLOG16RSConfiguration configuration) {
		dataSourceConfig.setDriver(configuration.getDbDriver());
		dataSourceConfig.setUrl(configuration.getDbUrl());
		dataSourceConfig.setUsername(configuration.getDbUsername());
		dataSourceConfig.setPassword(configuration.getDbPassword());
	}

	private void initServerConfig(TLOG16RSConfiguration configuration) {
		serverConfig.setName(configuration.getDbName());
		serverConfig.setDdlGenerate(false);
		serverConfig.setDdlRun(false);
		serverConfig.setRegister(true);
		serverConfig.setDataSourceConfig(dataSourceConfig);
		serverConfig.addClass(Task.class);
		serverConfig.addClass(WorkDay.class);
		serverConfig.addClass(WorkMonth.class);
		serverConfig.addClass(TimeLogger.class);
		serverConfig.setDefaultServer(true);
	}
}
