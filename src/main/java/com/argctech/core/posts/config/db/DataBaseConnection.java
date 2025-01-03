//package com.argctech.core.posts.config.db;
//
//import com.zaxxer.hikari.HikariDataSource;
//import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
//import org.springframework.boot.context.properties.ConfigurationProperties;
//import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
//import org.springframework.orm.jpa.JpaTransactionManager;
//import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
//import org.springframework.transaction.PlatformTransactionManager;
//import javax.sql.DataSource;
//
//@Configuration
//@EnableJpaRepositories(basePackages = {"com.argctech.core.posts.repository", "com.argctech.core.users.repository"})
//public class DataBaseConnection {
//
//    @Bean
//    @ConfigurationProperties("spring.datasource.default")
//    public DataSourceProperties dataSourceProperties() {
//        return new DataSourceProperties();
//    }
//
//    @Bean
//    public DataSource dataSource() {
//        return dataSourceProperties().initializeDataSourceBuilder()
//                .type(HikariDataSource.class).build();
//    }
//
//    @Bean
//    public LocalContainerEntityManagerFactoryBean entityManagerFactory(EntityManagerFactoryBuilder builder) {
//        return builder.dataSource(dataSource())
//                .packages("com.argctech.core.posts.entity", "com.argctech.core.users.entity")
//                .build();
//    }
//
//    @Bean
//    public PlatformTransactionManager transactionManager(
//            final LocalContainerEntityManagerFactoryBean entityManagerFactory
//    ) {
//        return new JpaTransactionManager(entityManagerFactory.getObject());
//    }
//
//}
