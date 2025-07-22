import { useUserStore } from '@/stores/userStore'

// 不需要登录就可以访问的页面
const whiteList = [
    'pages/tabbar/home/home',
    'pages/tabbar/follow/follow',  // 添加关注页面
    'pages/tabbar/tabbar-3/tabbar-3',  // 添加发布页面
    'pages/login/login',
	'pages/tabbar/mine/mine'  // 我的页面

]

// 需要登录才能访问的页面
const authPages = [
    'pages/favorites/favorites',
    'pages/tabbar/message/message' // 消息页面
  	]

// tabBar页面列表
const tabBarPages = [
    'pages/tabbar/home/home',
    'pages/tabbar/follow/follow',
    'pages/tabbar/tabbar-3/tabbar-3',
    'pages/tabbar/message/message',
    'pages/tabbar/mine/mine'
]

class RouterGuard {
    /**
     * 标准化路径 - 移除前导斜杠和查询参数
     * @param {string} path 原始路径
     * @returns {string} 标准化后的路径
     */
    static normalizePath(path) {
        if (!path) return ''
        
        // 移除前导斜杠
        let normalizedPath = path.startsWith('/') ? path.slice(1) : path
        
        // 移除查询参数
        normalizedPath = normalizedPath.split('?')[0]
        
        return normalizedPath
    }
    
    /**
     * 判断是否是需要登录的页面
     * @param {string} path 页面路径
     * @returns {boolean}
     */
    static needAuth(path) {
        const normalizedPath = this.normalizePath(path)
        
        // 先检查白名单 - 精确匹配
        const isInWhiteList = whiteList.some(item => item === normalizedPath)
        if (isInWhiteList) return false
        
        // 再检查是否在需要登录的页面列表中 - 精确匹配
        return authPages.some(page => page === normalizedPath)
    }
    
    /**
     * 检查是否是tabBar页面
     * @param {string} url 页面路径
     * @returns {boolean}
     */
    static isTabBarPage(url) {
        const normalizedPath = this.normalizePath(url)
        return tabBarPages.some(item => item === normalizedPath)
    }
    
    /**
     * 获取用户Store实例
     * @returns {Object} userStore实例
     */
    static getUserStore() {
        try {
            return useUserStore()
        } catch (error) {
            console.warn('无法获取用户Store，可能是在Store初始化之前调用')
            return { isLoggedIn: false }
        }
    }
    
    /**
     * 路由拦截处理
     * @param {Object} options 页面参数
     * @param {string} options.path 页面路径
     * @param {Object} options.query 查询参数
     * @returns {Promise<boolean>}
     */
    static async handleRoute(options) {
        try {
            const userStore = this.getUserStore()
            const path = this.normalizePath(options.path)
            
            console.log('路由守卫检查:', { path, needAuth: this.needAuth(path), isLoggedIn: userStore.isLoggedIn })
            
            // 如果是需要登录的页面且用户未登录
            if (this.needAuth(path) && !userStore.isLoggedIn) {
                // 构建重定向URL
                let redirectUrl = path
                if (options.query && Object.keys(options.query).length > 0) {
                    const queryString = Object.keys(options.query)
                        .map(key => `${key}=${encodeURIComponent(options.query[key])}`)
                        .join('&')
                    redirectUrl = `${path}?${queryString}`
                }
                
                console.log('需要登录，跳转到登录页面:', redirectUrl)
                
                // 跳转到登录页
                uni.navigateTo({
                    url: `/pages/login/login?redirect=${encodeURIComponent(redirectUrl)}`
                })
                
                return false
            }
            
            return true
        } catch (error) {
            console.error('路由守卫错误:', error)
            return true // 发生错误时允许通过，避免阻塞用户操作
        }
    }
    
    /**
     * 处理tabBar页面切换
     * @param {string} pagePath tabBar页面路径
     * @returns {Promise<boolean>}
     */
    static async handleTabBarSwitch(pagePath) {
        try {
            const userStore = this.getUserStore()
            const normalizedPath = this.normalizePath(pagePath)
            
            console.log('TabBar切换检查:', { pagePath: normalizedPath, needAuth: this.needAuth(normalizedPath), isLoggedIn: userStore.isLoggedIn })
            
            // 如果是需要登录的tabBar页面且用户未登录
            if (this.needAuth(normalizedPath) && !userStore.isLoggedIn) {
                console.log('TabBar页面需要登录，跳转到登录页面')
                
                uni.navigateTo({
                    url: `/pages/login/login?redirect=${encodeURIComponent(normalizedPath)}`
                })
                return false
            }
            
            return true
        } catch (error) {
            console.error('TabBar路由守卫错误:', error)
            return true
        }
    }
    
    /**
     * 检查用户登录状态
     * @returns {boolean}
     */
    static checkLoginStatus() {
        try {
            const userStore = this.getUserStore()
            return userStore.isLoggedIn
        } catch (error) {
            console.error('检查登录状态失败:', error)
            return false
        }
    }
    
    /**
     * 处理登录成功后的跳转
     * @param {string} redirectUrl 重定向URL
     */
    static handleLoginSuccess(redirectUrl) {
        try {
            if (!redirectUrl) {
                // 默认跳转到首页
                uni.switchTab({
                    url: '/pages/tabbar/home/home'
                })
                return
            }
            
            const decodedUrl = decodeURIComponent(redirectUrl)
            const normalizedPath = this.normalizePath(decodedUrl)
            
            // 判断是否是 tabBar 页面
            if (this.isTabBarPage(normalizedPath)) {
                uni.switchTab({
                    url: `/${normalizedPath}`
                })
            } else {
                // 非 tabBar 页面，检查是否包含查询参数
                if (decodedUrl.includes('?')) {
                    uni.redirectTo({
                        url: `/${decodedUrl}`
                    })
                } else {
                    uni.redirectTo({
                        url: `/${normalizedPath}`
                    })
                }
            }
        } catch (error) {
            console.error('登录成功跳转失败:', error)
            // 发生错误时跳转到首页
            uni.switchTab({
                url: '/pages/tabbar/home/home'
            })
        }
    }
}

export default RouterGuard