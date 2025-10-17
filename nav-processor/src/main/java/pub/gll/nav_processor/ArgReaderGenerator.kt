package pub.gll.nav_processor

import com.squareup.kotlinpoet.CodeBlock

/**
 * ArgReaderGenerator
 * -------------------
 * 自动生成从 NavBackStackEntry 读取参数的 Kotlin 代码块。
 * 支持 String、数值类型、Boolean、以及其他类型的安全解析。
 *
 * 用于生成类似：
 * ```kotlin
 * val idStr = backStackEntry.arguments?.getString("id")
 * val id = idStr?.toIntOrNull() ?: 0
 * ```
 */
object ArgReaderGenerator {

    /**
     * 生成从 backStackEntry 中读取参数的代码块。
     *
     * @param p 参数元信息 [ParamInfo]
     * @return KotlinPoet [CodeBlock]
     */
    fun generate(p: ParamInfo): CodeBlock {
        val name = p.name
        val key = p.name
        val type = p.qname.removeSuffix("?")

        return when (type) {
            // region ==== String ====
            "kotlin.String" -> stringBlock(name, key, p.isNullable)

            // region ==== Int ====
            "kotlin.Int" -> numberBlock(name, key, "toIntOrNull", "0", p.isNullable)

            // region ==== Long ====
            "kotlin.Long" -> numberBlock(name, key, "toLongOrNull", "0L", p.isNullable)

            // region ==== Float ====
            "kotlin.Float" -> numberBlock(name, key, "toFloatOrNull", "0f", p.isNullable)

            // region ==== Double ====
            "kotlin.Double" -> numberBlock(name, key, "toDoubleOrNull", "0.0", p.isNullable)

            // region ==== Boolean ====
            "kotlin.Boolean" -> booleanBlock(name, key, p.isNullable)

            // region ==== 默认：字符串回退 ====
            else -> stringBlock(name, key, p.isNullable)
        }
    }

    // =====================================================================================
    // 🔹 String 处理
    private fun stringBlock(name: String, key: String, isNullable: Boolean): CodeBlock {
        return if (isNullable) {
            CodeBlock.of(
                """val %L = backStackEntry.arguments?.getString(%S)""",
                name, key
            )
        } else {
            CodeBlock.of(
                """val %L = backStackEntry.arguments?.getString(%S) ?: """"",
                name, key
            )
        }
    }

    // =====================================================================================
    // 🔹 通用数值类型处理
    private fun numberBlock(
        name: String,
        key: String,
        method: String,
        default: String,
        isNullable: Boolean
    ): CodeBlock {
        val base =
            "val %LStr = backStackEntry.arguments?.getString(%S)\n" +
                    "val %L = %LStr?.%L()%L"

        val fallback = if (isNullable) "" else " ?: $default"

        return CodeBlock.of(
            base,
            name, key, name, name, method, fallback
        )
    }

    // =====================================================================================
    // 🔹 Boolean 类型单独处理（因为 toBooleanStrictOrNull 安全性更高）
    private fun booleanBlock(name: String, key: String, isNullable: Boolean): CodeBlock {
        val base =
            "val %LStr = backStackEntry.arguments?.getString(%S)\n" +
                    "val %L = %LStr?.toBooleanStrictOrNull()%L"

        val fallback = if (isNullable) "" else " ?: false"

        return CodeBlock.of(
            base,
            name, key, name, name, fallback
        )
    }
}
