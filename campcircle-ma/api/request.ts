import { useUserStore } from '@/stores/userStore' 

// // API 基础URL
const BASE_URL = 'http://192.168.1.9:8101/api'
// const BASE_URL = 'http://106.55.158.97:8101/api'

// 请求配置接口
interface RequestConfig extends UniApp.RequestOptions {
    baseURL?: string
    custom?: any
}

// 响应接口
interface ResponseData<T = any> {
    code: number
    message: string
    data: T
}

class Request {
    private baseURL: string

    constructor(baseURL: string) {
        this.baseURL = baseURL
    }

    // 请求拦截器
    private beforeRequest(config: RequestConfig): RequestConfig {
        const userStore = useUserStore()

        // 添加token到请求头
        if (userStore.token) {
            config.header = {
                ...config.header,
                'Authorization': userStore.token
                // `Bearer ${userStore.token}`
            }
        }

        // 拼接完整请求地址
        config.url = this.baseURL + config.url

        return config
    }

    // 响应拦截器
    private handleResponse<T>(
        response: UniApp.RequestSuccessCallbackResult,
        resolve: (value: T | PromiseLike<T>) => void,
        reject: (reason?: any) => void
    ): void {
        const userStore = useUserStore()
        const res = response.data as ResponseData<T>

        // 根据状态码处理响应
        if (response.statusCode === 200) {
            // 业务状态码处理
            switch (res.code) {
                case 0:
                    console.log("请求成功了")
                    resolve(res)
                    break
                case 40100: // 未授权或token过期
                    userStore.clearUserInfo() // 清除用户信息
                    uni.showToast({
                        title: '请重新登录',
                        icon: 'none'
                    })
                    // 跳转到登录页
                    uni.navigateTo({
                        url: '/pages/login/login'
                    })
                    reject(new Error(res.message))
                    break
                default:
                    uni.showToast({
                        title: res.message || '请求失败',
                        icon: 'none'
                    })
                    reject(new Error(res.message))
            }
        } else {
            // HTTP 错误处理
            uni.showToast({
                title: `HTTP错误：${response.statusCode}`,
                icon: 'none'
            })
            reject(new Error(`HTTP错误：${response.statusCode}`))
        }
    }

    // 请求方法
    request<T = any>(config: RequestConfig): Promise<T> {
        const finalConfig = this.beforeRequest(config)

        return new Promise<T>((resolve, reject) => {
            uni.request({
                ...finalConfig,
                success: (response) => {
                    this.handleResponse<T>(response, resolve, reject)
                },
                fail: (error) => {
                    uni.showToast({
                        title: '网络请求失败',
                        icon: 'none'
                    })
                    reject(error)
                }
            })
        })
    }

    // GET 请求
    get<T = any>(url: string, data?: any, config: Partial<RequestConfig> = {}): Promise<T> {
        return this.request<T>({
            url,
            method: 'GET',
            data,
            ...config
        })
    }

    // POST 请求
    post<T = any>(url: string, data?: any, config: Partial<RequestConfig> = {}): Promise<T> {
        return this.request<T>({
            url,
            method: 'POST',
            data,
            ...config
        })
    }

    // PUT 请求
    put<T = any>(url: string, data?: any, config: Partial<RequestConfig> = {}): Promise<T> {
        return this.request<T>({
            url,
            method: 'PUT',
            data,
            ...config
        })
    }

    // DELETE 请求
    delete<T = any>(url: string, data?: any, config: Partial<RequestConfig> = {}): Promise<T> {
        return this.request<T>({
            url,
            method: 'DELETE',
            data,
            ...config
        })
    }

    // 新增：文件上传方法
    uploadFile<T = any>(url: string, filePath: string, name = 'file', formData: Record<string, any> = {}, config: Partial<RequestConfig> = {}): Promise<T> {
        const userStore = useUserStore()
        return new Promise<T>((resolve, reject) => {
            uni.uploadFile({
                url: this.baseURL + url,
                filePath,
                name,
                formData,
                header: {
                    ...(userStore.token ? { 'Authorization': userStore.token } : {}),
                    ...(config.header || {})
                },
                success: (res) => {
                    try {
                        const data = typeof res.data === 'string' ? JSON.parse(res.data) : res.data
                        if (data.code === 0) {
                            resolve(data)
                        } else {
                            uni.showToast({ title: data.message || '上传失败', icon: 'none' })
                            reject(data)
                        }
                    } catch (e) {
                        reject(e)
                    }
                },
                fail: (err) => {
                    uni.showToast({ title: '图片上传失败', icon: 'none' })
                    reject(err)
                }
            })
        })
    }
}

// 创建请求实例
const request = new Request(BASE_URL)

export default request 