package pub.gll.nav_processor

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

/**
 * 🔧 提供给 KSP 框架的入口工厂类。
 * 每个注解处理器模块必须实现 SymbolProcessorProvider，
 * 用于创建 SymbolProcessor 实例。
 */
class NavProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        // 从 KSP 环境中获取必要的工具类
        return NavProcessor(environment.codeGenerator, environment.logger)
    }
}
