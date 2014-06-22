package org.springframework.jdbc.datasource.embedded;

import org.springframework.util.ClassUtils;

import java.sql.Driver;

/**
 * Created by mrjbee on 22/06/14.
 */
public class H2EmbeddedFileDatabaseConfigurer extends AbstractEmbeddedDatabaseConfigurer {

    private Class<? extends Driver> driverClass;

    public H2EmbeddedFileDatabaseConfigurer() throws ClassNotFoundException {
        this.driverClass = (Class<? extends Driver>) ClassUtils.forName("org.h2.Driver", H2EmbeddedFileDatabaseConfigurer.class.getClassLoader());
    }

    public void configureConnectionProperties(ConnectionProperties properties, String databaseName) {
        properties.setDriverClass(this.driverClass);
        properties.setUrl(String.format("jdbc:h2:./%s;DB_CLOSE_DELAY=-1", databaseName));
        properties.setUsername("sa");
        properties.setPassword("");
    }

}
