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
    /* */
    id?: string;
}
// 参数接口
export interface EditPostParams {
    /* */
    content?: string;

    /* */
    id?: number;

    /* */
    pictureList?: Record<string, unknown>[];

    /* */
    tags?: Record<string, unknown>[];
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
    getPostById(id: string | number) {
        return request.get(`/post/get/vo/?id=${id}`)
    },
    deletePost(deletePostParams: DeletePostParams) {
        return request.post(`/api/post/delete`, deletePostParams);
    },

    editPost(editPostParams:EditPostParams) {
    return request.post(`/api/post/edit`, editPostParams);
    },
	getHotPostList(limit:number) {
	  return request.get(`/post/get/hot/post/list?limit=${limit}`);
	},
    searchPostVOByKeyword(current, keyWord, pageSize, sortField, sortOrder) {
        return request.get(`/post/search/by/keyword?current=${current}&keyWord=${keyWord}&pageSize=${pageSize}&sortField=${sortField}&sortOrder=${sortOrder}`);
    }
}