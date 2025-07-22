package com.caden.campcircle.model;

import com.caden.campcircle.model.dto.user.UserAddRequest;
import com.caden.campcircle.model.dto.user.UserQueryRequest;
import com.caden.campcircle.model.dto.user.UserUpdateMyRequest;
import com.caden.campcircle.model.dto.user.UserUpdateRequest;
import com.caden.campcircle.model.entity.User;
import com.caden.campcircle.model.vo.LoginUserVO;
import com.caden.campcircle.model.vo.UserVO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 用户背景字段测试
 * 验证新增的背景字段（gender、school、backgroundUrl）在各个类中是否正确定义和复制
 *
 * @author caden
 */
public class UserBackgroundFieldsTest {

    @Test
    public void testUserEntityBackgroundFields() {
        // 测试User实体类是否包含所有背景字段
        User user = new User();
        user.setGender(1);
        user.setSchool("清华大学");
        user.setBackgroundUrl("https://example.com/background.jpg");

        assertEquals(Integer.valueOf(1), user.getGender());
        assertEquals("清华大学", user.getSchool());
        assertEquals("https://example.com/background.jpg", user.getBackgroundUrl());
    }

    @Test
    public void testUserVOBackgroundFields() {
        // 测试UserVO是否包含所有背景字段
        UserVO userVO = new UserVO();
        userVO.setGender(2);
        userVO.setSchool("北京大学");
        userVO.setBackgroundUrl("https://example.com/bg.png");

        assertEquals(Integer.valueOf(2), userVO.getGender());
        assertEquals("北京大学", userVO.getSchool());
        assertEquals("https://example.com/bg.png", userVO.getBackgroundUrl());
    }

    @Test
    public void testLoginUserVOBackgroundFields() {
        // 测试LoginUserVO是否包含所有背景字段
        LoginUserVO loginUserVO = new LoginUserVO();
        loginUserVO.setGender(1);
        loginUserVO.setSchool("复旦大学");
        loginUserVO.setBackgroundUrl("https://example.com/login-bg.jpg");

        assertEquals(Integer.valueOf(1), loginUserVO.getGender());
        assertEquals("复旦大学", loginUserVO.getSchool());
        assertEquals("https://example.com/login-bg.jpg", loginUserVO.getBackgroundUrl());
    }

    @Test
    public void testUserUpdateMyRequestBackgroundFields() {
        // 测试UserUpdateMyRequest是否包含所有背景字段
        UserUpdateMyRequest request = new UserUpdateMyRequest();
        request.setGender(2);
        request.setSchool("上海交通大学");
        request.setBackgroundUrl("https://example.com/update-bg.jpg");

        assertEquals(Integer.valueOf(2), request.getGender());
        assertEquals("上海交通大学", request.getSchool());
        assertEquals("https://example.com/update-bg.jpg", request.getBackgroundUrl());
    }

    @Test
    public void testUserUpdateRequestBackgroundFields() {
        // 测试UserUpdateRequest是否包含所有背景字段
        UserUpdateRequest request = new UserUpdateRequest();
        request.setGender(1);
        request.setSchool("中山大学");
        request.setBackgroundUrl("https://example.com/admin-bg.jpg");

        assertEquals(Integer.valueOf(1), request.getGender());
        assertEquals("中山大学", request.getSchool());
        assertEquals("https://example.com/admin-bg.jpg", request.getBackgroundUrl());
    }

    @Test
    public void testUserAddRequestBackgroundFields() {
        // 测试UserAddRequest是否包含所有背景字段
        UserAddRequest request = new UserAddRequest();
        request.setGender(2);
        request.setSchool("华中科技大学");
        request.setBackgroundUrl("https://example.com/add-bg.jpg");

        assertEquals(Integer.valueOf(2), request.getGender());
        assertEquals("华中科技大学", request.getSchool());
        assertEquals("https://example.com/add-bg.jpg", request.getBackgroundUrl());
    }

    @Test
    public void testUserQueryRequestBackgroundFields() {
        // 测试UserQueryRequest是否包含背景查询字段
        UserQueryRequest request = new UserQueryRequest();
        request.setGender(1);
        request.setSchool("西安交通大学");

        assertEquals(Integer.valueOf(1), request.getGender());
        assertEquals("西安交通大学", request.getSchool());
    }

    @Test
    public void testBeanUtilsCopyProperties() {
        // 测试BeanUtils.copyProperties是否能正确复制背景字段
        User user = new User();
        user.setId(1L);
        user.setUserName("测试用户");
        user.setGender(1);
        user.setSchool("测试大学");
        user.setBackgroundUrl("https://example.com/test-bg.jpg");

        // 测试复制到UserVO
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        assertEquals(user.getGender(), userVO.getGender());
        assertEquals(user.getSchool(), userVO.getSchool());
        assertEquals(user.getBackgroundUrl(), userVO.getBackgroundUrl());

        // 测试复制到LoginUserVO
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        assertEquals(user.getGender(), loginUserVO.getGender());
        assertEquals(user.getSchool(), loginUserVO.getSchool());
        assertEquals(user.getBackgroundUrl(), loginUserVO.getBackgroundUrl());
    }
}
