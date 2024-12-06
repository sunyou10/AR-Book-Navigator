package com.example.librarytest

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.librarytest.data.TreeNode
import com.example.librarytest.utils.FileUtils.loadJsonFromAssets
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.ar.core.Config.PlaneFindingMode
import com.google.ar.core.Pose
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment

class NavigationActivity : AppCompatActivity() {

    private lateinit var bookNumber: String
    private lateinit var navigationButton: ExtendedFloatingActionButton
    private var arrowSource: Uri = Uri.parse("direction_arrow.sfb")
    private var pointSource: Uri = Uri.parse("map_point.sfb")
    private var nodes: List<TreeNode> = emptyList()
    private var currentNodeId: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 버튼 설정
        navigationButton = findViewById(R.id.startButton)
        navigationButton.text = "Start"

        // 책 정보와 관련 노드들 가져오기
        bookNumber = intent.getStringExtra("bookNumber") ?: ""
        nodes = loadJsonFromAssets(this, "nodes.json", bookNumber)

        // 버튼 클릭 이벤트
        navigationButton.setOnClickListener {
            Toast.makeText(this, "Current: $currentNodeId", Toast.LENGTH_SHORT).show()
            handleButtonClick()
        }
    }

    private fun handleButtonClick() {
        val currentNode = findNodeById(currentNodeId)
        if (currentNode == null) {
            Toast.makeText(this, "현재 노드를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        // 현재 노드의 neighbors 확인
        val nextNodeId = if (currentNode is TreeNode.Entry) {
            currentNode.neighbors[0]
        } else if (currentNode is TreeNode.Dest) {
            null
        } else {
            currentNode.neighbors[1]
        }

        if (nextNodeId == null) {
            drawArrowForNode(currentNode)
            // 도착
            navigationButton.text = "도착!"
            Toast.makeText(this, "도착했습니다!", Toast.LENGTH_SHORT).show()
//            finish()
        } else {
            // 화살표 표시
            drawArrowForNode(currentNode)

            // 다음 노드로 이동
            currentNodeId = nextNodeId

            navigationButton.text = "Next"
        }
    }

    private fun drawArrowForNode(node: TreeNode) {

        // ARFragment 가져오기
        val arFragment = supportFragmentManager.findFragmentById(R.id.ar_fragment) as ArFragment

        val frame = arFragment.arSceneView.arFrame
        val session = arFragment.arSceneView.session

        val config = arFragment.arSceneView.session?.config
        config?.planeFindingMode = PlaneFindingMode.HORIZONTAL_AND_VERTICAL
        arFragment.arSceneView.session?.configure(config)

        // ARCore 세션 및 Plane 상태 확인
        if (session == null || frame == null || frame.camera.trackingState != TrackingState.TRACKING) {
            Toast.makeText(this, "ARCore가 초기화되지 않았거나 표면을 감지하지 못했습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        // 위치 정보 추출
        val position = node.position

        // 회전 정보 추출
        val forwardVector = node.forwardVector

        // Anchor 생성
        val anchor = arFragment.arSceneView.session?.createAnchor(
            Pose(floatArrayOf(position.x, position.y, position.z),
                floatArrayOf(forwardVector.x, forwardVector.y, forwardVector.z, forwardVector.w))
        )
        val anchorNode = AnchorNode(anchor).apply { setParent(arFragment.arSceneView.scene) }

        ModelRenderable.builder()
            .setSource(this, selectSource(node))
            .build()
            .thenAccept { renderable ->
                renderable.isShadowCaster = false
                renderable.isShadowReceiver = false
                anchorNode.localScale = Vector3(0.1f, 0.1f, 0.1f)

                anchorNode.renderable = renderable
                arFragment.arSceneView.scene.addChild(anchorNode)
            }
            .exceptionally { throwable ->
                Toast.makeText(this, "Unable to load arrow model: ${throwable.message}", Toast.LENGTH_LONG).show()
                null
            }
    }

    private fun selectSource(node: TreeNode): Uri {
        if (node is TreeNode.Dest)
            return pointSource
        else
            return arrowSource
    }

    private fun findNodeById(nodeId: Int): TreeNode? {
        return nodes.firstOrNull() { it.id == nodeId }
    }
}