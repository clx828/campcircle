import request from './request'
import { useUserStore } from '@/stores/userStore'

// 用户相关接口类型定义
interface PictureParams {

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
    }

} 