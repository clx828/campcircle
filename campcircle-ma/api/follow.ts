import request from './request'

export interface pageRequest  {
    pageSize: number,
        current: number
}
// 关注
export const followApi = {

    doFollow(followUserId: string) {
        return request.post(`/follow/?followUserId=${followUserId}`)
    },

    getMyFollowVOList(pageRequest: pageRequest) {
        return request.get('/follow/my/follow/list', pageRequest)
    },
    getMyFansVOlist(pageRequest: pageRequest) {
        return request.get('/follow/my/fans/list', pageRequest)
    },
    getMyFollowNum() {
        return request.get('/follow/get/my/num')
    },
    getFollowPostList(pageRequest: pageRequest) {
        return request.get('/follow/my/follow/post/list', pageRequest)
    },

    // 获取指定用户的关注/粉丝数量
    getUserFollowNum(userId: string | number) {
        return request.get(`/follow/get/user/num?userId=${userId}`)
    },

    // 检查是否关注了指定用户
    checkFollowStatus(userId: string | number) {
        return request.get(`/follow/check/status?userId=${userId}`)
    }
}