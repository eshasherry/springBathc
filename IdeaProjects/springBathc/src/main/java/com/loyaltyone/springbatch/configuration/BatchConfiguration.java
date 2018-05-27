package com.loyaltyone.springbatch.configuration;

import com.loyaltyone.springbatch.model.User;
import com.loyaltyone.springbatch.processor.UserItemProcessor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by esherry on 2018-05-27.
 */
@Configuration
@EnableBatchProcessing
public class BatchConfiguration {
    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    @Autowired
    public DataSource dataSource;

    @Bean
    public DataSource dataSource(){
        final DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://localhost:3306/batch_schema?autoConnect=true&useSSL=false");
        dataSource.setUsername("root");
        dataSource.setPassword("root");
        System.out.print("CONNECTED!!!!!");
        return dataSource;
    }

    @Bean
    public JdbcCursorItemReader<User> reader(){
        JdbcCursorItemReader<User> reader = new JdbcCursorItemReader<User>();
        reader.setDataSource(dataSource);
        reader.setSql("select name, gender from user");
        reader.setRowMapper(new UserRowMapper());
        return reader;
    }

    public class UserRowMapper implements RowMapper<User>{

        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException{
            User user = new User();
            user.setName(rs.getString("name"));
            System.out.println("HELLOOOOO"+user.getName());
            user.setGender(rs.getString("gender"));
            return user;
        }
    }

    public UserItemProcessor processor(){
        return new UserItemProcessor();
    }

    @Bean
    public FlatFileItemWriter<User> writer(){
        FlatFileItemWriter<User> writer = new FlatFileItemWriter<User>();
        writer.setResource(new ClassPathResource("user.csv"));
        writer.setLineAggregator(new DelimitedLineAggregator<User>(){{
            setDelimiter(",");
            setFieldExtractor(new BeanWrapperFieldExtractor<User>(){{
                setNames(new String[]{"name", "gender"});
            }});
        }});
        return writer;
    }

    @Bean
    public Step step1() {
        return stepBuilderFactory.get("step1")
                .<User, User> chunk(10)
                .reader(reader())
                .processor(processor())
                .writer(writer())
                .build();
    }

    public Job exportUserJob(){
        return jobBuilderFactory.get("exportUserJob")
                .incrementer(new RunIdIncrementer())
                .flow(step1())
                .end()
                .build();
    }
}
