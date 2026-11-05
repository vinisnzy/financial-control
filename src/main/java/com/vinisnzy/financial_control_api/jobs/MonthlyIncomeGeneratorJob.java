package com.vinisnzy.financial_control_api.jobs;

import com.vinisnzy.financial_control_api.services.income.FixedIncomeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.time.ZoneId;

@Slf4j
@Component
public class MonthlyIncomeGeneratorJob {

    private final FixedIncomeService fixedIncomeService;

    public MonthlyIncomeGeneratorJob(FixedIncomeService fixedIncomeService) {
        this.fixedIncomeService = fixedIncomeService;
    }

    @Scheduled(cron = "0 0 0 1 * *", zone = "America/Sao_Paulo")
    public void run() {
        YearMonth month = YearMonth.now(ZoneId.of("America/Sao_Paulo"));
        log.info("Monthly income generator job Started for {}", month);
        try {
            int count = this.fixedIncomeService.generateForMonth(month);
            log.info("Created {} incomes for month {}", count, month);
            log.info("Monthly income generator job Finished for {}", month);
        } catch (Exception e) {
            log.error("Monthly income generator job failed for {}", month, e);
        }
    }
}
