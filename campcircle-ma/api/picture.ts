import request from './request'

// 图片上传相关接口类型定义
export interface Picture {
    id?: number;
    url?: string;
    name?: string;
    introduction?: string;
    category?: string;
    tags?: string;
    picSize?: number;
    picWidth?: number;
    picHeight?: number;
    picScale?: number;
    picFormat?: string;
    userId?: number;
    createTime?: string;
    updateTime?: string;
    isDelete?: number;
}

// 图片相关API
export const pictureApi = {
    // 图片上传（MultipartFile）
    uploadPicture(filePath: string) {
        return request.uploadFile('/picture/upload', filePath, 'file')
    }
}
