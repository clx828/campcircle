import request from './request'

// 系统消息添加参数接口
export interface SystemMessageAddParams {
  /* 消息标题 */
  title?: string;
  
  /* 消息内容 */
  content?: string;
  
  /* 消息类型 0-系统通知 1-点赞通知 2-收藏通知 3-评论通知 4-关注通知 */
  type?: number;
  
  /* 接收用户ID，为空时发送给所有用户 */
  toUserId?: number;
  
  /* 关联的帖子ID */
  postId?: number;
  
  /* 关联的评论ID */
  commentId?: number;
}

// 系统消息更新参数接口
export interface SystemMessageUpdateParams {
  /* 消息ID */
  id: number;
  
  /* 消息标题 */
  title?: string;
  
  /* 消息内容 */
  content?: string;
  
  /* 消息类型 */
  type?: number;
  
  /* 接收用户ID */
  toUserId?: number;
  
  /* 关联的帖子ID */
  postId?: number;
  
  /* 关联的评论ID */
  commentId?: number;
}

// 系统消息查询参数接口
export interface SystemMessageQueryParams {
  /* 当前页码 */
  current?: number;

  /* 页面大小 */
  pageSize?: number;

  /* 消息类型 */
  type?: number;

  /* 消息类型列表 */
  types?: number[];

  /* 接收用户ID */
  toUserId?: number;

  /* 消息状态 0-未读 1-已读 */
  status?: number;

  /* 排序字段 */
  sortField?: string;

  /* 排序顺序 */
  sortOrder?: string;
}

// 删除请求参数接口
export interface DeleteParams {
  /* 要删除的ID */
  id: number;
}

// 系统消息VO响应接口
export interface SystemMessageVO {
  /* 消息ID */
  id: number;

  /* 消息标题 */
  title: string;

  /* 消息内容 */
  content: string;

  /* 消息类型 */
  type: number;

  /* 消息类型描述 */
  typeDesc: string;

  /* 发送用户ID */
  fromUserId: number;

  /* 接收用户ID */
  toUserId: number;

  /* 消息状态 0-未读 1-已读 */
  status: number;

  /* 状态描述 */
  statusDesc: string;

  /* 关联的帖子ID */
  postId?: number;

  /* 关联的评论ID */
  commentId?: number;

  /* 是否为全局消息 */
  isGlobal?: number;

  /* 发送用户信息 */
  fromUser?: any;

  /* 接收用户信息 */
  toUser?: any;

  /* 关联的帖子信息 */
  post?: any;

  /* 创建时间 */
  createTime: Record<string, unknown>;

  /* 更新时间 */
  updateTime: Record<string, unknown>;
}

// 分页响应接口
export interface SystemMessagePageRes {
  /* 响应码 */
  code: number;
  
  /* 响应数据 */
  data: {
    /* 当前页码 */
    current: number;
    
    /* 页面大小 */
    size: number;
    
    /* 总页数 */
    pages: number;
    
    /* 总记录数 */
    total: number;
    
    /* 记录列表 */
    records: SystemMessageVO[];
    
    /* 其他分页信息 */
    countId?: string;
    maxLimit?: number;
    optimizeCountSql?: boolean;
    orders?: {
      asc: boolean;
      column: string;
    }[];
    searchCount?: boolean;
  };
  
  /* 响应消息 */
  message: string;
}

// 未读消息数量响应接口
export interface UnreadCountRes {
  /* 响应码 */
  code: number;

  /* 响应数据 */
  data: {
    /* 总未读数量 */
    total: string;

    /* 系统通知未读数量 */
    system: string;

    /* 点赞和收藏未读数量 */
    likeFavour: string;

    /* 评论和关注未读数量 */
    commentFollow: string;
  };

  /* 响应消息 */
  message: string;
}

// 基础响应接口
export interface BaseResponse<T> {
  /* 响应码 */
  code: number;
  
  /* 响应数据 */
  data: T;
  
  /* 响应消息 */
  message: string;
}

// 系统消息相关API
export const systemMessageApi = {

  
  // 根据ID获取系统消息详情
  getSystemMessageById(id: number) {
    return request.get(`/system-message/get/vo?id=${id}`);
  },
  
  // 分页获取系统消息列表
  listSystemMessageByPage(params: SystemMessageQueryParams) {
    return request.post('/system-message/list/page/vo', params);
  },
  
  // 获取我的消息列表
  listMySystemMessageByPage(params: SystemMessageQueryParams) {
    return request.post('/system-message/my/list/page/vo', params);
  },
  
  // 标记消息为已读
  markAsRead(messageId: number) {
    return request.post(`/system-message/mark-read/${messageId}`);
  },
  
  // 批量标记消息为已读
  markAllAsRead(type?: number) {
    const url = type !== undefined 
      ? `/system-message/mark-all-read?type=${type}`
      : '/system-message/mark-all-read';
    return request.post(url);
  },
  
  // 获取未读消息数量
  getUnreadCount() {
    return request.get('/system-message/unread-count');
  },

}
