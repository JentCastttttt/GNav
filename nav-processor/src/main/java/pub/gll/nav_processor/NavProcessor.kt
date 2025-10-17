package pub.gll.nav_processor

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo
import pub.gll.nav_annotations.NavPage

/**
 * ==============================================
 * 🧭 NavProcessor.kt
 * ==============================================
 *
 * ## 功能简介
 * 这是一个 **KSP (Kotlin Symbol Processing)** 注解处理器，
 * 用于扫描所有使用 `@NavPage` 注解的 `@Composable` 函数，
 * 并自动生成：
 *
 * 1. 路由注册器：`GeneratedNavRegistrar`
 * 2. 跳转扩展函数：`goXxx()`、`getXxxRoute()`
 *
 * 目标是让 Jetpack Compose Navigation 的声明式路由配置更加自动化。
 *
 * ---
 * ### 💡 示例
 * ```kotlin
 * @NavPage("home")
 * @Composable fun HomePage(id: Int)
 * ```
 *
 * 自动生成：
 * ```kotlin
 * builder.composable("home?id={id}") { ... }
 * fun NavController.goHome(id: Int) { navigate("home?id=$id") }
 * ```
 *
 * ---
 * @author Leo
 * @since 1.0.0
 */
class NavProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    /** 记录已处理的符号，避免重复生成 */
    private val processedSymbols = mutableSetOf<String>()

    // ==============================================
    // 🔹 入口方法
    // ==============================================
    override fun process(resolver: Resolver): List<KSAnnotated> {
        // 1️⃣ 找出所有使用了 @NavPage 的函数
        val symbols = resolver.getSymbolsWithAnnotation(NavPage::class.java.name)
            .filterIsInstance<KSFunctionDeclaration>()
            .toList()

        // 过滤出有效/无效符号（无效留给下一轮）
        val deferred = symbols.filterNot { it.validate() }
        val valid = symbols.filter { it.validate() }
        if (valid.isEmpty()) return deferred

        // 2️⃣ 构建生成所需的基础类型引用
        val navGraphBuilder = ClassName("androidx.navigation", "NavGraphBuilder")
        val navController = ClassName("androidx.navigation", "NavController")
        val navBackStackEntry = ClassName("androidx.navigation", "NavBackStackEntry")
        val composableMember = MemberName("androidx.navigation.compose", "composable")
        val disposableEffect = MemberName("androidx.compose.runtime", "DisposableEffect")
        val navAnalytics = ClassName("pub.gll.nav_api", "NavAnalytics")
        val navRouteRegistrar = ClassName("pub.gll.nav_api", "NavRouteRegistrar")

        // 3️⃣ 输出文件定义
        val className = ClassName("pub.gll.generated", "GeneratedNavRegistrar")
        val fileBuilder = FileSpec.builder(className.packageName, className.simpleName)

        // 4️⃣ 主体代码构建容器
        val methodBody = CodeBlock.builder()
        val extensions = mutableListOf<FunSpec>()
        val initBlock = CodeBlock.builder()

        // ==============================================
        // 🔹 处理每个 @NavPage 函数
        // ==============================================
        valid.forEach { func ->
            processNavPageFunction(
                func = func,
                navGraphBuilder = navGraphBuilder,
                navController = navController,
                navBackStackEntry = navBackStackEntry,
                composableMember = composableMember,
                disposableEffect = disposableEffect,
                navAnalytics = navAnalytics,
                methodBody = methodBody,
                extensions = extensions,
                initBlock = initBlock
            )
        }

        // ==============================================
        // 🔹 构建最终的注册器类
        // ==============================================
        val classSpec = TypeSpec.classBuilder(className)
            .addSuperinterface(navRouteRegistrar)
            .addInitializerBlock(initBlock.build())
            .addFunction(
                FunSpec.builder("registerRoutes")
                    .addModifiers(KModifier.OVERRIDE)
                    .addParameter("builder", navGraphBuilder)
                    .addParameter("navController", navController)
                    .addParameter("analytics", navAnalytics.copy(nullable = true))
                    .addCode(methodBody.build())
                    .build()
            )
            .build()

        // 输出所有扩展函数
        fileBuilder.addType(classSpec)
        extensions.forEach { fileBuilder.addFunction(it) }

        // ==============================================
        // 🔹 写入生成文件
        // ==============================================
        fileBuilder.build().writeTo(
            codeGenerator,
            Dependencies(
                aggregating = true,
                sources = valid.mapNotNull { it.containingFile }.toTypedArray()
            )
        )

        return deferred
    }

    // ==============================================
    // 🔹 核心逻辑：处理单个 NavPage 函数
    // ==============================================
    private fun processNavPageFunction(
        func: KSFunctionDeclaration,
        navGraphBuilder: ClassName,
        navController: ClassName,
        navBackStackEntry: ClassName,
        composableMember: MemberName,
        disposableEffect: MemberName,
        navAnalytics: ClassName,
        methodBody: CodeBlock.Builder,
        extensions: MutableList<FunSpec>,
        initBlock: CodeBlock.Builder
    ) {
        // --- 读取注解信息 ---
        val annotation = func.annotations.first { it.shortName.asString() == "NavPage" }
        val routeArg = annotation.arguments.find { it.name?.asString() == "route" }?.value as? String
        val interceptorsArg = annotation.arguments.find { it.name?.asString() == "interceptors" }?.value as? List<KSType>
        val interceptorClassNames = interceptorsArg?.mapNotNull { it.declaration.qualifiedName?.asString() } ?: emptyList()

        processedSymbols.add(func.qualifiedName?.asString() ?: "")

        // --- 解析函数参数 ---
        val params = func.parameters.mapNotNull { param ->
            val name = param.name?.asString() ?: return@mapNotNull null
            val type = param.type.resolve()
            val qn = type.declaration.qualifiedName?.asString() ?: type.toString()
            ParamInfo(
                name = name,
                qname = qn,
                isNullable = type.nullability == Nullability.NULLABLE,
                isNavController = qn == navController.canonicalName,
                typeName = type.toTypeName()
            )
        }

        // --- 生成 route path ---
        val route = RouteBuilder.buildRoute(func, routeArg, params)
        val funcClassName = ClassName(func.packageName.asString(), func.simpleName.asString())

        // --- 构建 composable 注册语句 ---
        methodBody.add("%L.%M(%S) { backStackEntry: %T ->\n", "builder", composableMember, route, navBackStackEntry)
        buildComposableBody(methodBody, funcClassName, params, navController, disposableEffect, navAnalytics, route)
        methodBody.add("}\n\n")

        // --- 注册拦截器 ---
        interceptorClassNames.forEach { fqcn ->
            initBlock.addStatement("pub.gll.nav.GNav.addInterceptor(%S, %T())", route, ClassName.bestGuess(fqcn))
        }

        // --- 生成 goXxx() / getXxxRoute() 扩展函数 ---
        extensions += NavExtensionBuilder.build(func, route, routeArg, params, navController)
        extensions += buildGetRouteFunction(func, route, routeArg, params)
    }

    // ==============================================
    // 🔹 构建 composable() 的内容部分
    // ==============================================
    private fun buildComposableBody(
        body: CodeBlock.Builder,
        funcClassName: ClassName,
        params: List<ParamInfo>,
        navController: ClassName,
        disposableEffect: MemberName,
        navAnalytics: ClassName,
        route: String
    ) {
        val callArgs = mutableListOf<String>()

        // 读取参数
        params.forEach { p ->
            if (p.isNavController) {
                body.add("  val %L = navController\n", p.name)
            } else {
                body.add("  %L\n", ArgReaderGenerator.generate(p))
            }
            callArgs += p.name
        }

        // 参数 map（供分析埋点）
        body.add("  val paramsMap = mutableMapOf<String, Any?>()\n")
        params.filterNot { it.isNavController }
            .forEach { body.add("  paramsMap[%S] = %L\n", it.name, it.name) }

        // 添加生命周期监听
        body.add(
            """
            |  %M(Unit) {
            |    val prevRoute = navController.previousBackStackEntry?.destination?.route
            |    analytics?.onPageEnter(%S, prevRoute, paramsMap)
            |    onDispose { analytics?.onPageExit(%S) }
            |  }
            |
            """.trimMargin(), disposableEffect, route, route
        )

        // 调用目标页面
        body.add("  %T(${callArgs.joinToString(", ")})\n", funcClassName)
    }

    // ==============================================
    // 🔹 生成 getXxxRoute() 方法
    // ==============================================
    private fun buildGetRouteFunction(
        func: KSFunctionDeclaration,
        route: String,
        routeArg: String?,
        params: List<ParamInfo>
    ): FunSpec {
        val baseName = if (!routeArg.isNullOrBlank()) routeArg else func.simpleName.asString().removeSuffix("Page")
        val funBuilder = FunSpec.builder("get${baseName.replaceFirstChar { it.uppercase() }}Route")
            .returns(String::class)

        val nonNavParams = params.filterNot { it.isNavController }

        if (nonNavParams.isNotEmpty()) {
            nonNavParams.forEach { funBuilder.addParameter(it.name, it.typeName) }

            val routeBuilder = StringBuilder("\"${route.split("/")[0]}")
            nonNavParams.forEach { p ->
                routeBuilder.append("/\" + android.net.Uri.encode(${p.name}.toString()) + \"")
            }
            routeBuilder.append("\"")

            funBuilder.addStatement("return %L", routeBuilder.toString())
        } else {
            funBuilder.addStatement("return %S", route)
        }

        return funBuilder.build()
    }
}
