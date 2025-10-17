package pub.gll.nav_api

/**
 * 导航分析接口
 * 
 * 定义导航事件的分析跟踪接口，用于记录页面进入和退出事件。
 * 实现此接口可以在页面导航时执行自定义的分析逻辑，如记录用户行为、
 * 页面访问统计或性能监控等。
 * 
 * 在AppNavHost中使用此接口的实现来跟踪页面导航事件。
 */
interface NavAnalytics {
    /**
     * 页面进入事件回调
     * 
     * 当导航到新页面时触发此方法。
     * 
     * @param currentRoute 当前进入的路由路径
     * @param previousRoute 前一个路由路径，如果是应用启动的第一个页面则为null
     * @param params 导航参数，包含传递给页面的所有参数
     */
    fun onPageEnter(currentRoute: String, previousRoute: String?, params: Map<String, Any?>)
    
    /**
     * 页面退出事件回调
     * 
     * 当离开当前页面时触发此方法。
     * 
     * @param currentRoute 当前退出的路由路径
     */
    fun onPageExit(currentRoute: String)
}