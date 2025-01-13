package com.fast.jmx.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class InitCommandLineRunner implements CommandLineRunner {

    @Resource
    private VmService vmService;

    @Override
    public void run(String... args) {
        log.info("init vm monitor cache");
        vmService.getLocalMonitorList();
    }
}
