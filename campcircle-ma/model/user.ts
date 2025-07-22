export interface IUser {
  id: number;
  userName: string;
  userAvatar: string;
  userProfile: string;
  userRole: string;
  backgroundUrl?: string; // 用户背景图片URL
  createTime?: Date | string; // 支持 Date 或 JSON 字符串
  updateTime?: Date | string;
  token?: string;
}