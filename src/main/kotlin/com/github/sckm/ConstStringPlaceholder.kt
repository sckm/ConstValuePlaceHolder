package com.github.sckm

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.idea.caches.resolve.analyze
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.resolve.constants.CompileTimeConstant
import org.jetbrains.kotlin.resolve.constants.evaluate.ConstantExpressionEvaluator
import org.jetbrains.kotlin.resolve.lazy.BodyResolveMode

class ConstStringPlaceholder : FoldingBuilderEx() {
    override fun getPlaceholderText(node: ASTNode): String? {

        val psi = node.psi
        return when(psi) {
            is KtSimpleNameStringTemplateEntry -> "simple"
            is KtBlockStringTemplateEntry -> "Block"
            is KtNameReferenceExpression -> {
                val bindingContext = (psi as KtElement).analyze(BodyResolveMode.PARTIAL)
                return (ConstantExpressionEvaluator.getConstant(psi, bindingContext) as CompileTimeConstant)
                        .toConstantValue(bindingContext.getType(psi)!!)
                        .value as? String
            }
            else -> null
        }
    }

    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        val descriptors = mutableListOf<FoldingDescriptor>()
        root.acceptChildren(object : KtTreeVisitor<PsiElement>() {
            override fun visitSimpleNameStringTemplateEntry(entry: KtSimpleNameStringTemplateEntry, data: PsiElement?): Void? {
                super.visitSimpleNameStringTemplateEntry(entry, data)
                val nameRef = entry.expression as? KtNameReferenceExpression ?: throw IllegalStateException()
                descriptors += FoldingDescriptor(nameRef, nameRef.textRange)
                return null
            }

            override fun visitBlockStringTemplateEntry(entry: KtBlockStringTemplateEntry, data: PsiElement?): Void? {
                super.visitBlockStringTemplateEntry(entry, data)
                descriptors += FoldingDescriptor(entry, entry.textRange)
                return null
            }
        })
        return descriptors.toTypedArray()
    }

    override fun isCollapsedByDefault(node: ASTNode): Boolean {
        return true
    }
}


class Const {
    companion object {

        const val CONST_VAL = "const value"
    }
}

class Example {
    fun a() {
        val str = "str"
        println("value = ${Const.CONST_VAL} $str")
    }
}