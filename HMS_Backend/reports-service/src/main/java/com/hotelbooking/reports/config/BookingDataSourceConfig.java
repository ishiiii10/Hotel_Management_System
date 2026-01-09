package com.hotelbooking.reports.config;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import jakarta.persistence.EntityManagerFactory;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
    basePackages = "com.hotelbooking.reports.repository.booking",
    entityManagerFactoryRef = "bookingEntityManagerFactory",
    transactionManagerRef = "bookingTransactionManager"
)
public class BookingDataSourceConfig {

    @Primary
    @Bean(name = "bookingDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.booking")
    public DataSource bookingDataSource() {
        return DataSourceBuilder.create().type(com.zaxxer.hikari.HikariDataSource.class).build();
    }

    @Primary
    @Bean(name = "bookingEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean bookingEntityManagerFactory(
            @Qualifier("bookingDataSource") DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.hotelbooking.reports.domain.booking");
        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        
        Map<String, Object> jpaProps = new HashMap<>();
        jpaProps.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
        jpaProps.put("hibernate.hbm2ddl.auto", "none");
        jpaProps.put("jakarta.persistence.jdbc.url", ((com.zaxxer.hikari.HikariDataSource) dataSource).getJdbcUrl());
        em.setJpaPropertyMap(jpaProps);
        return em;
    }

    @Primary
    @Bean(name = "bookingTransactionManager")
    public PlatformTransactionManager bookingTransactionManager(
            @Qualifier("bookingEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}

