package br.fernando.springbatchjdbccsv.config;

import br.fernando.springbatchjdbccsv.model.Person;
import br.fernando.springbatchjdbccsv.service.PersonFieldSetMapper;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.sql.DataSource;


@Configuration
public class JobConfiguration {

    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    @Autowired
    public DataSource dataSource;

    @Bean
    public FlatFileItemReader<Person> personItemReader() {
        FlatFileItemReader<Person> reader = new FlatFileItemReader<>();
        reader.setLinesToSkip(1);
        reader.setResource(new ClassPathResource("data/person.csv"));

        DefaultLineMapper<Person> customerLineMapper = new DefaultLineMapper<>();

        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setNames(new String[] {"id", "firstName", "lastName", "birthdate"});

        customerLineMapper.setLineTokenizer(tokenizer);
        customerLineMapper.setFieldSetMapper(new PersonFieldSetMapper());
        customerLineMapper.afterPropertiesSet();
        reader.setLineMapper(customerLineMapper);
        return reader;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Bean
    public JdbcBatchItemWriter<Person> personItemWriter() {
        JdbcBatchItemWriter<Person> itemWriter = new JdbcBatchItemWriter<>();

        itemWriter.setDataSource((javax.sql.DataSource) this.dataSource);
        itemWriter.setSql("INSERT INTO person VALUES (:id, :firstName, :lastName, :birthdate)");
        itemWriter.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider());
        itemWriter.afterPropertiesSet();

        return itemWriter;
    }

    @Bean
    public Step step1() {
        return stepBuilderFactory.get("step1")
                .<Person, Person>chunk(10)
                .reader(personItemReader())
                .writer(personItemWriter())
                .build();
    }

    @Bean
    public Job job() {
        return jobBuilderFactory.get("job")
                .start(step1())
                .build();
    }
}