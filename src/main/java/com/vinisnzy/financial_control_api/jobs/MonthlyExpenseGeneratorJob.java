package com.vinisnzy.financial_control_api.jobs;

import com.vinisnzy.financial_control_api.services.expense.FixedExpenseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.time.ZoneId;

@Slf4j
@Component
public class MonthlyExpenseGeneratorJob {
    private final FixedExpenseService fixedExpenseService;

    public MonthlyExpenseGeneratorJob(FixedExpenseService fixedExpenseService) {
        this.fixedExpenseService = fixedExpenseService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void catchUp() {
        generate("startup catch up");
    }

    @Scheduled(cron = "0 0 0 1 * *", zone = "America/Sao_Paulo")
    public void run() {
        generate("scheduled");
    }

    public void generate(String trigger) {
        YearMonth month = YearMonth.now(ZoneId.of("America/Sao_Paulo"));
        log.info("Monthly expense generator ({}) started for {}", trigger, month);
        try {
            int count = this.fixedExpenseService.generateForMonth(month);
            log.info("Created {} expenses for month {}", count, month);
            log.info("Monthly expense generator ({}) finished for {}", trigger, month);
        } catch (Exception e) {
            log.error("Monthly expense generator ({}) failed for {}", trigger, month, e);
        }
    }
}
