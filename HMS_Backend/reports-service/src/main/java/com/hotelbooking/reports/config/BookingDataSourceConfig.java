package com.hotelbooking.reports.config;

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
        return DataSourceBuilder.create().build();
    }

    @Primary
    @Bean(name = "bookingEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean bookingEntityManagerFactory(
            @Qualifier("bookingDataSource") DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.hotelbooking.reports.domain.booking");
        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        return em;
    }

    @Primary
    @Bean(name = "bookingTransactionManager")
    public PlatformTransactionManager bookingTransactionManager(
            @Qualifier("bookingEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}

