package com.example.ui.editor

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.EditorColors

data class EditorColorScheme(
    val id: String,
    val name: String,
    val isDark: Boolean,
    val background: Color,
    val surface: Color,
    val gutterBackground: Color,
    val gutterDivider: Color,
    val gutterText: Color,
    val gutterActiveText: Color,
    val activeLineBg: Color,
    val text: Color,
    val cursor: Color,
    val selection: Color,
    val keyword: Color,
    val string: Color,
    val number: Color,
    val comment: Color,
    val type: Color,
    val punctuation: Color,
    val tag: Color,
    val attribute: Color,
    val searchMatch: Color,
    val searchMatchCurrent: Color
)

object EditorThemes {
    val OneDarkPro = EditorColorScheme(
        id = "one_dark_pro",
        name = "One Dark Pro",
        isDark = true,
        background = Color(0xFF1E1E24),
        surface = Color(0xFF282C34),
        gutterBackground = Color(0xFF21252B),
        gutterDivider = Color(0xFF333842),
        gutterText = Color(0xFF5C6370),
        gutterActiveText = Color(0xFFABB2BF),
        activeLineBg = Color(0xFF2C313C),
        text = Color(0xFFABB2BF),
        cursor = Color(0xFF528BFF),
        selection = Color(0x403E4451),
        keyword = Color(0xFFC678DD),
        string = Color(0xFF98C379),
        number = Color(0xFFD19A66),
        comment = Color(0xFF5C6370),
        type = Color(0xFFE5C07B),
        punctuation = Color(0xFFABB2BF),
        tag = Color(0xFFE06C75),
        attribute = Color(0xFFD19A66),
        searchMatch = Color(0x66E5C07B),
        searchMatchCurrent = Color(0xFFE5C07B)
    )

    val Dracula = EditorColorScheme(
        id = "dracula",
        name = "Dracula Pro",
        isDark = true,
        background = Color(0xFF282A36),
        surface = Color(0xFF21222C),
        gutterBackground = Color(0xFF21222C),
        gutterDivider = Color(0xFF44475A),
        gutterText = Color(0xFF6272A4),
        gutterActiveText = Color(0xFFF8F8F2),
        activeLineBg = Color(0xFF44475A).copy(alpha = 0.35f),
        text = Color(0xFFF8F8F2),
        cursor = Color(0xFFAE81FF),
        selection = Color(0x5544475A),
        keyword = Color(0xFFFF79C6),
        string = Color(0xFFF1FA8C),
        number = Color(0xFFBD93F9),
        comment = Color(0xFF6272A4),
        type = Color(0xFF8BE9FD),
        punctuation = Color(0xFFF8F8F2),
        tag = Color(0xFFFF79C6),
        attribute = Color(0xFF50FA7B),
        searchMatch = Color(0x66FFB86C),
        searchMatchCurrent = Color(0xFFFFB86C)
    )

    val Monokai = EditorColorScheme(
        id = "monokai",
        name = "Monokai Pro",
        isDark = true,
        background = Color(0xFF272822),
        surface = Color(0xFF1E1F1C),
        gutterBackground = Color(0xFF1E1F1C),
        gutterDivider = Color(0xFF3E3D32),
        gutterText = Color(0xFF75715E),
        gutterActiveText = Color(0xFFF8F8F2),
        activeLineBg = Color(0xFF3E3D32).copy(alpha = 0.45f),
        text = Color(0xFFF8F8F2),
        cursor = Color(0xFFF8F8F0),
        selection = Color(0x4449483E),
        keyword = Color(0xFFF92672),
        string = Color(0xFFE6DB74),
        number = Color(0xFFAE81FF),
        comment = Color(0xFF75715E),
        type = Color(0xFF66D9EF),
        punctuation = Color(0xFFF8F8F2),
        tag = Color(0xFFF92672),
        attribute = Color(0xFFA6E22E),
        searchMatch = Color(0x66FD971F),
        searchMatchCurrent = Color(0xFFFD971F)
    )

    val GitHubLight = EditorColorScheme(
        id = "github_light",
        name = "GitHub Light",
        isDark = false,
        background = Color(0xFFFFFFFF),
        surface = Color(0xFFF6F8FA),
        gutterBackground = Color(0xFFF6F8FA),
        gutterDivider = Color(0xFFE1E4E8),
        gutterText = Color(0xFF959DA5),
        gutterActiveText = Color(0xFF24292E),
        activeLineBg = Color(0xFFF1F8FF),
        text = Color(0xFF24292E),
        cursor = Color(0xFF0366D6),
        selection = Color(0x330366D6),
        keyword = Color(0xFFD73A49),
        string = Color(0xFF032F62),
        number = Color(0xFF005CC5),
        comment = Color(0xFF6A737D),
        type = Color(0xFF6F42C1),
        punctuation = Color(0xFF24292E),
        tag = Color(0xFF22863A),
        attribute = Color(0xFFE36209),
        searchMatch = Color(0x66FFDF5D),
        searchMatchCurrent = Color(0xFFFFDF5D)
    )

    val SolarizedDark = EditorColorScheme(
        id = "solarized_dark",
        name = "Solarized Dark",
        isDark = true,
        background = Color(0xFF002B36),
        surface = Color(0xFF073642),
        gutterBackground = Color(0xFF073642),
        gutterDivider = Color(0xFF586E75),
        gutterText = Color(0xFF657B83),
        gutterActiveText = Color(0xFF93A1A1),
        activeLineBg = Color(0xFF073642),
        text = Color(0xFF839496),
        cursor = Color(0xFF268BD2),
        selection = Color(0x40586E75),
        keyword = Color(0xFF859900),
        string = Color(0xFF2AA198),
        number = Color(0xFFD33682),
        comment = Color(0xFF586E75),
        type = Color(0xFFB58900),
        punctuation = Color(0xFF839496),
        tag = Color(0xFF268BD2),
        attribute = Color(0xFF6C71C4),
        searchMatch = Color(0x66CB4B16),
        searchMatchCurrent = Color(0xFFCB4B16)
    )

    fun forAppTheme(custom: EditorColors): EditorColorScheme {
        return if (custom.isDark) {
            OneDarkPro.copy(
                background = custom.surface,
                surface = custom.secondarySurface,
                gutterBackground = custom.secondarySurface,
                gutterDivider = custom.divider
            )
        } else {
            GitHubLight.copy(
                background = custom.surface,
                surface = custom.secondarySurface,
                gutterBackground = custom.secondarySurface,
                gutterDivider = custom.divider
            )
        }
    }

    val allThemes = listOf(OneDarkPro, Dracula, Monokai, GitHubLight, SolarizedDark)
}
