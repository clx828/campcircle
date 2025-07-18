import request from './request'

// 用户相关接口类型定义
interface LoginParams {
    username: string
    password: string
}

interface RegisterParams {
    username: string
    password: string
    confirmPassword: string
}

interface UpdateUserParams {
    userName?: string
    userAvatar?: string
}
export interface UpdateMyUserParams {
  /* */
  userAvatar?: string;

  /* */
  userName?: string;

  /* */
  userProfile?: string;
}

// 响应接口
export interface UpdateMyUserRes {
  /* */
  code: number;

  /* */
  data: boolean;

  /* */
  message: string;
}

// 用户相关API
export const userApi = {
    // 登录
    login(data: LoginParams) {
        return request.get<{ token: string }>('/user/login/wx_miniapp', data)
    },

    // 注册
    register(data: RegisterParams) {
        return request.post('/user/register', data)
    },

    // 获取用户信息
    getUserInfo() {
        return request.get('/user/info')
    },

    // 更新用户信息
    updateUserInfo(data: UpdateUserParams) {
        return request.post('/user/update/my', data)
    },

    // 修改密码
    changePassword(oldPassword: string, newPassword: string) {
        return request.put('/user/password', {
            oldPassword,
            newPassword
        })
    },

    // 退出登录
    logout() {
        return request.post('/user/logout')
    },
	//更新资料
	updateMyUser(updateMyUserParams :UpdateMyUserParams){
		return request.post('/user/update/my',updateMyUserParams)
	},
    searchUserVOByKeyword(current, keyWord, pageSize, sortField, sortOrder) {
        return request.get(`/user/search/by/keyword?current=${current}&keyWord=${keyWord}&pageSize=${pageSize}&sortField=${sortField}&sortOrder=${sortOrder}`);
    }
} 