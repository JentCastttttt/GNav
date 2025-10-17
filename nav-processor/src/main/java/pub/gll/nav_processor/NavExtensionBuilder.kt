package pub.gll.nav_processor

import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.CodeBlock

/**
 * ==============================================
 * 🧭 NavExtensionBuilder.kt
 * ==============================================
 *
 * ## 功能简介
 * 该对象用于为每个 `@NavPage` 标注的目标页面函数
 * 自动生成对应的 **NavController 扩展函数**。
 *
 * 示例：
 * ```kotlin
 * fun NavController.goUserDetail(userId: Int, userName: String)
 * ```
 *
 * 生成后开发者即可直接调用：
 * ```kotlin
 * navController.goUserDetail(10, "Tom")
 * ```
 * 内部会自动拼接 Uri 编码后的路由路径：
 * ```
 * "user_detail/10/Tom"
 * ```
 *
 * ---
 * @author Leo
 * @since 1.0.0
 */
object NavExtensionBuilder {

    /**
     * 构建单个目标页面对应的 NavController 扩展函数。
     *
     * @param func 被 @NavPage 标注的 Composable 函数声明
     * @param route 导航路由模板（例如 `"user_detail/{userId}/{userName}"`）
     * @param routeArg 注解中指定的 route 参数（可能为空）
     * @param params 函数参数信息列表
     * @param navControllerClass `NavController` 的类定义
     *
     * @return 构建好的 [FunSpec]（KotlinPoet 函数模型）
     *
     * ### 生成效果示例
     * ```kotlin
     * fun NavController.goUserDetail(userId: Int, userName: String) {
     *     pub.gll.nav.GNav.navigate(this, "user_detail/" + Uri.encode(userId.toString()) + "/" + Uri.encode(userName))
     * }
     * ```
     */
    fun build(
        func: KSFunctionDeclaration,
        route: String,
        routeArg: String?,
        params: List<ParamInfo>,
        navControllerClass: ClassName
    ): FunSpec {
        val uriClass = ClassName("android.net", "Uri")

        // 基础页面名，如 HomePage -> "Home"
        val baseName = routeArg?.takeIf { it.isNotBlank() }
            ?: func.simpleName.asString().removeSuffix("Page")

        // 生成方法名：go + PascalCase
        val methodName = "go" + baseName.replaceFirstChar { it.uppercase() }

        // 过滤掉 NavController 类型参数
        val callParams = params.filterNot { it.isNavController }

        // 构建最终的路由表达式
        val navigateExpr = buildNavigateExpression(route, callParams, uriClass)

        // 定义扩展函数
        return FunSpec.builder(methodName)
            .receiver(navControllerClass)
            .addParameters(callParams.map { ParameterSpec.builder(it.name, it.typeName).build() })
            .addStatement("pub.gll.nav.GNav.navigate(this, %L)", navigateExpr)
            .build()
    }

    // =============================================================================================
    // 🔹 私有方法区：构建 navigate() 调用的路由表达式
    // =============================================================================================

    /**
     * 构造导航路径字符串表达式。
     *
     * 例如：
     * ```
     * route: "user_detail/{userId}/{userName}"
     * params: [userId, userName]
     * ->
     * "user_detail/" + Uri.encode(userId.toString()) + "/" + Uri.encode(userName)
     * ```
     *
     * @param route 路由模板字符串（含 `{param}` 占位符）
     * @param params 参数信息列表
     * @param uriClass [Uri] 的类定义（用于调用 `Uri.encode()`）
     *
     * @return 组装好的 [CodeBlock] 表达式
     */
    private fun buildNavigateExpression(
        route: String,
        params: List<ParamInfo>,
        uriClass: ClassName
    ): CodeBlock {
        val regex = "\\{(\\w+)}".toRegex()
        val builder = CodeBlock.builder()

        var lastIndex = 0
        var firstSegment = true

        // 解析 route 模板中的 {param} 占位符
        regex.findAll(route).forEach { match ->
            val start = match.range.first
            val end = match.range.last + 1

            // 处理静态片段（占位符前的部分）
            if (start > lastIndex) {
                val staticPart = route.substring(lastIndex, start)
                builder.add(if (firstSegment) "%S" else " + %S", staticPart)
                firstSegment = false
            }

            // 当前占位符名
            val paramName = match.groupValues[1]
            val param = params.firstOrNull { it.name == paramName }

            // 构建参数片段表达式（带 Uri.encode）
            val segment = buildParamSegment(param, uriClass)
            builder.add(if (firstSegment) "%L" else " + %L", segment)

            firstSegment = false
            lastIndex = end
        }

        // 添加最后的静态尾部（如果有）
        if (lastIndex < route.length) {
            val tail = route.substring(lastIndex)
            builder.add(if (firstSegment) "%S" else " + %S", tail)
        }

        return builder.build()
    }

    /**
     * 构建单个参数在路径中的表达式：
     *
     * - 对参数值调用 `Uri.encode()`，避免特殊字符破坏路径结构；
     * - 对可空参数进行空值保护；
     *
     * @param param 参数信息 [ParamInfo]（可能为 null）
     * @param uriClass Uri 类，用于调用 encode()
     *
     * @return 对应的 [CodeBlock]
     */
    private fun buildParamSegment(param: ParamInfo?, uriClass: ClassName): CodeBlock {
        return when {
            param == null -> CodeBlock.of("%S", "")
            param.isNullable -> CodeBlock.of(
                "%T.encode((%L)?.toString() ?: %S)",
                uriClass, param.name, ""
            )
            else -> CodeBlock.of("%T.encode(%L.toString())", uriClass, param.name)
        }
    }
}
