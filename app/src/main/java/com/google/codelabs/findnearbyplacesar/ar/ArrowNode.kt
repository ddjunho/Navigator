// ArrowNode.kt
package com.google.codelabs.findnearbyplacesar.ar

import android.net.Uri
import com.google.android.gms.maps.model.LatLng
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable

class ArrowNode(fragment: PlacesArFragment, private val origin: LatLng, private val destination: LatLng) : Node() {

    init {
        fragment.arSceneView.scene.addOnUpdateListener { frameTime ->
            val bearing = getBearing(origin, destination).toFloat()
            localRotation = Quaternion.axisAngle(Vector3.up(), bearing)

            // 화살표 모델의 위치와 크기를 설정합니다.
            worldPosition = Vector3(0f, 0f, -2f) // 화살표를 카메라에서 약간 떨어진 거리에 배치합니다.
            localScale = Vector3(0.1f, 0.1f, 0.1f) // 화살표의 크기를 조절합니다.
        }

        // 화살표 모델을 로드하고 추가합니다.
        ModelRenderable.builder()
            .setSource(fragment.context, Uri.parse("raw://arrow.gltf")) // 화살표 모델 파일의 경로 지정
            .build()
            .thenAccept { renderable ->
                // 모델 렌더링 설정
                renderable.isShadowCaster = false
                renderable.isShadowReceiver = false

                // 화살표 모델을 노드에 적용하여 AR 화면에 추가합니다.
                val arrowNode = Node().apply {
                    this.renderable = renderable
                }
                this.addChild(arrowNode) // 화살표 노드를 AR 루트 노드에 추가합니다.
            }
            .exceptionally {
                throw AssertionError("Could not load model.", it)
            }
    }

    private fun getBearing(origin: LatLng, destination: LatLng): Double {
        val startLat = Math.toRadians(origin.latitude)
        val startLong = Math.toRadians(origin.longitude)
        val endLat = Math.toRadians(destination.latitude)
        val endLong = Math.toRadians(destination.longitude)

        val dLong = endLong - startLong

        val dPhi = Math.log(
            Math.tan(endLat / 2.0 + Math.PI / 4) / Math.tan(startLat / 2.0 + Math.PI / 4)
        )

        val bearing = Math.toDegrees(Math.atan2(dLong, dPhi))

        return (bearing + 360) % 360
    }
}
