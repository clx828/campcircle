/**
 * 系统消息类型枚举
 */
export enum SystemMessageType {
  /** 文本消息 */
  TEXT = 0,
  /** 点赞通知 */
  THUMB = 1,
  /** 收藏通知 */
  FAVOUR = 2,
  /** 评论通知 */
  COMMENT = 3,
  /** 关注通知 */
  FOLLOW = 4,
  /** 系统通知 */
  SYSTEM = 0
}

/**
 * 系统消息类型分组
 */
export const SystemMessageTypeGroups = {
  /** 系统通知 */
  SYSTEM: [SystemMessageType.SYSTEM],
  /** 赞和收藏 */
  LIKE_AND_FAVOUR: [SystemMessageType.THUMB, SystemMessageType.FAVOUR],
  /** 评论和@ */
  COMMENT_AND_MENTION: [SystemMessageType.COMMENT]
} as const

/**
 * 系统消息类型描述
 */
export const SystemMessageTypeDesc = {
  [SystemMessageType.TEXT]: '文本消息',
  [SystemMessageType.THUMB]: '点赞通知',
  [SystemMessageType.FAVOUR]: '收藏通知', 
  [SystemMessageType.COMMENT]: '评论通知',
  [SystemMessageType.FOLLOW]: '关注通知',
  [SystemMessageType.SYSTEM]: '系统通知'
} as const