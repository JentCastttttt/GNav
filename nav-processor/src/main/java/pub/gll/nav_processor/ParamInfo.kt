package pub.gll.nav_processor

import com.squareup.kotlinpoet.TypeName

/**
 * ==============================================
 * 🔹 ParamInfo
 * ==============================================
 *
 * 描述页面目标函数（Composable 或普通函数）的参数信息。
 * 主要用于注解处理器（KSP）在编译期收集参数数据，
 * 并在生成导航扩展方法或路由模板时使用。
 *
 * 框架内部用途：
 * 1. 判断参数是否为 NavController，以决定是否在 navigate 调用中传递。
 * 2. 构建路由路径模板，例如 "profile/{userId}/{userName}"。
 * 3. 生成 NavController 扩展方法的参数列表。
 * 4. 生成从 NavBackStackEntry 安全读取参数的代码。
 *
 * ---
 * ### 示例
 *
 * ```kotlin
 * val param = ParamInfo(
 *     name = "userId",
 *     qname = "kotlin.Int",
 *     isNullable = false,
 *     isNavController = false,
 *     typeName = INT
 * )
 * ```
 *
 * 该信息可用于生成如下扩展方法：
 *
 * ```kotlin
 * fun NavController.goProfile(userId: Int) {
 *     navigate("profile/" + Uri.encode(userId.toString()))
 * }
 * ```
 *
 * @property name 参数名称
 * @property qname 参数完整限定类型名（如 kotlin.String、kotlin.Int）
 * @property isNullable 参数是否可空
 * @property isNavController 参数是否为 NavController（通常第一个参数）
 * @property typeName KotlinPoet 对应的 TypeName，用于代码生成
 */
data class ParamInfo(
    val name: String,              // 参数名
    val qname: String,             // 完整限定类型名
    val isNullable: Boolean,       // 是否可空
    val isNavController: Boolean,  // 是否是 NavController
    val typeName: TypeName         // KotlinPoet 类型对象
)
