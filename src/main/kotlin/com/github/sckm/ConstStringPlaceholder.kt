package com.github.sckm

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.console.actions.errorNotification
import org.jetbrains.kotlin.idea.caches.resolve.analyze
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.resolve.constants.ConstantValue
import org.jetbrains.kotlin.resolve.constants.evaluate.ConstantExpressionEvaluator
import org.jetbrains.kotlin.resolve.lazy.BodyResolveMode

class ConstStringPlaceholder : FoldingBuilderEx() {
    override fun getPlaceholderText(node: ASTNode): String? {
        val psi = node.psi
        return when(psi) {
            is KtSimpleNameStringTemplateEntry -> {
                val eval = psi.expression as? KtNameReferenceExpression ?: throw IllegalStateException()
                eval.toConstantValueOrNull()?.value.toString()
            }
            else -> {
                errorNotification(node.psi.project, "unexpected Type: ${psi.javaClass}")
                null
            }
        }
    }

    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        val descriptors = mutableListOf<FoldingDescriptor>()
        root.acceptChildren(object : KtTreeVisitor<PsiElement>() {
            override fun visitSimpleNameStringTemplateEntry(entry: KtSimpleNameStringTemplateEntry, data: PsiElement?): Void? {
                super.visitSimpleNameStringTemplateEntry(entry, data)
                descriptors += FoldingDescriptor(entry, entry.textRange)
                return null
            }

            override fun visitBlockStringTemplateEntry(entry: KtBlockStringTemplateEntry, data: PsiElement?): Void? {
                return super.visitBlockStringTemplateEntry(entry, data)
                // TODO handle BlockString
            }
        })
        return descriptors.toTypedArray()
    }

    override fun isCollapsedByDefault(node: ASTNode): Boolean {
        return true
    }

    private fun KtExpression.toConstantValueOrNull(): ConstantValue<*>? {
        val bindingContext = analyze(BodyResolveMode.PARTIAL)
        val kotlinType = bindingContext.getType(this) ?: return null
        val compileTimeConstant =
                ConstantExpressionEvaluator.getConstant(this, bindingContext) ?: return null
        return compileTimeConstant.toConstantValue(kotlinType)
    }
}
