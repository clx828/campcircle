import { defineStore } from 'pinia'

interface UserState {
    id: number | null
    userName: string
    userAvatar: string
    userProfile: string
    backgroundUrl: string
    userRole: 'user' | 'admin' | 'ban'
    token: string
}

export const useUserStore = defineStore('user', {
    state: (): UserState => ({
        id: null,
        userName: '',
        userAvatar: '',
        userProfile: '',
        userRole: 'user',
        backgroundUrl: '',
        token: ''
    }),

    getters: {
        // 获取用户信息
        getUserInfo(state): UserState {
            return {
                id: state.id,
                userName: state.userName,
                userAvatar: state.userAvatar,
                userProfile: state.userProfile,
                backgroundUrl: state.backgroundUrl,
                userRole: state.userRole,
                token: state.token
            }
        },
        // 判断是否登录
        isLoggedIn(state): boolean {
            return !!state.token
        },
        // 判断是否是管理员
        isAdmin(state): boolean {
            return state.userRole === 'admin'
        }
    },

    actions: {
        // 设置用户信息
        setUserInfo(userInfo: Partial<UserState>) {
            if (userInfo.id) this.id = userInfo.id
            if (userInfo.userName) this.userName = userInfo.userName
            if (userInfo.userAvatar) this.userAvatar = userInfo.userAvatar
            if (userInfo.userProfile) this.userProfile = userInfo.userProfile
            if (userInfo.backgroundUrl) this.backgroundUrl = userInfo.backgroundUrl
            if (userInfo.userRole) this.userRole = userInfo.userRole
            if (userInfo.token) this.token = userInfo.token
        },

        // 清除用户信息（退出登录时使用）
        clearUserInfo() {
            this.id = null
            this.userName = ''
            this.userAvatar = ''
            this.userProfile = ''
            this.backgroundUrl = ''
            this.userRole = 'user'
            this.token = ''
        }
    }
}) 