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
}