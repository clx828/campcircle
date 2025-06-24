package com.caden.campcircle.constant;

/**
 * 用户常量
 *

 */
public interface UserConstant {

    /**
     * 用户登录态键
     */
    String USER_LOGIN_STATE = "user_login";

    //  region 权限

    /**
     * 默认角色
     */
    String DEFAULT_ROLE = "user";

    /**
     * 管理员角色
     */
    String ADMIN_ROLE = "admin";

    /**
     * 被封号
     */
    String BAN_ROLE = "ban";

    /**
     * 我的帖子数量key前缀
     */
    String OWN_POST_NUM_KEY_PREFIX = "user:own_post_num:";

    /**
     * 我的收藏帖子数量key前缀
     */
    String FAVOUR_POST_NUM_KEY_PREFIX = "user:favour_post_num:";

    /**
     * 我的点赞帖子数量key前缀
     */
    String THUMB_POST_NUM_KEY_PREFIX = "user:thumb_post_num:";
}
