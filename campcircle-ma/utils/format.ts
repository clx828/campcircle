/**
 * 创建iOS兼容的Date对象
 * @param time 时间字符串或时间戳
 * @returns Date对象
 */
const createCompatibleDate = (time: string | number): Date => {
    if (typeof time === 'number') {
        return new Date(time)
    }

    // 如果是字符串，需要处理iOS不兼容的格式
    let timeStr = String(time)

    // 处理包含时区信息的字符串，如 "Sun Jul 06 2025 14:50:05 GMT+0800 (中国标准时间)"
    if (timeStr.includes('GMT') || timeStr.includes('CST') || timeStr.includes('中国标准时间')) {
        // 尝试提取时间戳或转换为ISO格式
        const date = new Date(timeStr)
        if (!isNaN(date.getTime())) {
            return date
        }

        // 如果直接转换失败，尝试解析时间戳
        const timestampMatch = timeStr.match(/(\d{4})\s+(\d{2}):(\d{2}):(\d{2})/)
        if (timestampMatch) {
            // 尝试从字符串中提取年月日时分秒
            const yearMatch = timeStr.match(/(\d{4})/)
            const monthMatch = timeStr.match(/(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)/)
            const dayMatch = timeStr.match(/\s(\d{1,2})\s/)
            const timeMatch = timeStr.match(/(\d{2}):(\d{2}):(\d{2})/)

            if (yearMatch && monthMatch && dayMatch && timeMatch) {
                const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec']
                const year = parseInt(yearMatch[1])
                const month = months.indexOf(monthMatch[1])
                const day = parseInt(dayMatch[1])
                const hour = parseInt(timeMatch[1])
                const minute = parseInt(timeMatch[2])
                const second = parseInt(timeMatch[3])

                return new Date(year, month, day, hour, minute, second)
            }
        }
    }

    // 处理ISO格式或其他标准格式
    if (timeStr.includes('T') || timeStr.includes('-')) {
        // 确保格式符合iOS要求
        timeStr = timeStr.replace(/\s/g, 'T').replace(/T+/g, 'T')
        if (!timeStr.includes('T') && timeStr.includes(' ')) {
            timeStr = timeStr.replace(' ', 'T')
        }
    }

    const date = new Date(timeStr)

    // 如果仍然无法解析，尝试作为时间戳处理
    if (isNaN(date.getTime())) {
        const timestamp = parseInt(timeStr)
        if (!isNaN(timestamp)) {
            return new Date(timestamp)
        }

        // 最后的回退：返回当前时间
        console.warn('无法解析时间格式:', time)
        return new Date()
    }

    return date
}

/**
 * 格式化时间
 * @param time 时间字符串或时间戳
 * @returns 格式化后的时间字符串
 */
export const formatTime = (time: string | number) => {
    try {
        const date = createCompatibleDate(time)
        const now = new Date()
        const diff = now.getTime() - date.getTime()

        // 小于1分钟
        if (diff < 60000) {
            return '刚刚'
        }
        // 小于1小时
        if (diff < 3600000) {
            return `${Math.floor(diff / 60000)}分钟前`
        }
        // 小于24小时
        if (diff < 86400000) {
            return `${Math.floor(diff / 3600000)}小时前`
        }
        // 小于30天
        if (diff < 2592000000) {
            return `${Math.floor(diff / 86400000)}天前`
        }
        // 大于30天
        return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
    } catch (error) {
        console.error('时间格式化失败:', error)
        return '刚刚'
    }
}

/**
 * 导出iOS兼容的日期创建函数供其他模块使用
 */
export { createCompatibleDate }