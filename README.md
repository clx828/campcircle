# 🎓 CampCircle 校园社交平台

<div align="center">

![CampCircle Logo](./campcircle-ma/static/logo.png)

**一个现代化的校园社交平台，连接校园生活的每一个精彩瞬间**

[![Vue 3](https://img.shields.io/badge/Vue-3.x-4FC08D?style=flat-square&logo=vue.js)](https://vuejs.org/)
[![uni-app](https://img.shields.io/badge/uni--app-3.x-2B9939?style=flat-square&logo=vue.js)](https://uniapp.dcloud.io/)
[![TypeScript](https://img.shields.io/badge/TypeScript-4.x-3178C6?style=flat-square&logo=typescript)](https://www.typescriptlang.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.x-6DB33F?style=flat-square&logo=spring)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-MIT-blue?style=flat-square)](LICENSE)

[在线体验](https://campcircle.example.com) · [功能演示](#-功能特色) · [快速开始](#-快速开始) · [技术文档](#-技术架构)

</div>

## 📖 项目简介

CampCircle 是一个专为校园用户打造的现代化社交平台，致力于为学生提供一个分享校园生活、学术交流、兴趣互动的数字化空间。

### 🌟 核心理念
- **连接校园** - 打破地域和专业界限，连接每一个校园用户
- **分享生活** - 记录和分享校园生活的美好瞬间
- **学术交流** - 促进学术讨论和知识分享
- **兴趣社交** - 基于共同兴趣建立社交关系

## 🚀 功能特色

### 📱 多端适配
- **微信小程序** - 原生小程序体验，无需下载安装
- **H5网页版** - 响应式设计，支持各种设备访问
- **App应用** - 原生应用性能，流畅的用户体验

### 🎯 核心功能
- **📝 内容发布** - 支持图文混排，多图上传，标签分类
- **💬 社交互动** - 点赞、评论、分享、关注等完整社交功能
- **🔍 智能搜索** - 基于Elasticsearch的全文搜索，快速找到感兴趣的内容
- **💌 消息通知** - 实时消息推送，不错过任何重要互动
- **👥 用户管理** - 完善的用户资料系统，个性化展示

### ✨ 技术亮点
- **🎨 现代化UI** - 基于最新设计规范，提供优雅的用户界面
- **⚡ 高性能** - Redis缓存 + CDN加速，毫秒级响应
- **🔒 安全可靠** - JWT认证 + 权限控制，保护用户数据安全
- **📊 数据驱动** - 完整的数据统计和分析功能
- **🔧 易于扩展** - 模块化架构，支持功能快速迭代

## 🛠 技术架构

### 前端技术栈
```
uni-app + Vue 3 + TypeScript + Pinia
├── 🎨 UI框架: wot-design-uni + TDesign Mobile
├── 📦 状态管理: Pinia
├── 🎯 类型检查: TypeScript
├── 🎪 样式处理: Sass
└── ⚡ 构建工具: Vite
```

### 后端技术栈
```
Spring Boot + MyBatis Plus + MySQL + Redis
├── 🏗️ 核心框架: Spring Boot 2.7.x
├── 🔐 安全认证: Spring Security + JWT
├── 💾 数据存储: MySQL 8.0 + Redis
├── 🔍 搜索引擎: Elasticsearch
├── ☁️ 云服务: 腾讯云COS + 微信开放平台
└── 📚 API文档: Knife4j (Swagger)
```

## 🎯 快速开始

### 环境要求
- **Node.js** >= 16.0.0
- **Java** >= 8
- **MySQL** >= 8.0
- **Redis** >= 6.0

### 前端启动
```bash
# 克隆项目
git clone https://github.com/your-username/campcircle.git

# 进入前端目录
cd campcircle/campcircle-ma

# 安装依赖
npm install

# 启动开发服务器
npm run dev:h5          # H5版本
npm run dev:mp-weixin   # 微信小程序版本

# 构建生产版本
npm run build:h5        # 构建H5版本
npm run build:mp-weixin # 构建微信小程序版本
```

### 后端启动
```bash
# 进入后端目录
cd campcircle/campcircle-backend

# 配置数据库连接
# 编辑 src/main/resources/application.yml

# 启动应用
./mvnw spring-boot:run

# 或使用IDE直接运行 CampcircleApplication.java
```

### 📱 体验方式
1. **在线体验**: [https://campcircle.example.com](https://campcircle.example.com)
2. **微信小程序**: 搜索"CampCircle"或扫描二维码
3. **本地开发**: 按照上述步骤启动本地环境

## 📸 功能截图

<div align="center">

| 首页动态 | 发布内容 | 个人中心 | 消息通知 |
|---------|---------|---------|---------|
| ![首页](./docs/screenshots/home.png) | ![发布](./docs/screenshots/publish.png) | ![个人](./docs/screenshots/profile.png) | ![消息](./docs/screenshots/message.png) |

</div>

## 📁 项目结构

```
campcircle/
├── 📱 campcircle-ma/              # 前端项目 (uni-app)
│   ├── 📄 pages/                  # 页面文件
│   │   ├── tabbar/               # 底部导航页面
│   │   ├── search/               # 搜索页面
│   │   ├── postDetail/           # 帖子详情
│   │   └── ...
│   ├── 🧩 components/            # 公共组件
│   │   ├── SocialCard.vue        # 社交卡片组件
│   │   ├── CommentPopup.vue      # 评论弹窗
│   │   └── ...
│   ├── 🔧 api/                   # API接口
│   ├── 📦 stores/                # 状态管理
│   ├── 🎨 static/                # 静态资源
│   └── 📚 docs/                  # 技术文档
├── 🖥️ campcircle-backend/        # 后端项目 (Spring Boot)
│   ├── src/main/java/            # Java源码
│   ├── src/main/resources/       # 配置文件
│   └── ...
└── 📖 README.md                  # 项目说明
```

## 🔧 核心功能模块

### 🏠 首页动态
- **📰 信息流展示** - 时间线排序的动态内容
- **🔥 热门推荐** - 基于热度算法的内容推荐
- **🎯 个性化推荐** - 基于用户兴趣的智能推荐

### 👤 用户系统
- **🔐 多种登录方式** - 账号密码、微信一键登录
- **📝 资料管理** - 头像、昵称、简介、学校信息
- **👥 社交关系** - 关注/粉丝系统，社交网络构建

### 📝 内容创作
- **✍️ 富文本编辑** - 支持图文混排，表情符号
- **📷 多媒体支持** - 最多9张图片上传
- **🏷️ 标签系统** - 内容分类和发现
- **👻 匿名发布** - 保护隐私的匿名分享

### 💬 社交互动
- **👍 点赞收藏** - 一键点赞，收藏夹管理
- **💭 评论系统** - 多级评论，实时互动
- **📤 分享功能** - 微信、朋友圈等多平台分享
- **🔔 消息通知** - 实时推送，不错过任何互动

### 🔍 搜索发现
- **🔎 全文搜索** - 基于Elasticsearch的智能搜索
- **👥 用户搜索** - 快速找到感兴趣的用户
- **🏷️ 标签搜索** - 按兴趣分类浏览内容
- **📈 热门排行** - 实时热门内容推荐

## 🎨 设计理念

### 用户体验优先
- **🎯 简洁直观** - 清晰的信息架构，降低学习成本
- **⚡ 快速响应** - 优化加载速度，提升使用体验
- **📱 移动优先** - 专为移动设备优化的交互设计
- **🎨 视觉统一** - 一致的设计语言和视觉规范

### 技术架构优势
- **🏗️ 模块化设计** - 高内聚低耦合，易于维护扩展
- **🔄 前后端分离** - 独立开发部署，提高开发效率
- **☁️ 云原生架构** - 支持弹性扩容，适应业务增长
- **🛡️ 安全可靠** - 多层安全防护，保障数据安全

## 🚀 系统特性

### ⚡ 高性能
- **💾 Redis缓存** - 热点数据缓存，毫秒级响应
- **🗃️ 数据库优化** - 索引优化，查询性能提升
- **🌐 CDN加速** - 全球内容分发，图片加载更快
- **📄 分页优化** - 智能分页，减少数据传输

### 🛡️ 高可用
- **🔧 异常处理** - 完善的异常捕获和处理机制
- **📊 监控告警** - 实时监控系统状态
- **💾 数据备份** - 定期备份，数据安全保障
- **⬇️ 服务降级** - 高峰期自动降级，保证核心功能

### 🔒 安全性
- **🎫 JWT认证** - 无状态认证，安全可靠
- **🔐 权限控制** - 细粒度权限管理
- **🛡️ 数据校验** - 多层数据验证和过滤
- **🚫 攻击防护** - SQL注入、XSS等攻击防护

## 🤝 参与贡献

我们欢迎所有形式的贡献，无论是新功能、bug修复、文档改进还是设计建议。

### 贡献方式
1. **🍴 Fork** 本仓库
2. **🌿 创建** 特性分支 (`git checkout -b feature/amazing-feature`)
3. **💾 提交** 你的修改 (`git commit -m 'Add amazing feature'`)
4. **📤 推送** 到分支 (`git push origin feature/amazing-feature`)
5. **🔀 创建** Pull Request

### 开发规范
- **📝 代码规范**: 遵循项目的ESLint和Prettier配置
- **🧪 测试覆盖**: 新功能需要包含相应的测试用例
- **📚 文档更新**: 重要变更需要更新相关文档
- **💬 提交信息**: 使用语义化的提交信息格式

## 📄 许可证

本项目基于 [MIT License](LICENSE) 开源协议，欢迎自由使用和修改。

## 🙏 致谢

感谢所有为 CampCircle 项目做出贡献的开发者和用户！

特别感谢以下开源项目：
- [Vue.js](https://vuejs.org/) - 渐进式JavaScript框架
- [uni-app](https://uniapp.dcloud.io/) - 跨平台开发框架
- [Spring Boot](https://spring.io/projects/spring-boot) - Java应用开发框架
- [wot-design-uni](https://wot-design-uni.cn/) - uni-app UI组件库

---

<div align="center">

**⭐ 如果这个项目对你有帮助，请给我们一个 Star！**

[🏠 首页](https://campcircle.example.com) · [📖 文档](./docs) · [🐛 反馈](https://github.com/your-username/campcircle/issues) · [💬 讨论](https://github.com/your-username/campcircle/discussions)

Made with ❤️ by CampCircle Team

</div>
