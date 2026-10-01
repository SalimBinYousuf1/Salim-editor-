package com.example.util

import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient

object PrintHelper {
    fun printDocument(context: Context, title: String, content: String, isMarkdown: Boolean = false) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager ?: return

        val webView = WebView(context)
        val htmlBody = if (isMarkdown) {
            MarkdownParser.renderToHtml(content)
        } else {
            "<pre style=\"font-family: monospace; white-space: pre-wrap; font-size: 12pt; line-height: 1.5;\">" +
                    content.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;") +
                    "</pre>"
        }

        val fullHtml = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8">
                <title>$title</title>
                <style>
                    body {
                        font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
                        padding: 24px;
                        color: #1a1a1a;
                        line-height: 1.6;
                        font-size: 11pt;
                    }
                    h1, h2, h3, h4 { color: #007aff; }
                    code { background: #f0f0f5; padding: 2px 4px; border-radius: 4px; font-family: monospace; }
                    pre { background: #f4f4f7; padding: 12px; border-radius: 6px; overflow-x: auto; }
                    blockquote { border-left: 4px solid #007aff; margin-left: 0; padding-left: 14px; color: #555; }
                    table { border-collapse: collapse; width: 100%; margin: 16px 0; }
                    th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
                    th { background-color: #f2f2f7; }
                </style>
            </head>
            <body>
                $htmlBody
            </body>
            </html>
        """.trimIndent()

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                val printAdapter = webView.createPrintDocumentAdapter(title)
                val jobName = "$title - Editor Pro Export"
                printManager.print(
                    jobName,
                    printAdapter,
                    PrintAttributes.Builder()
                        .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                        .setResolution(PrintAttributes.Resolution("id", "res", 300, 300))
                        .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                        .build()
                )
            }
        }
        webView.loadDataWithBaseURL(null, fullHtml, "text/html", "UTF-8", null)
    }
}
