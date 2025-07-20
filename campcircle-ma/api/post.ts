import request from './request'

// 用户相关接口类型定义
export interface addPostParams {
    content: string,
    tags: string[],
    pictureList: string[]
}
// 分页查询帖子列表
export interface ListPostVOByPageParams {
    content?: string;
    current?: number;
    favourUserId?: number;
    id?: number;
    notId?: number;
    orTags?: Record<string, unknown>[];
    pageSize?: number;
    searchText?: string;
    sortField?: string;
    sortOrder?: string;
    tags?: Record<string, unknown>[];
    userId?: number;
}


// 获取我的帖子列表参数
export interface ListMyPostVOByPageParams {
    current?: number;
    pageSize?: number;
}

//点赞参数接口
export interface DoThumbParams {
    postId: string;
}
//收藏参数接口
export interface DoPostFavourParams {
    postId: string;
}

export interface DeletePostParams {
    id?: string;
}

// 编辑帖子参数接口
export interface EditPostParams {
    content?: string;
    id?: number;
    pictureList?: string[];
    tags?: string[];
}

// 更新帖子参数接口
export interface UpdatePostParams {
    content?: string;
    id?: string;
    isPublic?: number;
    tags?: string[];
    pictureList?: string[];
}

// 帖子置顶请求参数
export interface PostTopRequest {
    postId: number;
    topTimeHours?: number;
}

// 分页搜索参数
export interface PageSearchByKeyWord {
    current?: number;
    pageSize?: number;
    keyWord?: string;
    sortField?: string;
    sortOrder?: string;
}
// 用户相关API
export const postApi = {

    // // 更新用户信息
    // uploadPicture(data: PictureParams) {
    //     return request.post('/picture/upload', data)
    // },

    // 图片上传（MultipartFile）
    uploadPicture(filePath: string) {
        return request.uploadFile('/picture/upload', filePath, 'file')
    },

    addPost(addPostParams: addPostParams) {
        return request.post('/post/add', addPostParams)
    },
    deletePost(deletePostParams: DeletePostParams) {
        return request.post('/post/delete', deletePostParams);
    },
    // 点赞/取消点赞
    doThumb(doThumbParams: DoThumbParams) {
        return request.post('/post_thumb/', doThumbParams)
    },

    // 收藏/取消收藏
    doFavour(doPostFavourParams: DoPostFavourParams) {
        return request.post('/post_favour/', doPostFavourParams)
    },

    // 分页获取帖子列表
    listPostVOByPage(listPostVOByPageParams: ListPostVOByPageParams) {
        return request.post('/post/list/page/vo', listPostVOByPageParams)
    },

    // 分页获取我的帖子列表
    listMyPostVOByPage(listMyPostVOByPageParams: ListMyPostVOByPageParams) {
        return request.post('/post/my/list/page/vo', listMyPostVOByPageParams)
    },

    // 分页获取我收藏的帖子列表
    listMyFavourPostVOByPage(listMyPostVOByPageParams: ListMyPostVOByPageParams) {
        return request.post('/post_favour/my/list/page', listMyPostVOByPageParams)
    },

    // 分页获取我点赞的帖子列表
    listMyThumbPostVOByPage(listMyPostVOByPageParams: ListMyPostVOByPageParams) {
        return request.post('/post_thumb/my/list/page', listMyPostVOByPageParams)
    },

    // 获取我的帖子数量
    getMyPostNum() {
        return request.get('/post/get/my/postNum')
    },

    // 根据ID获取帖子详情
    getPostVOById(id: number) {
        return request.get(`/post/get/vo?id=${id}`)
    },

    // 编辑帖子
    editPost(postEditRequest: EditPostParams) {
        return request.post('/post/edit', postEditRequest)
    },

    // 更新帖子（需要管理员权限，但保留接口）
    updatePost(postUpdateRequest: UpdatePostParams) {
        return request.post('/post/update', postUpdateRequest)
    },

    // 分页搜索帖子（从ES查询）
    searchPostVOByPage(postQueryRequest: ListPostVOByPageParams) {
        return request.post('/post/search/page/vo', postQueryRequest)
    },

    // 置顶/取消置顶帖子
    topPost(postTopRequest: PostTopRequest) {
        return request.post('/post/top', postTopRequest)
    },

    // 获取热门帖子列表
    getHotPostList(limit: number = 10) {
        return request.get(`/post/get/hot/post/list?limit=${limit}`)
    },

    // 根据关键词搜索帖子
    searchPostVOByKeyword(pageSearchByKeyWord: PageSearchByKeyWord) {
        return request.get('/post/search/by/keyword', pageSearchByKeyWord)
    },

    // 兼容旧版本的搜索方法
    searchByKeyword(current: number, keyWord: string, pageSize: number, sortField?: string, sortOrder?: string) {
        return request.get(`/post/search/by/keyword?current=${current}&keyWord=${keyWord}&pageSize=${pageSize}&sortField=${sortField || ''}&sortOrder=${sortOrder || ''}`)
    }

}