package data

import processing.core.PVector

class Node(private val center: PVector, val size: Float) {

    val children = arrayOfNulls<Node>(4)

    val centerOfMass = PVector()

    var mass = 0f

    var body: Body? = null

    fun isLeaf(): Boolean {
        return children[0] == null
    }

    fun insert(newBody: Body): Boolean {
        if (!contains(newBody.pos)) return false

        if (body == null && isLeaf()) {
            body = newBody
            updateMass(newBody)
            return true
        }

        if (isLeaf() && body != null) {
            subdivide()
            insertExistingBody()
        }

        updateMass(newBody)

        for (child in children) {
            if (child?.insert(newBody) == true) return true
        }

        return false
    }

    private fun insertExistingBody() {
        val existingBody = body
        body = null

        for (child in children) {
            if (existingBody?.let { child?.insert(it) } == true) break
        }
    }

    private fun subdivide() {
        val halfSize = size / 2
        val quarterSize = halfSize / 2
        children[0] = Node(PVector(center.x - quarterSize, center.y - quarterSize), halfSize)  // NW
        children[1] = Node(PVector(center.x + quarterSize, center.y - quarterSize), halfSize)  // NE
        children[2] = Node(PVector(center.x - quarterSize, center.y + quarterSize), halfSize)  // SW
        children[3] = Node(PVector(center.x + quarterSize, center.y + quarterSize), halfSize)  // SE
    }

    private fun updateMass(newBody: Body) {
        val totalMass = mass + newBody.mass
        centerOfMass.mult(mass)
        centerOfMass.add(PVector.mult(newBody.pos, newBody.mass))
        centerOfMass.div(totalMass)
        mass = totalMass
    }

    private fun contains(pos: PVector): Boolean {
        return pos.x >= center.x - size / 2 && pos.x < center.x + size / 2 &&
                pos.y >= center.y - size / 2 && pos.y < center.y + size / 2
    }

}