import request from './request'

export interface SendMessageParams {
  /* */
  content?: string;

  /* */
  messageType?: number;

  /* */
  pictureId?: number;

  /* */
  pictureUrl?: string;

  /* */
  toUserId?: number;
}
//正常情况只需传chatUserId、current、pageSize=30
export interface GetChatHistoryParams {
  /* */
  chatUserId?: number;

  /* */
  current?: number;

  /* */
  isRead?: number;

  /* */
  messageType?: number;

  /* */
  pageSize?: number;

  /* */
  sortField?: string;

  /* */
  sortOrder?: string;
}
// 响应接口
export interface GetChatListRes {
	/* */
	code: number;

	/* */
	data: {
		/* */
		chatUser: {
			/* */
			createTime: Record<string, unknown>;

			/* */
			id: number;

			/* */
			userAvatar: string;

			/* */
			userName: string;

			/* */
			userProfile: string;

			/* */
			userRole: string;
		};

		/* */
		chatUserId: number;

		/* */
		lastMessage: {
			/* */
			content: string;

			/* */
			createTime: Record<string, unknown>;

			/* */
			isRecalled: number;

			/* */
			messageType: number;
		};

		/* */
		unreadCount: number;
	}[];

	/* */
	message: string;
}


// 私信相关API
export const messageApi = {

	//获取消息列表
	getChatList(){
		return request.get('/message/get/chat/list')
	},
	//发送消息
	sendMessage(sendMessageParams : SendMessageParams){
		 return request.post(`/api/message/send`, sendMessageParams);
	},
	//撤回消息
	recallMessage(id : number){
		return request.post(`/message/recall?id=${id}`)
	},
	//标记消息已读
	markAsRead(id : number){
		return request.post(`/message/mark/as/read?id=${id}`)
	},
	//获取聊天记录
	getChatHistory(getChatHistoryParams:GetChatHistoryParams){
		return request.post('/message/get/chatHistory',getChatHistoryParams)
	},
	//获取未读消息的数量
	getUnReadMessageNum(){
		return request.get('/message/get/unread/count')
	}

}