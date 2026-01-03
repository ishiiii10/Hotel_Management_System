package com.hotelbooking.reports.config;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
    basePackages = "com.hotelbooking.reports.repository.hotel",
    entityManagerFactoryRef = "hotelEntityManagerFactory",
    transactionManagerRef = "hotelTransactionManager"
)
public class HotelDataSourceConfig {

    @Bean(name = "hotelDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.hotel")
    public DataSource hotelDataSource() {
        return DataSourceBuilder.create().type(com.zaxxer.hikari.HikariDataSource.class).build();
    }

    @Bean(name = "hotelEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean hotelEntityManagerFactory(
            @Qualifier("hotelDataSource") DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.hotelbooking.reports.domain.hotel");
        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        
        Map<String, Object> jpaProps = new HashMap<>();
        jpaProps.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
        jpaProps.put("hibernate.hbm2ddl.auto", "none");
        return em;
    }

    @Bean(name = "hotelTransactionManager")
    public PlatformTransactionManager hotelTransactionManager(
            @Qualifier("hotelEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}

