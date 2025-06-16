import request from './request'

export interface PostCommentQueryRequest {
  postId: string;
  current?: number;
  pageSize?: number;
}

export interface PostCommentAddRequest {
  postId: string;
  content: string;
  parentId?: string;
  replyUserId?: string;
}

// 参数接口
export interface AddCommentParams {
  /* */
  content?: string;

  /* */
  parentId?: string;

  /* */
  postId?: string;

  /* */
  replyUserId?: string;
}

// 用户相关API
export const postCommentApi = {
  // 获取评论列表
  getPostCommentByPage(postCommentQueryRequest: PostCommentQueryRequest) {
    return request.get('/post/comment/list', postCommentQueryRequest)
  },

  // 添加评论
  addComment(postCommentAddRequest: PostCommentAddRequest) {
    return request.post('/post/comment/add', postCommentAddRequest)
  },

  // 删除评论
  deleteComment(id: string) {
    return request.post('/post/comment/delete', { id })
  }
}
