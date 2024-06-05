package com.github.ajalt.mordant.input

import com.github.ajalt.mordant.animation.animation
import com.github.ajalt.mordant.rendering.TextColors.brightWhite
import com.github.ajalt.mordant.rendering.TextStyle
import com.github.ajalt.mordant.rendering.TextStyles.dim
import com.github.ajalt.mordant.rendering.Widget
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.widgets.SelectList
import com.github.ajalt.mordant.widgets.Text

private fun Terminal.animateSelectList(
    singleSelect: Boolean,
    limit: Int,
    entries: List<SelectList.Entry>,
    title: Widget?,
    startingCursorIndex: Int,
    cursorMarker: String?,
    selectedMarker: String?,
    unselectedMarker: String?,
    captionBottom: Widget?,
    selectedStyle: TextStyle?,
    unselectedTitleStyle: TextStyle?,
    unselectedMarkerStyle: TextStyle?,
    clearOnExit: Boolean,
    onlyShowActiveDescription: Boolean,
): List<SelectList.Entry>? {
    // TODO: descriptions
    val items = entries.toMutableList()
    enterRawMode()?.use { scope ->
        val a = animation<Int> { i ->
            SelectList(
                items,
                title = title,
                cursorIndex = i,
                styleOnHover = singleSelect,
                selectedMarker = selectedMarker,
                cursorMarker = cursorMarker,
                unselectedMarker = unselectedMarker,
                selectedStyle = selectedStyle,
                unselectedTitleStyle = unselectedTitleStyle,
                unselectedMarkerStyle = unselectedMarkerStyle,
                captionBottom = captionBottom,
            )
        }

        try {
            var cursor = startingCursorIndex
            fun updateCursor(newCursor: Int) {
                cursor = newCursor.coerceIn(0, entries.lastIndex)
                if (onlyShowActiveDescription) {
                    items.forEachIndexed { i, entry ->
                        items[i] = entry.copy(
                            description = if (i == cursor) entries[i].description else null
                        )
                    }
                }
            }
            while (true) {
                a.update(cursor)
                val key = scope.readKey()
                val entry = items[cursor]
                when {
                    key == null -> return null
                    key.isCtrlC() -> return null
                    key == KeyboardEvent("ArrowUp") -> updateCursor(cursor - 1)
                    key == KeyboardEvent("ArrowDown") -> updateCursor(cursor + 1)

                    !singleSelect && key == KeyboardEvent("x") -> {
                        if (entry.selected || items.count { it.selected } < limit) {
                            items[cursor] = entry.copy(selected = !entry.selected)
                        }
                    }

                    key == KeyboardEvent("Enter") -> {
                        if (singleSelect) return listOf(entry)
                        return items
                    }
                }
            }
        } finally {
            if (clearOnExit) a.clear() else a.stop()
        }
    }
    return null
}

fun Terminal.interactiveSelectList(
    entries: List<String>,
    title: String = "",
    cursorMarker: String? = null,
    // TODO add other style options
    startingCursorIndex: Int = 0,
    includeInstructions: Boolean = true,
    onlyShowActiveDescription: Boolean = false, // TODO
    clearOnExit: Boolean = true,
): String? {
    return animateSelectList(
        singleSelect = true,
        limit = 1,
        entries = entries.map { SelectList.Entry(it) },
        title = Text(theme.style("select.title")(title)),
        startingCursorIndex = startingCursorIndex,
        cursorMarker = cursorMarker,
        selectedMarker = "",
        unselectedMarker = "",
        captionBottom = if (includeInstructions) {
            Text(
                theme.style("select.instructions")(
                    " ${brightWhite("↑")} up • ${brightWhite("↓")} down • ${brightWhite("enter")} select"
                )
            )
        } else null,

        selectedStyle = null,//TODO
        unselectedTitleStyle = null,//TODO
        unselectedMarkerStyle = null,//TODO
        clearOnExit = clearOnExit,
        onlyShowActiveDescription = onlyShowActiveDescription,
    )?.first()?.title
}

fun Terminal.interactiveMultiSelectList(
    entries: List<SelectList.Entry>,
    title: String = "",
    limit: Int = Int.MAX_VALUE,
    startingCursorIndex: Int = 0,
    // TODO add other style options
    includeInstructions: Boolean = true,
    onlyShowActiveDescription: Boolean = false, // TODO
    clearOnExit: Boolean = true,
): List<String>? {
    return animateSelectList(
        singleSelect = false,
        limit = limit,
        entries = entries,
        title = Text(theme.style("select.title")(title)),
        startingCursorIndex = startingCursorIndex,
        cursorMarker = null,
        selectedMarker = null,
        unselectedMarker = null,
        captionBottom = if (includeInstructions) {
            // TODO: theme
            Text(
                dim(
                    " ${brightWhite("x")} toggle • ${brightWhite("↑")} up • ${brightWhite("↓")} down • ${
                        brightWhite(
                            "enter"
                        )
                    } confirm"
                )
            )
        } else null,
        selectedStyle = null,//TODO
        unselectedTitleStyle = null,//TODO
        unselectedMarkerStyle = null,//TODO
        clearOnExit = clearOnExit,
        onlyShowActiveDescription = onlyShowActiveDescription,
    )?.mapNotNull { if (it.selected) it.title else null }
}

