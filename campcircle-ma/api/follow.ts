import request from './request'

// 用户相关API
export const followApi = {

    doFollow(followUserId: string) {
        return request.post(`/follow/?followUserId=${followUserId}`)
    },
}