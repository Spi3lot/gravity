import data.Body
import data.Node
import processing.core.PApplet
import processing.core.PVector
import kotlin.random.Random

fun main() {
    PApplet.main(Gravity::class.java)
}

class Gravity : PApplet() {

    companion object {

        const val WIDTH = 800

        const val HEIGHT = 600

        const val NUM_BODIES = 50000

        const val THETA = 0.5f  // Barnes-Hut approximation criteria

        const val G = 6.67430e-2f  // Gravitational constant (scaled for visualization)

        const val SOFTENING = 5f  // Prevents division by zero

        private lateinit var bodies: Array<Body>

        private var paused = false

    }

    override fun settings() {
        size(WIDTH, HEIGHT)
    }

    override fun setup() {
        frameRate(1000f)

        bodies = Array(NUM_BODIES) {
            val center = PVector(width / 4f, height / 2f)
            val offset = PVector.random2D().mult(random(25f, 100f))
            val vel = PVector(offset.y, -offset.x).setMag(10f)

            if (Random.nextBoolean()) {
                center.x *= 3
                vel.mult(-1f)
            }

            Body(PVector.add(center, offset), vel, Random.nextFloat() * 10f + 1f)
        }
    }

    override fun draw() {
        background(0)

        if (!paused) {
            val root = buildTree()
            updateBodies(root)
        }

        stroke(255)

        bodies.forEach {
            strokeWeight(sqrt(it.mass))
            point(it.pos.x, it.pos.y)
        }

        fill(255)
        textSize(16f)
        text("FPS: ${frameRate.toInt()}", 10f, 20f)
        text("Bodies: ${bodies.size}", 10f, 40f)
        text("Press SPACE to pause/resume", 10f, 60f)
    }

    override fun keyPressed() {
        if (key == ' ') {
            paused = !paused
        }
    }

    private fun buildTree(): Node {
        val root = Node(PVector(width / 2f, height / 2f), max(width.toFloat(), height.toFloat()))
        bodies.forEach { root.insert(it) }
        return root
    }

    private fun updateBodies(root: Node) {
        bodies.forEach { body ->
            val force = calculateForce(body, root)
            body.vel.add(PVector.mult(force, 1f / body.mass))
            body.pos.add(body.vel)
        }
    }

    private fun calculateForce(body: Body, node: Node): PVector {
        if (node.isLeaf() && (node.body == null || node.body == body)) {
            return PVector()
        }

        val distance = PVector.dist(body.pos, node.centerOfMass)

        if (node.isLeaf() || node.size < THETA * distance) {
            if (distance < SOFTENING) return PVector()
            val force = PVector.sub(node.centerOfMass, body.pos)
            val strength = G * body.mass * node.mass / (distance * distance + SOFTENING)
            return force.normalize().mult(strength)
        }

        val totalForce = PVector()

        node.children.filterNotNull().forEach { child ->
            totalForce.add(calculateForce(body, child))
        }

        return totalForce
    }

}