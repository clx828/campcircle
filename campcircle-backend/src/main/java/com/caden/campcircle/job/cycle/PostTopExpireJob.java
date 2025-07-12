package com.caden.campcircle.job.cycle;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.caden.campcircle.model.entity.Post;
import com.caden.campcircle.service.PostService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * 帖子置顶过期检查任务
 */
@Component
@Slf4j
public class PostTopExpireJob {

    @Resource
    private PostService postService;

    /**
     * 每分钟执行一次
     */
    @Scheduled(fixedRate = 60 * 1000)
    public void run() {
        log.info("PostTopExpireJob start");
        // 查询所有已置顶且过期时间小于当前时间的帖子
        QueryWrapper<Post> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("isTop", 1)
                .lt("topExpireTime", new Date());
        
        List<Post> expiredTopPosts = postService.list(queryWrapper);
        if (expiredTopPosts.isEmpty()) {
            log.info("No expired top posts");
            return;
        }
        
        // 批量取消置顶
        for (Post post : expiredTopPosts) {
            post.setIsTop(0);
            post.setTopExpireTime(null);
        }
        
        boolean result = postService.updateBatchById(expiredTopPosts);
        log.info("PostTopExpireJob end, processed {} posts, result: {}", expiredTopPosts.size(), result);
    }
}