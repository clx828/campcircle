package com.caden.campcircle.job.cycle;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.caden.campcircle.mapper.PostMapper;
import com.caden.campcircle.model.entity.Post;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 * 帖子热度分数计算定时任务
 * 
 * 热度计算公式：
 * 热度分数 = (3×点赞 + 4×收藏 + 5×评论 + 0.5×浏览) / ((当前时间 - 发布时间 + 2)^0.5)
 * 
 * 执行策略：
 * 1. 每30分钟执行一次
 * 2. 只计算本周内发布的帖子（7天内）
 * 3. 优先计算最近更新的帖子
 * 4. 分批处理，避免一次性处理过多数据
 * 
 * @author caden
 */
@Component
@Slf4j
public class PostHotScoreCalculateJob {

    @Resource
    private PostMapper postMapper;

    /**
     * 每30分钟执行一次热度计算
     * 选择30分钟的原因：
     * 1. 频率适中，既能及时反映热度变化，又不会造成过大的系统负担
     * 2. 热度变化相对缓慢，30分钟的延迟是可接受的
     * 3. 避免在高峰期频繁执行影响系统性能
     */
    @Scheduled(fixedRate = 30 * 60 * 1000)
    public void calculateHotScore() {
        long startTime = System.currentTimeMillis();
        log.info("开始执行帖子热度计算任务");

        try {
            // 计算7天前的时间
            LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
            Date sevenDaysAgoDate = Date.from(sevenDaysAgo.atZone(ZoneId.systemDefault()).toInstant());

            // 查询本周内发布的帖子，按创建时间倒序（优先处理最新的帖子）
            QueryWrapper<Post> queryWrapper = new QueryWrapper<>();
            queryWrapper.ge("createTime", sevenDaysAgoDate)
                    .eq("isDelete", 0)
                    .eq("status", 1) // 只计算已发布的帖子
                    .orderByDesc("createTime");

            // 分批处理，每次处理500条
            int pageSize = 500;
            int offset = 0;
            int totalProcessed = 0;

            while (true) {
                // 使用limit分页查询
                queryWrapper.last("LIMIT " + offset + ", " + pageSize);
                List<Post> postList = postMapper.selectList(queryWrapper);

                if (postList.isEmpty()) {
                    break;
                }

                // 批量计算并更新热度分数
                for (Post post : postList) {
                    double hotScore = calculatePostHotScore(post);
                    updatePostHotScore(post.getId(), hotScore);
                }

                totalProcessed += postList.size();
                offset += pageSize;

                log.info("已处理 {} 条帖子热度计算", totalProcessed);

                // 如果查询结果少于pageSize，说明已经是最后一批
                if (postList.size() < pageSize) {
                    break;
                }
            }

            long endTime = System.currentTimeMillis();
            log.info("帖子热度计算任务完成，共处理 {} 条帖子，耗时 {} ms", totalProcessed, endTime - startTime);

        } catch (Exception e) {
            log.error("帖子热度计算任务执行失败", e);
        }
    }

    /**
     * 计算单个帖子的热度分数
     * 
     * @param post 帖子对象
     * @return 热度分数
     */
    private double calculatePostHotScore(Post post) {
        // 获取帖子的各项数据
        int thumbNum = post.getThumbNum() != null ? post.getThumbNum() : 0;
        int favourNum = post.getFavourNum() != null ? post.getFavourNum() : 0;
        int commentNum = post.getCommentNum() != null ? post.getCommentNum() : 0;
        int viewNum = post.getViewNum() != null ? post.getViewNum() : 0;

        // 计算时间差（小时）
        long currentTime = System.currentTimeMillis();
        long createTime = post.getCreateTime().getTime();
        double hoursDiff = (currentTime - createTime) / (1000.0 * 60 * 60);

        // 热度分数计算公式
        // 分子：3×点赞 + 4×收藏 + 5×评论 + 0.5×浏览
        double numerator = 3.0 * thumbNum + 4.0 * favourNum + 5.0 * commentNum + 1.0 * viewNum;
        
        // 分母：(当前时间 - 发布时间 + 2)^0.5
        // 加2是为了避免刚发布的帖子分母过小，同时给新帖子一定的基础权重
        double denominator = Math.sqrt(hoursDiff + 2);

        // 计算最终热度分数
        double hotScore = numerator / denominator;

        // 保留4位小数
        return Math.round(hotScore * 1000.0) / 10.0;
    }

    /**
     * 更新帖子的热度分数
     * 
     * @param postId 帖子ID
     * @param hotScore 热度分数
     */
    private void updatePostHotScore(Long postId, double hotScore) {
        UpdateWrapper<Post> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", postId)
                .set("hotScore", hotScore)
                .set("lastHotUpdateTime", new Date());

        postMapper.update(null, updateWrapper);
    }

    /**
     * 手动触发热度计算（用于测试或紧急更新）
     * 可以通过管理接口调用
     */
    public void manualCalculateHotScore() {
        log.info("手动触发帖子热度计算任务");
        calculateHotScore();
    }
}
