package com.caden.campcircle.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caden.campcircle.common.ErrorCode;
import com.caden.campcircle.common.PageSearchByKeyWord;
import com.caden.campcircle.constant.CommonConstant;
import com.caden.campcircle.constant.UserConstant;
import com.caden.campcircle.exception.BusinessException;
import com.caden.campcircle.exception.ThrowUtils;
import com.caden.campcircle.mapper.FollowMapper;
import com.caden.campcircle.mapper.PostFavourMapper;
import com.caden.campcircle.mapper.PostMapper;
import com.caden.campcircle.mapper.PostThumbMapper;
import com.caden.campcircle.model.dto.post.PostEsDTO;
import com.caden.campcircle.model.dto.post.PostQueryRequest;
import com.caden.campcircle.model.entity.*;
import com.caden.campcircle.model.vo.HotPostVO;
import com.caden.campcircle.model.vo.MyPostNumVO;
import com.caden.campcircle.model.vo.PostVO;
import com.caden.campcircle.model.vo.UserVO;
import com.caden.campcircle.service.PictureService;
import com.caden.campcircle.service.PostService;
import com.caden.campcircle.service.UserService;
import com.caden.campcircle.utils.SqlUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import cn.hutool.core.collection.CollUtil;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * 帖子服务实现
 *
 * 
 */
