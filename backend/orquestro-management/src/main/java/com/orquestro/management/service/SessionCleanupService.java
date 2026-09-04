package com.orquestro.management.service;

import com.orquestro.data.repository.UserSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Enterprise Scheduled Service responsible for purging expired and revoked user sessions.
 * Prevents continuous database growth and maintains optimal query index performance.
 * 
 * @author L.F. Desenvolvimento de Softwares LTDA
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SessionCleanupService {

    private final UserSessionRepository userSessionRepository;

    /**
     * Executes daily at 03:00 AM server time to delete expired or revoked sessions.
     */
    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public int purgeExpiredSessions() {
        LocalDateTime now = LocalDateTime.now();
        int deletedCount = userSessionRepository.deleteExpiredOrRevokedSessions(now);
        if (deletedCount > 0) {
            log.info("Session cleanup job executed: successfully purged {} expired/revoked sessions.", deletedCount);
        }
        return deletedCount;
    }
}
