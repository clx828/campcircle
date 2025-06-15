package com.caden.campcircle.model.vo;

import cn.hutool.json.JSONUtil;
import com.caden.campcircle.model.entity.Post;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class MyPostVO implements Serializable {

        /**
         * id
         */
        private Long id;

        /**
         * 内容
         */
        private String content;

        /**
         * 图片列表
         */
        private List<String> pictureUrlList;

        /**
         * 点赞数
         */
        private Integer thumbNum;

        /**
         * 收藏数
         */
        private Integer favourNum;
        /**
         * 评论数
         */
        private Integer commentNum;
        /**
         * 浏览数
         */
        private Integer viewNum;
        /**
         *  当前状态
         */
        private Integer status;

         /**
         * 创建用户 id
         */
        private Long userId;

        /**
         * 创建时间
         */
        private Date createTime;

        /**
         * 更新时间
         */
        private Date updateTime;

        /**
         * 标签列表
         */
        private List<String> tagList;

        /**
         * 创建人信息
         */
        private UserVO user;

        /**
         * 是否已点赞
         */
        private Boolean hasThumb;

        /**
         * 是否已收藏
         */
        private Boolean hasFavour;

        /**
         * 是否匿名
         */
        private Boolean isAnonymous;

        /**
         * 包装类转对象
         *
         * @param postVO
         * @return
         */
        public static Post voToObj(com.caden.campcircle.model.vo.PostVO postVO) {
            if (postVO == null) {
                return null;
            }
            Post post = new Post();
            BeanUtils.copyProperties(postVO, post);
            List<String> tagList = postVO.getTagList();
            post.setTags(JSONUtil.toJsonStr(tagList));
            return post;
        }

        /**
         * 对象转包装类
         *
         * @param post
         * @return
         */
        public static com.caden.campcircle.model.vo.PostVO objToVo(Post post) {
            if (post == null) {
                return null;
            }
            com.caden.campcircle.model.vo.PostVO postVO = new com.caden.campcircle.model.vo.PostVO();
            BeanUtils.copyProperties(post, postVO);
            postVO.setTagList(JSONUtil.toList(post.getTags(), String.class));
            postVO.setPictureUrlList(JSONUtil.toList(post.getPictureList(), String.class));
            return postVO;
        }


}