@Service
@Slf4j
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService {

    @Resource
    private UserService userService;

    @Resource
    private PostThumbMapper postThumbMapper;

    @Resource
    private PostFavourMapper postFavourMapper;

    @Resource
    private FollowMapper followMapper;

    @Resource
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Resource
    private PictureService pictureService;

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public void validPost(Post post, boolean add) {
        if (post == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String content = post.getContent();
        String tags = post.getTags();
        // 创建时，参数不能为空
        if (add) {
            ThrowUtils.throwIf(StringUtils.isAnyBlank(content, tags), ErrorCode.PARAMS_ERROR);
        }
        // 有参数则校验
        if (StringUtils.isNotBlank(content) && content.length() > 8192) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "内容过长");
        }
    }

    /**
     * 获取查询包装类
     *
     * @param postQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<Post> getQueryWrapper(PostQueryRequest postQueryRequest) {
        QueryWrapper<Post> queryWrapper = new QueryWrapper<>();
        if (postQueryRequest == null) {
            return queryWrapper;
        }
        String searchText = postQueryRequest.getSearchText();
        String sortField = postQueryRequest.getSortField();
        String sortOrder = postQueryRequest.getSortOrder();
        Long id = postQueryRequest.getId();
        String content = postQueryRequest.getContent();
        List<String> tagList = postQueryRequest.getTags();
        Long userId = postQueryRequest.getUserId();
        Long notId = postQueryRequest.getNotId();
        // 拼接查询条件
        if (StringUtils.isNotBlank(searchText)) {
            queryWrapper.and(qw -> qw.like("title", searchText).or().like("content", searchText));
        }
        queryWrapper.like(StringUtils.isNotBlank(content), "content", content);
        if (CollUtil.isNotEmpty(tagList)) {
            for (String tag : tagList) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }
        queryWrapper.ne(ObjectUtils.isNotEmpty(notId), "id", notId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        
        // 先按置顶状态降序排序，再按其他条件排序
        queryWrapper.orderByDesc("isTop");

        // 再按用户指定的排序条件排序
        if (SqlUtils.validSortField(sortField)) {
            queryWrapper.orderBy(true, sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
        } else {
            // 默认按创建时间降序
            queryWrapper.orderByDesc("createTime");
        }
        
        return queryWrapper;
    }

    @Override
    public Page<Post> searchFromEs(PostQueryRequest postQueryRequest) {
        Long id = postQueryRequest.getId();
        Long notId = postQueryRequest.getNotId();
        String searchText = postQueryRequest.getSearchText();
        String content = postQueryRequest.getContent();
        List<String> tagList = postQueryRequest.getTags();
        List<String> orTagList = postQueryRequest.getOrTags();
        Long userId = postQueryRequest.getUserId();
        // es 起始页为 0
        long current = postQueryRequest.getCurrent() - 1;
        long pageSize = postQueryRequest.getPageSize();
        String sortField = postQueryRequest.getSortField();
        String sortOrder = postQueryRequest.getSortOrder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        // 过滤
        boolQueryBuilder.filter(QueryBuilders.termQuery("isDelete", 0));
        if (id != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("id", id));
        }
        if (notId != null) {
            boolQueryBuilder.mustNot(QueryBuilders.termQuery("id", notId));
        }
        if (userId != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("userId", userId));
        }
        // 必须包含所有标签
        if (CollUtil.isNotEmpty(tagList)) {
            for (String tag : tagList) {
                boolQueryBuilder.filter(QueryBuilders.termQuery("tags", tag));
            }
        }
        // 包含任何一个标签即可
        if (CollUtil.isNotEmpty(orTagList)) {
            BoolQueryBuilder orTagBoolQueryBuilder = QueryBuilders.boolQuery();
            for (String tag : orTagList) {
                orTagBoolQueryBuilder.should(QueryBuilders.termQuery("tags", tag));
            }
            orTagBoolQueryBuilder.minimumShouldMatch(1);
            boolQueryBuilder.filter(orTagBoolQueryBuilder);
        }
        // 按关键词检索
        if (StringUtils.isNotBlank(searchText)) {
            boolQueryBuilder.should(QueryBuilders.matchQuery("title", searchText));
            boolQueryBuilder.should(QueryBuilders.matchQuery("description", searchText));
            boolQueryBuilder.should(QueryBuilders.matchQuery("content", searchText));
            boolQueryBuilder.minimumShouldMatch(1);
        }

        // 按内容检索
        if (StringUtils.isNotBlank(content)) {
            boolQueryBuilder.should(QueryBuilders.matchQuery("content", content));
            boolQueryBuilder.minimumShouldMatch(1);
        }
        // 排序
        SortBuilder<?> sortBuilder = SortBuilders.scoreSort();
        if (StringUtils.isNotBlank(sortField)) {
            sortBuilder = SortBuilders.fieldSort(sortField);
            sortBuilder.order(CommonConstant.SORT_ORDER_ASC.equals(sortOrder) ? SortOrder.ASC : SortOrder.DESC);
        }
        // 分页
        PageRequest pageRequest = PageRequest.of((int) current, (int) pageSize);
        // 构造查询
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(boolQueryBuilder)
                .withPageable(pageRequest).withSorts(sortBuilder).build();
        SearchHits<PostEsDTO> searchHits = elasticsearchRestTemplate.search(searchQuery, PostEsDTO.class);
        Page<Post> page = new Page<>();
        page.setTotal(searchHits.getTotalHits());
        List<Post> resourceList = new ArrayList<>();
        // 查出结果后，从 db 获取最新动态数据（比如点赞数）
        if (searchHits.hasSearchHits()) {
            List<SearchHit<PostEsDTO>> searchHitList = searchHits.getSearchHits();
            List<Long> postIdList = searchHitList.stream().map(searchHit -> searchHit.getContent().getId())
                    .collect(Collectors.toList());
            List<Post> postList = baseMapper.selectBatchIds(postIdList);
            if (postList != null) {
                Map<Long, List<Post>> idPostMap = postList.stream().collect(Collectors.groupingBy(Post::getId));
                postIdList.forEach(postId -> {
                    if (idPostMap.containsKey(postId)) {
                        resourceList.add(idPostMap.get(postId).get(0));
                    } else {
                        // 从 es 清空 db 已物理删除的数据
                        String delete = elasticsearchRestTemplate.delete(String.valueOf(postId), PostEsDTO.class);
                        log.info("delete post {}", delete);
                    }
                });
            }
        }
        page.setRecords(resourceList);
        return page;
    }

    @Override
    public PostVO getPostVO(Post post, HttpServletRequest request) {
        if (post == null) {
            return null;
        }

        PostVO postVO = PostVO.objToVo(post);
        long postId = post.getId();

        // 1. 处理图片列表
        fillPostVOPictures(post, postVO);

        // 2. 关联查询用户信息
        Long userId = post.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        postVO.setUser(userVO);

        // 3. 已登录，获取用户点赞、收藏、关注状态
        User loginUser = userService.getLoginUserPermitNull(request);
        if (loginUser != null) {
            // 获取点赞
            QueryWrapper<PostThumb> postThumbQueryWrapper = new QueryWrapper<>();
            postThumbQueryWrapper.eq("postId", postId);
            postThumbQueryWrapper.eq("userId", loginUser.getId());
            PostThumb postThumb = postThumbMapper.selectOne(postThumbQueryWrapper);
            postVO.setHasThumb(postThumb != null);

            // 获取收藏
            QueryWrapper<PostFavour> postFavourQueryWrapper = new QueryWrapper<>();
            postFavourQueryWrapper.eq("postId", postId);
            postFavourQueryWrapper.eq("userId", loginUser.getId());
            PostFavour postFavour = postFavourMapper.selectOne(postFavourQueryWrapper);
            postVO.setHasFavour(postFavour != null);

            // 获取关注
            QueryWrapper<Follow> followQueryWrapper = new QueryWrapper<>();
            followQueryWrapper.eq("userId", loginUser.getId());
            followQueryWrapper.eq("followUserId", post.getUserId());
            Follow follow = followMapper.selectOne(followQueryWrapper);
            postVO.setHasFollow(follow != null);
        }

        return postVO;
    }

    @Override
    public Page<PostVO> getPostVOPage(Page<Post> postPage, HttpServletRequest request) {
        List<Post> postList = postPage.getRecords();
        Page<PostVO> postVOPage = new Page<>(postPage.getCurrent(), postPage.getSize(), postPage.getTotal());
        if (CollUtil.isEmpty(postList)) {
            return postVOPage;
        }

        // 1. 关联查询用户信息
        Set<Long> userIdSet = postList.stream().map(Post::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 已登录，获取用户点赞、收藏状态
        Map<Long, Boolean> postIdHasThumbMap = new HashMap<>();
        Map<Long, Boolean> postIdHasFavourMap = new HashMap<>();
        Map<Long, Boolean> userIdHasFollowMap = new HashMap<>();
        User loginUser = userService.getLoginUserPermitNull(request);
        if (loginUser != null) {
            Set<Long> postIdSet = postList.stream().map(Post::getId).collect(Collectors.toSet());
            loginUser = userService.getLoginUser(request);
            // 获取点赞
            QueryWrapper<PostThumb> postThumbQueryWrapper = new QueryWrapper<>();
            postThumbQueryWrapper.in("postId", postIdSet);
            postThumbQueryWrapper.eq("userId", loginUser.getId());
            List<PostThumb> postPostThumbList = postThumbMapper.selectList(postThumbQueryWrapper);
            postPostThumbList.forEach(postPostThumb -> postIdHasThumbMap.put(postPostThumb.getPostId(), true));
            // 获取收藏
            QueryWrapper<PostFavour> postFavourQueryWrapper = new QueryWrapper<>();
            postFavourQueryWrapper.in("postId", postIdSet);
            postFavourQueryWrapper.eq("userId", loginUser.getId());
            List<PostFavour> postFavourList = postFavourMapper.selectList(postFavourQueryWrapper);
            postFavourList.forEach(postFavour -> postIdHasFavourMap.put(postFavour.getPostId(), true));
            //获取关注
            QueryWrapper<Follow> FollowQueryWrapper = new QueryWrapper<>();
            FollowQueryWrapper.eq("userId", loginUser.getId());
            FollowQueryWrapper.in("followUserId",userIdSet);
            List<Follow> followList = followMapper.selectList(FollowQueryWrapper);
            followList.forEach(follow -> userIdHasFollowMap.put(follow.getFollowUserId(), true));
        }
        // 填充信息
        List<PostVO> postVOList = postList.stream().map(post -> {
            PostVO postVO = PostVO.objToVo(post);

            // 处理图片列表（复用私有方法）
            fillPostVOPictures(post, postVO);

            // 设置用户信息
            Long userId = post.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            postVO.setUser(userService.getUserVO(user));

            // 设置用户交互状态
            postVO.setHasFollow(userIdHasFollowMap.getOrDefault(post.getUserId(), false));
            postVO.setHasThumb(postIdHasThumbMap.getOrDefault(post.getId(), false));
            postVO.setHasFavour(postIdHasFavourMap.getOrDefault(post.getId(), false));

            return postVO;
        }).collect(Collectors.toList());
        postVOPage.setRecords(postVOList);
        return postVOPage;
    }

    @Override
    public MyPostNumVO getMyPostNum(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String ownKey = UserConstant.OWN_POST_NUM_KEY_PREFIX + id;
        String favourKey = UserConstant.FAVOUR_POST_NUM_KEY_PREFIX + id;
        String thumbKey = UserConstant.THUMB_POST_NUM_KEY_PREFIX + id;
        MyPostNumVO myPostNumVO = new MyPostNumVO();
        // 获取各类统计值（如果没有缓存，则查库并写入缓存）
        myPostNumVO.setOwnPostNum(getOrCacheCount(ownKey, () ->
                this.count(new QueryWrapper<Post>().eq("userId", id))));

        myPostNumVO.setFavourPostNum(getOrCacheCount(favourKey, () ->
                postFavourMapper.selectCount(new QueryWrapper<PostFavour>().eq("userId", id))));

        myPostNumVO.setThumbPostNum(getOrCacheCount(thumbKey, () ->
                postThumbMapper.selectCount(new QueryWrapper<PostThumb>().eq("userId", id))));

        return myPostNumVO;
    }

    /**
     * 填充 PostVO 的图片信息
     *
     * @param post   原始帖子对象
     * @param postVO 帖子VO对象
     */
    private void fillPostVOPictures(Post post, PostVO postVO) {
        String pictureIdStr = post.getPictureList();
        List<String> pictureIdList = JSONUtil.toList(pictureIdStr, String.class);

        // 判断是否有图片
        if (pictureIdList != null && !pictureIdList.isEmpty()) {
            List<Long> pictureIds = pictureIdList.stream()
                    .map(id -> {
                        try {
                            return Long.parseLong(id);
                        } catch (NumberFormatException e) {
                            log.warn("Invalid picture ID: {}", id);
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (!pictureIds.isEmpty()) {
                List<Picture> pictures = pictureService.listByIds(pictureIds);
                List<String> pictureUrlList = pictures.stream()
                        .map(Picture::getPictureUrl)
                        .collect(Collectors.toList());
                postVO.setPictureUrlList(pictureUrlList);
            } else {
                postVO.setPictureUrlList(new ArrayList<>());
            }
        } else {
            postVO.setPictureUrlList(pictureIdList != null ? pictureIdList : new ArrayList<>());
        }
    }

    private Long getOrCacheCount(String redisKey, Supplier<Long> dbQuery) {
        String cached = redisTemplate.opsForValue().get(redisKey);
        if (cached != null) {
            return Long.parseLong(cached);
        }
        Long count = dbQuery.get();
        // 设置 8 到 10 分钟之间的随机过期时间（单位：秒）
        int expireSeconds = 480 + new Random().nextInt(121); // 480~600 秒
        redisTemplate.opsForValue().set(redisKey, String.valueOf(count), expireSeconds, TimeUnit.SECONDS);
        return count;
    }

    @Override
    public boolean topPost(long postId, Date topExpireTime) {
        Post post = this.getById(postId);
        if (post == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        
        // 如果过期时间为空，则取消置顶
        if (topExpireTime == null) {
            post.setIsTop(0);
            post.setTopExpireTime(null);
        } else {
            // 设置置顶
            post.setIsTop(1);
            post.setTopExpireTime(topExpireTime);
        }
        
        return this.updateById(post);
    }

    @Override
    public List<HotPostVO> getHotPostList(Integer limit) {
        // 限制查询数量
        if (limit == null || limit <= 0) {
            limit = 10; // 默认查询10条
        }
        //不能大于30
        if (limit > 30) {
            limit = 30;
        }
        // 只查询需要的字段，提高查询效率
        QueryWrapper<Post> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id", "content", "hotScore", "lastHotUpdateTime")
                    .eq("isDelete", 0)
                    .orderByDesc("hotScore")
                    .last("limit " + limit);
        
        // 查询热门帖子
        List<Post> postList = this.list(queryWrapper);
        // 转换为VO对象
        List<HotPostVO> hotPostVOList = postList.stream().map(post -> {
            HotPostVO hotPostVO = new HotPostVO();
            BeanUtils.copyProperties(post, hotPostVO);
            return hotPostVO;
        }).collect(Collectors.toList());
        return hotPostVOList;
    }

    @Override
    public Page<PostVO> listPostVOByPage(PageSearchByKeyWord pageSearchByKeyWord, HttpServletRequest request) {
        String keyWord = pageSearchByKeyWord.getKeyWord();
        QueryWrapper<Post> queryWrapper = new QueryWrapper<>();
        queryWrapper.like(StringUtils.isNotBlank(keyWord), "content", keyWord);
        Page<Post> postPage = this.page(new Page<>(pageSearchByKeyWord.getCurrent(), pageSearchByKeyWord.getPageSize()), queryWrapper);
        if (postPage.getTotal() == 0){
            return new Page<>();
        }
        Page<PostVO> postVOPage = this.getPostVOPage(postPage, request);
        return postVOPage;
    }

}
