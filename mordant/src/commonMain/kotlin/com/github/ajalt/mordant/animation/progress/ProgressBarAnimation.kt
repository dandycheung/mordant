package com.github.ajalt.mordant.animation.progress

import com.github.ajalt.mordant.widgets.progress.ProgressBarCell
import com.github.ajalt.mordant.widgets.progress.ProgressBarDefinition
import com.github.ajalt.mordant.widgets.progress.ProgressState
import com.github.ajalt.mordant.widgets.progress.TaskId

// TODO docs
interface ProgressTaskUpdateScope<T> {
    var context: T
    var completed: Long
    var total: Long?
    var visible: Boolean
    var started: Boolean
    var paused: Boolean
}

interface ProgressTask<T> {
    /**
     * Update the task's state.
     *
     * If the completed count is equal to the total, the task will be marked as [finished].
     *
     * If the task is already finished, this method will still update the task's state, but it will
     * remain marked as finished. Use [reset] if you want to start the task again.
     */
    fun update(block: ProgressTaskUpdateScope<T>.() -> Unit)

    /**
     * Reset the task so its completed count is 0 and its clock is reset.
     *
     * @param start If true, start the task after resetting it and running [block].
     * @param block A block to [update] the task's state after resetting it.
     */
    fun reset(
        start: Boolean = true,
        block: ProgressTaskUpdateScope<T>.() -> Unit = {},
    )

    fun makeState(): ProgressState<T>

    val finished: Boolean
    val id: TaskId

    val context: T
    val completed: Long
    val total: Long?
    val visible: Boolean
    val started: Boolean
    val paused: Boolean
}

/**
 * Advance the completed progress of this task by [amount].
 *
 * This is a shortcut for `update { completed += amount }`.
 */
fun ProgressTask<*>.advance(amount: Long = 1) = update { completed += amount }

/**
 * Advance the completed progress of this task by [amount].
 *
 * This is a shortcut for `update { completed += amount }`.
 */
fun ProgressTask<*>.advance(amount: Number) = advance(amount.toLong())

/**
 * Set the completed progress of this task to [completed].
 *
 * This is a shortcut for `update { this.completed += completed }`.
 */
fun ProgressTask<*>.update(completed: Long) = update { this.completed = completed }

/**
 * Set the completed progress of this task to [completed].
 *
 * This is a shortcut for `update { this.completed += completed }`.
 */
fun ProgressTask<*>.update(completed: Number) = update(completed.toLong())

// This isn't a RefreshableAnimation because the coroutine animator needs its methods to be
// suspending
/**
 * An animation that can draw one or more progress [tasks][addTask] to the screen.
 */
interface ProgressBarAnimation<T> {
    // TODO docs
    fun addTask(
        definition: ProgressBarDefinition<T>,
        context: T,
        total: Long? = null,
        completed: Long = 0,
        start: Boolean = true,
        visible: Boolean = true,
    ): ProgressTask<T>

    /**
     * Remove a task from the progress bar.
     *
     * @return `true` if the task was removed, `false` if it was not found.
     */
    fun removeTask(task: ProgressTask<T>): Boolean

    /**
     * Draw the progress bar to the screen.
     *
     * If [visible] is `false`, this call has no effect.
     *
     * This is called automatically when the animation is running, so you don't usually need to call
     * it yourself.
     *
     * @param refreshAll If `true`, refresh all cells, ignoring their [fps][ProgressBarCell.fps].
     */
    fun refresh(refreshAll: Boolean = false)

    /**
     * Return `true` if all tasks are [finished][ProgressTask.finished].
     */
    val finished: Boolean
}

fun ProgressBarAnimation<Unit>.addTask(
    definition: ProgressBarDefinition<Unit>,
    total: Long? = null,
    completed: Long = 0,
    start: Boolean = true,
    visible: Boolean = true,
): ProgressTask<Unit> {
    return addTask(definition, Unit, total, completed, start, visible)
}

