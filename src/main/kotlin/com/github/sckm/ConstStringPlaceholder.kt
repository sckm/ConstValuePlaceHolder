package com.github.sckm

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.KtBlockStringTemplateEntry
import org.jetbrains.kotlin.psi.KtSimpleNameStringTemplateEntry
import org.jetbrains.kotlin.psi.KtTreeVisitor

class ConstStringPlaceholder : FoldingBuilderEx() {
    override fun getPlaceholderText(node: ASTNode): String? {
        return "placeholder"
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
            }
        })
        return descriptors.toTypedArray()
    }

    override fun isCollapsedByDefault(node: ASTNode): Boolean {
        return true
    }
}
