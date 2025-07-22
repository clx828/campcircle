import request from './request'

// 参数接口
export interface GenerateQrCodeParams {
    /*是否自动配置线条颜色 */
    autoColor?: boolean;

    /*是否检查页面存在 */
    checkPath?: boolean;

    /*跳转页面路径 */
    page?: string;

    /*场景参数 */
    scene: string;

    /*二维码宽度 */
    width?: number;
}

// 响应接口
export interface GenerateQrCodeRes {
    /* */
    code: number;

    /* */
    data: {
        /*二维码图片的MIME类型 */
        mimeType: string;

        /*二维码图片的Base64编码 */
        qrCodeBase64: string;

        /*二维码图片大小（字节） */
        size: number;
    };

    /* */
    message: string;
}

/**
 * 生成小程序二维码
 * @param {object} params 二维码生成请求
 * @param {boolean} params.autoColor 是否自动配置线条颜色
 * @param {boolean} params.checkPath 是否检查页面存在
 * @param {string} params.page 跳转页面路径
 * @param {string} params.scene 场景参数
 * @param {number} params.width 二维码宽度
 * @returns
 */
export const qrApi = {
    generateQrCode(params: GenerateQrCodeParams): Promise<GenerateQrCodeRes> {
        return request.post(`/qrcode/generate`, params);
    }
}