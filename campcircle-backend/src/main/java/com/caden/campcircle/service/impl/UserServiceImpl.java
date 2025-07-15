package com.caden.campcircle.service.impl;

import static com.caden.campcircle.constant.UserConstant.USER_LOGIN_STATE;

import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caden.campcircle.common.ErrorCode;
import com.caden.campcircle.common.PageSearchByKeyWord;
import com.caden.campcircle.constant.CommonConstant;
import com.caden.campcircle.exception.BusinessException;
import com.caden.campcircle.mapper.UserMapper;
import com.caden.campcircle.model.dto.user.UserQueryRequest;
import com.caden.campcircle.model.entity.User;
import com.caden.campcircle.model.enums.UserRoleEnum;
import com.caden.campcircle.model.vo.LoginUserVO;
import com.caden.campcircle.model.vo.UserVO;
import com.caden.campcircle.service.UserService;
import com.caden.campcircle.utils.JwtUtils;
import com.caden.campcircle.utils.SqlUtils;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

/**
 * 用户服务实现
 *
 * 
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private JwtUtils jwtUtils;

    /**
     * 盐值，混淆密码
     */
    public static final String SALT = "caden";

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        synchronized (userAccount.intern()) {
            // 账户不能重复
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userAccount", userAccount);
            long count = this.baseMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
            }
            // 2. 加密
            String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
            // 3. 插入数据
            User user = new User();
            user.setUserAccount(userAccount);
            user.setUserPassword(encryptPassword);
            boolean saveResult = this.save(user);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
            }
            return user.getId();
        }
    }

    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号错误");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = this.baseMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        // 3. 生成token而不是使用session
        LoginUserVO loginUserVO = this.getLoginUserVO(user);
        String token = jwtUtils.generateToken(user.getId(), user.getUserRole());
        loginUserVO.setToken(token);
        return loginUserVO;
    }

    @Override
    public LoginUserVO userLoginByMpOpen(WxOAuth2UserInfo wxOAuth2UserInfo, HttpServletRequest request) {
        String unionId = wxOAuth2UserInfo.getUnionId();
        String mpOpenId = wxOAuth2UserInfo.getOpenid();
        // 单机锁
        synchronized (unionId.intern()) {
            // 查询用户是否已存在
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("unionId", unionId);
            User user = this.getOne(queryWrapper);
            // 被封号，禁止登录
            if (user != null && UserRoleEnum.BAN.getValue().equals(user.getUserRole())) {
                throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "该用户已被封，禁止登录");
            }
            // 用户不存在则创建
            if (user == null) {
                user = new User();
                user.setUnionId(unionId);
                user.setMaOpenId(mpOpenId);
                user.setUserAvatar(wxOAuth2UserInfo.getHeadImgUrl());
                user.setUserName(wxOAuth2UserInfo.getNickname());
                boolean result = this.save(user);
                if (!result) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "登录失败");
                }
            }
            // 生成token而不是使用session
            LoginUserVO loginUserVO = this.getLoginUserVO(user);
            String token = jwtUtils.generateToken(user.getId(), user.getUserRole());
            loginUserVO.setToken(token);
            return loginUserVO;
        }
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 从请求头获取token
        String token = request.getHeader("Authorization");
        if (StringUtils.isBlank(token)) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 验证token并获取用户ID
        Long userId = jwtUtils.getUserIdFromToken(token);
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 从数据库查询（追求性能的话可以注释，直接走缓存）
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return user;
    }

    @Override
    public User getLoginUserPermitNull(HttpServletRequest request) {
        // 从请求头获取token
        String token = request.getHeader("Authorization");
        if (StringUtils.isBlank(token)) {
            return null;
        }
        // 验证token并获取用户ID
        Long userId = jwtUtils.getUserIdFromToken(token);
        if (userId == null) {
            return null;
        }
        // 从数据库查询
        return this.getById(userId);
    }

    @Override
    public boolean isAdmin(HttpServletRequest request) {
        // 从请求头获取token
        String token = request.getHeader("Authorization");
        if (StringUtils.isBlank(token)) {
            return false;
        }
        // 验证token并获取用户角色
        String userRole = jwtUtils.getUserRoleFromToken(token);
        return UserRoleEnum.ADMIN.getValue().equals(userRole);
    }

    @Override
    public boolean isAdmin(User user) {
        return user != null && UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
    }

    @Override
    public boolean userLogout(HttpServletRequest request) {
        // 使用token的方式，服务端不需要处理登出，客户端删除token即可
        return true;
    }

    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public List<UserVO> getUserVO(List<User> userList) {
        if (CollUtil.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    @Override
    public Page<UserVO> listUserVOByPage(PageSearchByKeyWord pageSearchByKeyWord, HttpServletRequest request) {
        //构建查询条件
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        String keyWord = pageSearchByKeyWord.getKeyWord();
        if (StringUtils.isNotBlank(keyWord)) {
            queryWrapper.and(w ->
                    w.like("userName", keyWord)
                            .or().like("userProfile", keyWord)
                            .or().like("id", keyWord)
            );
        }
        // 分页查询
        Page<User> page = this.page(new Page<>(pageSearchByKeyWord.getCurrent(), pageSearchByKeyWord.getPageSize()), queryWrapper);
        List<User> userList = page.getRecords();
        if (CollUtil.isEmpty(userList)){
            return new Page<>();
        }
        // 转换为VO
        Page<UserVO> userVOPage = new Page<>();
        userVOPage.setRecords(this.getUserVO(userList));
        userVOPage.setTotal(page.getTotal());
        userVOPage.setCurrent(pageSearchByKeyWord.getCurrent());
        userVOPage.setSize(pageSearchByKeyWord.getPageSize());
        return userVOPage;
    }

    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = userQueryRequest.getId();
        String unionId = userQueryRequest.getUnionId();
        String mpOpenId = userQueryRequest.getMpOpenId();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(id != null, "id", id);
        queryWrapper.eq(StringUtils.isNotBlank(unionId), "unionId", unionId);
        queryWrapper.eq(StringUtils.isNotBlank(mpOpenId), "mpOpenId", mpOpenId);
        queryWrapper.eq(StringUtils.isNotBlank(userRole), "userRole", userRole);
        queryWrapper.like(StringUtils.isNotBlank(userProfile), "userProfile", userProfile);
        queryWrapper.like(StringUtils.isNotBlank(userName), "userName", userName);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    @Override
    public LoginUserVO userLoginByMaOpen(WxMaJscode2SessionResult session, HttpServletRequest request) {
        String maOpenId = session.getOpenid();
        // 单机锁
        synchronized (maOpenId.intern()) {
            // 查询用户是否已存在
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("maOpenId", maOpenId);
            User user = this.getOne(queryWrapper);
            // 被封号，禁止登录
            if (user != null && UserRoleEnum.BAN.getValue().equals(user.getUserRole())) {
                throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "该用户已被封，禁止登录");
            }
            // 用户不存在则创建
            if (user == null) {
                user = new User();
                user.setMaOpenId(maOpenId);
                user.setUserAccount(this.generateUniqueUserAccount());
                user.setUserPassword(this.generateDefaultPassword());
                user.setUserName("微信用户");
                user.setCreateTime(new Date());

                boolean result = this.save(user);
                if (!result) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "登录失败");
                }
            }
            // 生成token而不是使用session
            LoginUserVO loginUserVO = this.getLoginUserVO(user);
            String token = jwtUtils.generateToken(user.getId(), user.getUserRole());
            loginUserVO.setToken(token);
            return loginUserVO;
        }
    }

    /**
     * 生成唯一的用户账号
     */
    private String generateUniqueUserAccount() {
        String prefix = "wx_";
        int maxAttempts = 10;

        for (int i = 0; i < maxAttempts; i++) {
            // 生成随机数字后缀
            String suffix = String.valueOf(System.currentTimeMillis() % 1000000 + new Random().nextInt(1000));
            String userAccount = prefix + suffix;

            // 检查账号是否已存在
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userAccount", userAccount);
            User existUser = this.baseMapper.selectOne(queryWrapper);

            if (existUser == null) {
                return userAccount;
            }
        }

        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成用户账号失败");
    }

    /**
     * 生成默认密码
     */
    private String generateDefaultPassword() {
        // 可以生成随机密码，或者使用固定的默认密码
        return "wx" + System.currentTimeMillis() % 100000;
    }

}