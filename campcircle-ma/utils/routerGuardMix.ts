import RouterGuard from '@/utils/routerGuard'
import { useUserStore } from '@/stores/userStore'

// 页面混入
export default  {
    onLoad(options: any) {
        RouterGuard.handleRoute({
            path: this.$scope?.route || '',
            query: options
        })
    },

    onShow() {
        // 获取当前页面路径
        const pages = getCurrentPages()
        const currentPage = pages[pages.length - 1]
        const route = currentPage?.route || ''

        // 如果是 tabBar 页面，需要特殊处理
        if (RouterGuard.isTabBarPage(route)) {
            const userStore = useUserStore()

            // 如果页面需要登录且用户未登录
            if (RouterGuard.needAuth(route) && !userStore.isLoggedIn) {
                console.log('TabBar页面需要登录，跳转到登录页面')

                // 延迟执行跳转，避免可能的闪烁
                setTimeout(() => {
                    uni.navigateTo({
                        url: `/pages/login/login?redirect=${encodeURIComponent(route)}`
                    })
                }, 50)

                return
            }
        }
    }
} 