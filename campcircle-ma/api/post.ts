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

    // 当前页码,默认1
    current?: number;


    favourUserId?: number;


    id?: number;

    notId?: number;

    orTags?: Record<string, unknown>[];

    // 每页条数,默认10
    pageSize?: number;


    searchText?: string;


    sortField?: string;


    sortOrder?: string;

    tags?: Record<string, unknown>[];


    userId?: number;
}

//点赞参数接口
export interface DoThumbParams {
    postId: string;
}
//收藏参数接口
export interface DoPostFavourParams {
    postId: string;
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

    listPostVOByPage(listPostVOByPageParams: ListPostVOByPageParams) {
        return request.post('/post/list/page/vo', listPostVOByPageParams)
    }
}