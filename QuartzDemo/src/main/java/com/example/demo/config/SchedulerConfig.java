package com.example.demo.config;

import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.quartz.SchedulerFactoryBeanCustomizer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import java.util.List;
import java.util.Map;

/**
 * @author Demo-Liu
 * @create 2020-06-12 11:20
 * @description 配置定时任务
 */
@Configuration
@ConfigurationProperties(prefix = "jobs")
public class SchedulerConfig implements CommandLineRunner,SchedulerFactoryBeanCustomizer {

    @Autowired
    private Scheduler scheduler;

    private final String JOB_CONF = "jobConf";
    private final String JOB_CLASS = "job";
    private final String JOB_NAME = "name";
    private final String JOB_PARAM = "param";
    private final String JOB_ACTIVE = "active";
    private final String JOB_CRON = "cron";

    private List<Map<String,Object>> jobList;

    /**
     * @Author Demo-Liu
     * @Date 20200613 14:48
     * 注册配置文件中的定时任务
     * @param args
     * @throws Exception
     */
    @Override
    public void run(String... args) throws Exception {
        //配置文件信息
        Class job;
        JobDataMap jdm = new JobDataMap();
        String jobName,cron;
        Boolean active;
        JobDetail jobDetail;
        Trigger trigger;
        //清除原有任务
        scheduler.clear();

        Map<String,Object> mapConf;
        for (Map<String, Object> map : jobList) {
            mapConf = (Map<String, Object>) map.get(JOB_CONF);
            active = Boolean.parseBoolean(mapConf.get(JOB_ACTIVE).toString());
            jobName = mapConf.get(JOB_NAME) == null ? "" : mapConf.get(JOB_NAME).toString();
            job = Class.forName(mapConf.get(JOB_CLASS).toString());
            if (active) {
                if(mapConf.get(JOB_PARAM) != null){
                    //设置任务类属性
                    jdm.putAll((Map<String, String>)mapConf.get(JOB_PARAM));
                }

                // 构建job信息
                jobDetail = JobBuilder
                        .newJob(job)
                        .withIdentity(job.getName())
                        .withDescription(jobName)
                        .usingJobData(jdm)
                        .build();
                // 表达式
                cron =  mapConf.get(JOB_CRON).toString();
                // 按新的cronExpression表达式构建一个新的trigger
                trigger = TriggerBuilder
                        .newTrigger()
                        .withIdentity(job.getName())
                        .withDescription(jobName)
                        .withSchedule(CronScheduleBuilder.cronSchedule(cron))
                        .build();
                //注册任务
                scheduler.scheduleJob(jobDetail, trigger);
                //log.info("{}({})任务注册成功...",jobName,job.getName());
            }else{
                //log.info("{}({})任务未开启...",jobName,job.getName());
            }
        }
        // 启动调度器
        scheduler.start();
    }


    /**
     * @Author Demo-Liu
     * @Date 20200614 12:44
     * 自定义 quartz配置
     * @param schedulerFactoryBean
     */
    @Override
    public void customize(SchedulerFactoryBean schedulerFactoryBean) {
        schedulerFactoryBean.setDataSource(null);
        schedulerFactoryBean.setStartupDelay(3);
    }


    public void setJobList(List<Map<String, Object>> jobList) {
        this.jobList = jobList;
    }
}