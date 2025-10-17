package pub.gll.nav_processor

import com.google.devtools.ksp.symbol.KSFunctionDeclaration

/**
 * RouteBuilder
 * -------------------
 * 用于根据目标函数 (@Destination 标注的页面函数)
 * 自动生成导航路由字符串。
 *
 * 🔧 优化要点：
 * - 支持显式 route 优先级最高；
 * - 自动将函数名转换为 kebab_case（更通用于前端路由风格）；
 * - 支持过滤多余斜杠；
 * - 参数自动附带 {param} 占位符；
 * - 保证空 route 时不生成非法路径；
 *
 * 示例：
 *  ----------------------------------------------------------------
 *  fun ExamplePage(id: Int, name: String)
 *  → "example/{id}/{name}"
 *
 *  fun UserProfilePage()
 *  → "user_profile"
 *
 *  @Destination("custom/route")
 *  fun AnyPage()
 *  → "custom/route"
 *  ----------------------------------------------------------------
 */
object RouteBuilder {

    /**
     * 构建导航路由字符串
     *
     * @param func      页面函数声明
     * @param routeArg  注解中可选 route 参数（优先使用）
     * @param params    函数参数信息（用于生成占位符）
     * @return          导航路由字符串，例如 `"user_detail/{id}/{name}"`
     */
    fun buildRoute(
        func: KSFunctionDeclaration,
        routeArg: String?,
        params: List<ParamInfo>
    ): String {
        // region ===== 1️⃣ 优先使用注解中显式指定的 route =====
        routeArg?.trim()?.takeIf { it.isNotEmpty() }?.let { explicit ->
            // 去除多余的斜杠（防止 "//"）
            return explicit.trim('/').replace(Regex("/{2,}"), "/")
        }
        // endregion

        // region ===== 2️⃣ 自动生成 base 路径 =====
        val funcName = func.simpleName.asString()
        val base = funcName
            .removeSuffix("Page")              // 去掉惯用后缀
            .replace(Regex("([a-z])([A-Z])"), "$1_$2") // 驼峰转连接符（userDetail → user_detail）
            .replace(Regex("[^a-zA-Z0-9_-]"), "")       // 清理非法字符
            .lowercase()
            .ifBlank { "unknown" }             // 保底防空
        // endregion

        // region ===== 3️⃣ 生成参数占位部分 =====
        val paramPlaceholders = params
            .filterNot { it.isNavController }   // 忽略 NavController 参数
            .map { "{${it.name}}" }

        val paramsPart = paramPlaceholders.joinToString("/")
        // endregion

        // region ===== 4️⃣ 拼接最终路径 =====
        // 如果存在参数 → "base/{param}/{param2}"
        // 否则 → "base"
        val route = if (paramsPart.isNotEmpty()) "$base/$paramsPart" else base

        // 清理路径：去掉首尾斜杠、防止重复斜杠
        return route.trim('/').replace(Regex("/{2,}"), "/")
        // endregion
    }
}
