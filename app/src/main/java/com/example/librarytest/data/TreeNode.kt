package com.example.librarytest.data

import dev.romainguy.kotlin.math.Float3
import dev.romainguy.kotlin.math.Quaternion

sealed class TreeNode(
    val id: Int,
    val type: String,
    val position: Float3,
    val neighbors: MutableList<Int> = mutableListOf(),
    val forwardVector: Quaternion
){
    class Entry(
        id: Int,
        position: Float3,
        neighbors: MutableList<Int> = mutableListOf(),
        forwardVector: Quaternion
    ): TreeNode(id, "Entry", position, neighbors, forwardVector)

    class Path(
        id: Int,
        position: Float3,
        neighbors: MutableList<Int> = mutableListOf(),
        forwardVector: Quaternion
    ): TreeNode(id, "Path", position, neighbors, forwardVector)

    class Dest(
        id: Int,
        position: Float3,
        neighbors: MutableList<Int> = mutableListOf(),
        forwardVector: Quaternion
    ): TreeNode(id, "Dest", position, neighbors, forwardVector)
}