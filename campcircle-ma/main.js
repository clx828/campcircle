import { createSSRApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import RouterGuard from './utils/routerGuard'

export function createApp() {
  const app = createSSRApp(App)
  const pinia = createPinia()
  
  // 安装插件的正确顺序
  app.use(pinia)         // 先安装 Pinia
  
  // 封装路由拦截逻辑
  const interceptRouter = (originalMethod) => {
    return function(options) {
      const path = options.url.split('?')[0]
      const canNavigate = RouterGuard.handleRoute({
        path: path,
        query: options.query
      })
      if (canNavigate) {
        return originalMethod.call(this, options)
      }
    }
  }
  
  // 拦截导航方法
  uni.navigateTo = interceptRouter(uni.navigateTo)
  uni.redirectTo = interceptRouter(uni.redirectTo)
  uni.reLaunch = interceptRouter(uni.reLaunch)
  
  return {
    app,
    pinia
  }
}