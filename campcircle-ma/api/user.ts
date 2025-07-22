import request from './request'

// 用户相关接口类型定义
interface LoginParams {
    userAccount: string
    userPassword: string
}

interface RegisterParams {
    userAccount: string
    userPassword: string
    checkPassword: string
}

interface UpdateUserParams {
    userName?: string
    userAvatar?: string
}

export interface UpdateMyUserParams {
    userAvatar?: string;
    userName?: string;
    userProfile?: string;
    backgroundUrl?: string;
}

// 用户查询请求参数
export interface UserQueryRequest {
    current?: number;
    pageSize?: number;
    id?: number;
    unionId?: string;
    mpOpenId?: string;
    userName?: string;
    userProfile?: string;
    userRole?: string;
    sortField?: string;
    sortOrder?: string;
}

// 分页搜索参数
export interface PageSearchByKeyWord {
    current?: number;
    pageSize?: number;
    keyWord?: string;
    sortField?: string;
    sortOrder?: string;
}

// 响应接口
export interface UpdateMyUserRes {
    code: number;
    data: boolean;
    message: string;
}

// 用户相关API
export const userApi = {
    // 用户注册
    register(userRegisterRequest: RegisterParams) {
        return request.post('/user/register', userRegisterRequest)
    },


    // 微信小程序登录
    loginByWxMiniapp(code: string) {
        return request.get(`/user/login/wx_miniapp?code=${code}`)
    },

    // 用户注销
    logout() {
        return request.post('/user/logout')
    },

    // 获取当前登录用户
    getLoginUser() {
        return request.get('/user/get/login')
    },

    // 根据ID获取用户包装类
    getUserVOById(id: number) {
        return request.get(`/user/get/vo?id=${id}`)
    },

    // 分页获取用户封装列表
    listUserVOByPage(userQueryRequest: UserQueryRequest) {
        return request.post('/user/list/page/vo', userQueryRequest)
    },

    // 更新个人信息
    updateMyUser(userUpdateMyRequest: UpdateMyUserParams) {
        return request.post('/user/update/my', userUpdateMyRequest)
    },

    // 根据关键词搜索用户
    searchUserVOByKeyword(pageSearchByKeyWord: PageSearchByKeyWord) {
        return request.get('/user/search/by/keyword', pageSearchByKeyWord)
    },

    // 兼容旧版本的搜索方法
    searchUserByKeyword(current: number, keyWord: string, pageSize: number, sortField?: string, sortOrder?: string) {
        return request.get(`/user/search/by/keyword?current=${current}&keyWord=${keyWord}&pageSize=${pageSize}&sortField=${sortField || ''}&sortOrder=${sortOrder || ''}`)
    }
}