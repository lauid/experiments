package com.example.sgm.task;

import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.quartz.*;


@Configuration
public class QuartzTaskConfig {
    @Bean
    public JobDetail simpleJobDetail() {
        return JobBuilder.newJob(QuartzTask.class).withIdentity("quartzTaskJob").storeDurably()
                .build();
    }

    @Bean
    public Trigger simpleJobTrigger() {
        //定义每 5 秒执行一次
        SimpleScheduleBuilder simpleScheduleBuilder = SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(5).repeatForever();
        //定义触发器
        return TriggerBuilder.newTrigger().forJob(simpleJobDetail()).withIdentity("quartzTaskJobTrigger").withSchedule(simpleScheduleBuilder).build();
    }
}
