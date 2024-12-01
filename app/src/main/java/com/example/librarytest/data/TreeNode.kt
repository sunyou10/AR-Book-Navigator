package com.example.librarytest.data

import dev.romainguy.kotlin.math.Float3
import dev.romainguy.kotlin.math.Quaternion

sealed class TreeNode(
    val id: Int,
    val type: String,
    val position: Float3,
    val neighbors: MutableList<Int> = mutableListOf()
){
    class Entry(
        var forwardVector: Quaternion,
        id: Int,
        position: Float3,
        neighbors: MutableList<Int> = mutableListOf(),
    ): TreeNode(id, "Entry", position, neighbors)

    class Path(
        var forwardVector: Quaternion,
        id: Int,
        position: Float3,
        neighbors: MutableList<Int> = mutableListOf()
    ): TreeNode(id, "Path", position, neighbors)

    class Dest(
        var forwardVector: Quaternion?,
        id: Int,
        position: Float3,
        neighbors: MutableList<Int> = mutableListOf()
    ): TreeNode(id, "Dest", position, neighbors)
}