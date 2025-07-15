package com.caden.campcircle.job.cycle;

import com.caden.campcircle.service.ApiRequestLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * API请求日志清理定时任务
 * 
 * 定期清理过期的请求日志，避免数据库存储过多历史数据
 *
 * @author caden
 */
@Component
@Slf4j
public class ApiRequestLogCleanJob {

    @Resource
    private ApiRequestLogService apiRequestLogService;

    /**
     * 每天凌晨2点执行日志清理任务
     * 清理30天前的日志数据
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanExpiredLogs() {
        long startTime = System.currentTimeMillis();
        log.info("开始执行API请求日志清理任务");

        try {
            // 计算30天前的时间
            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
            Date beforeDate = Date.from(thirtyDaysAgo.atZone(ZoneId.systemDefault()).toInstant());

            // 清理过期日志
            int cleanedCount = apiRequestLogService.cleanExpiredLogs(beforeDate);

            long endTime = System.currentTimeMillis();
            log.info("API请求日志清理任务完成，清理了 {} 条记录，耗时 {} ms", 
                    cleanedCount, endTime - startTime);

        } catch (Exception e) {
            log.error("API请求日志清理任务执行失败", e);
        }
    }

    /**
     * 手动触发日志清理（用于测试或紧急清理）
     * 
     * @param days 清理多少天前的日志
     */
    public void manualCleanLogs(int days) {
        log.info("手动触发API请求日志清理任务，清理 {} 天前的日志", days);
        
        try {
            LocalDateTime daysAgo = LocalDateTime.now().minusDays(days);
            Date beforeDate = Date.from(daysAgo.atZone(ZoneId.systemDefault()).toInstant());
            
            int cleanedCount = apiRequestLogService.cleanExpiredLogs(beforeDate);
            log.info("手动清理完成，清理了 {} 条记录", cleanedCount);
            
        } catch (Exception e) {
            log.error("手动清理API请求日志失败", e);
            throw e;
        }
    }
}
