package com.kosiso.smartcount.ui.ui_utils

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

class PhoneNumberVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = text.text.take(10)
        var output = ""
        trimmed.forEachIndexed { index, c ->
            output += c
            when (index) {
                2 -> output += "-"
                5 -> output += "-"
            }
        }
        return TransformedText(
            text = AnnotatedString(output),
            offsetMapping = PhoneOffsetMapping
        )
    }
}

object PhoneOffsetMapping : OffsetMapping {
    override fun originalToTransformed(offset: Int): Int {
        return when {
            offset <= 2 -> offset
            offset <= 5 -> offset + 1
            else -> offset + 2
        }
    }

    override fun transformedToOriginal(offset: Int): Int {
        return when {
            offset <= 3 -> offset
            offset <= 7 -> offset - 1
            else -> offset - 2
        }
    }
}