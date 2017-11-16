package com.bbi93.tlog16rs.services;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.EbeanServerFactory;
import com.avaje.ebean.config.DataSourceConfig;
import com.avaje.ebean.config.ServerConfig;
import com.bbi93.tlog16rs.entities.TestEntity;
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
		serverConfig.setDdlGenerate(false);
		serverConfig.setDdlRun(false);
		serverConfig.setRegister(true);
		serverConfig.setDataSourceConfig(dataSourceConfig);
		serverConfig.addClass(TestEntity.class);
		serverConfig.setDefaultServer(true);

		ebeanServer = EbeanServerFactory.create(serverConfig);
		updateSchema();
	}

	private void updateSchema() {
		try {
			Connection connection = DriverManager.getConnection(dataSourceConfig.getUrl(), dataSourceConfig.getUsername(), dataSourceConfig.getPassword());
			Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
			Liquibase liquibase = new Liquibase(this.getClass().getResource("/liquibaseMigrations/migrations.xml").getPath(), new FileSystemResourceAccessor(), database);
			liquibase.update(new Contexts());
		} catch (SQLException ex) {
			Logger.getLogger(DbService.class.getName()).log(Level.SEVERE, null, ex);
		} catch (DatabaseException ex) {
			Logger.getLogger(DbService.class.getName()).log(Level.SEVERE, null, ex);
		} catch (LiquibaseException ex) {
			Logger.getLogger(DbService.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
