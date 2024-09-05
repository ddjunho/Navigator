package com.dewakoding.arlocationbased.ui

import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.opengl.Matrix
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.dewakoding.arlocationbased.R
import com.dewakoding.arlocationbased.helper.LocationHelper.ECEFtoENU
import com.dewakoding.arlocationbased.helper.LocationHelper.WSG84toECEF
import com.dewakoding.arlocationbased.listener.PointClickListener
import com.dewakoding.arlocationbased.model.Place

open class AROverlayView constructor(activity: Activity, val places: MutableList<Place>?, val radiusInMeter: Double, val pointClickListener: PointClickListener) :
    View(activity), SensorEventListener {

    private var projectionMatrix = FloatArray(16)
    private var currentLocation: Location? = null
    private val arFrameLayout: FrameLayout = activity.findViewById(R.id.ar_frame_layout)
    private val arPointLayouts: MutableList<View> = mutableListOf()

    //화살표
    private val arrowView: ImageView
    private val handler = Handler(Looper.getMainLooper())
    private var arrowUpdateRunnable: Runnable? = null
    private var arrowVisible = false

    // Sensor 관련 변수
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var magnetometer: Sensor? = null
    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)
    init {
        // 화살표 초기화
        val arrowLayout = LayoutInflater.from(context).inflate(R.layout.arrow, null) as FrameLayout
        arrowView = arrowLayout.findViewById(R.id.arrow)
        arrowView.visibility = View.INVISIBLE
        arFrameLayout.addView(arrowLayout)
        // 화살표 위치 설정
        setArrowPosition()

        // SensorManager 초기화
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

        // 가속도계와 자기장 센서 등록
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        // Sensor 등록
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL)
    }

    fun start() {
        if (parent != null) {
            val viewGroup = parent as ViewGroup
            viewGroup.removeView(this)
        }
        arFrameLayout.addView(this)
    }

    fun updateProjectionMatrix(matrix: FloatArray) {
        val rotationMatrix = FloatArray(16)
        SensorManager.getRotationMatrixFromVector(rotationMatrix, matrix)

        val ratio: Float = when {
            width < height -> {
                width.toFloat() / height.toFloat()
            }
            else -> {
                height.toFloat() / width.toFloat()
            }
        }

        val viewMatrix = FloatArray(16)
        Matrix.frustumM(
            viewMatrix, 0, -ratio, ratio,
            -1f, 1f, 0.5f, 10000f
        )

        val projectionMatrix = FloatArray(16)
        Matrix.multiplyMM(
            projectionMatrix, 0, viewMatrix, 0,
            rotationMatrix, 0
        )
        this.projectionMatrix = projectionMatrix
        invalidate()
    }

    fun updateCurrentLocation(currentLocation: Location?) {
        this.currentLocation = currentLocation
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (currentLocation == null) {
            return
        }
        places?.let {
            for (i in this.places.indices) {
                val currentLocationInECEF = WSG84toECEF(
                    currentLocation!!
                )
                val pointInECEF = WSG84toECEF(places?.get(i)!!.getCoordinate())
                val pointInENU = ECEFtoENU(currentLocation!!, currentLocationInECEF, pointInECEF)
                val cameraCoordinateVector = FloatArray(4)
                Matrix.multiplyMV(
                    cameraCoordinateVector,
                    0,
                    projectionMatrix,
                    0,
                    pointInENU,
                    0
                )
                if (cameraCoordinateVector[2] < 0) {
                    val x =
                        (0.5f + cameraCoordinateVector[0] / cameraCoordinateVector[3]) * canvas!!.width
                    val y =
                        (0.5f - cameraCoordinateVector[1] / cameraCoordinateVector[3]) * canvas.height

                    places[i].x = x
                    places[i].y = y
                    places[i].distance = distance(currentLocation!!, places!!.get(i).location)

                    val isVisible = isVisible(places[i])

                    if (isVisible) {
                        if (arPointLayouts.size <= i) {
                            val arPointCardView = LayoutInflater.from(context).inflate(R.layout.cardview_point_with_image, null)
                            val arPointIcon = arPointCardView.findViewById<ImageView>(R.id.img_status)
                            val arPointName = arPointCardView.findViewById<TextView>(R.id.tv_title)
                            val arPointDescription = arPointCardView.findViewById<TextView>(R.id.tv_description)
                            val arPointDistance = arPointCardView.findViewById<TextView>(R.id.tv_distance)

                            if (places[i].name != null) {
                                arPointIcon.visibility = View.VISIBLE
                                if (places[i].name.equals("서일대학교")) {
                                    Glide.with(this)
                                        .load(this.resources.getDrawable(R.drawable.location_school))
                                        .into(arPointIcon)
                                } else {
                                    Glide.with(this)
                                        .load(this.resources.getDrawable(R.drawable.location_green))
                                        .into(arPointIcon)
                                }
                            }

                            arPointName.text = places[i].name
                            arPointDescription.visibility = View.GONE
                            arPointDescription.text = places[i].description
                            arPointDistance.text = distanceStr(places[i].distance!!.toDouble())

                            arPointCardView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)

                            arPointCardView.setOnClickListener {
                                pointClickListener.onClick(places[i])
                                showArrow(true, places[i].location) // 장소 클릭 시 화살표 회전
                            }

                            val parentView = parent as ViewGroup
                            if (parentView != null) {
                                parentView.addView(arPointCardView)
                            }

                            arPointLayouts.add(arPointCardView)


                            // Update the AR point layout position
                            val arPointCardViewLayoutParams = FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                            )
                            arPointCardViewLayoutParams.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
                            arPointCardView.layoutParams = arPointCardViewLayoutParams

                            val marginInPixels = resources.getDimensionPixelSize(R.dimen.margin)
                            arPointCardViewLayoutParams.setMargins(0, marginInPixels, 0, 0)

                            arPointCardView.x = x
                            arPointCardView.y = y
                        }
                        if (arPointLayouts.size > 0) {
                            if (i < arPointLayouts.size) {
                                // Update the AR point layout position
                                val arPointLayout = arPointLayouts[i]
                                arPointLayout.x = x
                                arPointLayout.y = y
                            }

                        }
                    }
                }
            }
        }
    }

    private fun isVisible(place: Place): Boolean {
        return if (currentLocation != null && radiusInMeter != null) {
            val distance = place.distance ?: return false
            distance <= radiusInMeter &&
                    ((distance <= 500 && place.name != "서일대학교") ||
                            (distance > 500 && place.name == "서일대학교"))
        } else {
            false
        }
    }
    private fun setArrowPosition() {
        val layoutParams = arrowView.layoutParams as FrameLayout.LayoutParams
        layoutParams.gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        val bottomMargin = resources.getDimensionPixelSize(R.dimen.bottom_margin)
        layoutParams.setMargins(0, 0, 0, bottomMargin)
        arrowView.layoutParams = layoutParams
    }

    // 화살표 회전 메서드
    private fun rotateArrow(destination: Location) {
        currentLocation?.let {
            val bearingToDestination = it.bearingTo(destination)
            val deviceBearing = getDeviceBearing()
            val rotation = bearingToDestination - deviceBearing
            val correctedRotation = 180 - rotation
            // 화살표 회전 애니메이션
            val rotateAnimation = RotateAnimation(
                0f, correctedRotation,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
            )
            rotateAnimation.duration = 500
            rotateAnimation.fillAfter = true

            arrowView.startAnimation(rotateAnimation)
        }
    }

    // 디바이스 방향 계산
    private fun getDeviceBearing(): Float {
        val rotationMatrix = FloatArray(9)
        val orientationValues = FloatArray(3)
        SensorManager.getRotationMatrixFromVector(rotationMatrix, projectionMatrix)
        SensorManager.getOrientation(rotationMatrix, orientationValues)
        return Math.toDegrees(orientationValues[0].toDouble()).toFloat()
    }

    // onSensorChanged 메서드 수정
    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return

        // 센서 종류에 따라 데이터 처리
        when (event.sensor?.type) {
            Sensor.TYPE_ACCELEROMETER -> System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.size)
            Sensor.TYPE_MAGNETIC_FIELD -> System.arraycopy(event.values, 0, magnetometerReading, 0, magnetometerReading.size)
        }

        // 회전 매트릭스 계산
        SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerReading, magnetometerReading)

        // 방향 계산
        SensorManager.getOrientation(rotationMatrix, orientationAngles)

        // 화살표 회전
        rotateArrowBasedOnDeviceOrientation()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    private fun rotateArrowBasedOnDeviceOrientation() {
        // orientationAngles[0]은 디바이스의 방위각입니다. 필요에 따라 조정하여 화살표 회전 가능
        arrowView.rotation = ((-Math.toDegrees(orientationAngles[0].toDouble()).toFloat()+47f)* 1.2).toFloat()
    }


    // 화살표 업데이트 작업 시작
    private fun startArrowUpdate(destination: Location) {
        arrowUpdateRunnable = object : Runnable {
            override fun run() {
                currentLocation?.let { currentLoc ->
                    if (arrowVisible) {
                        rotateArrow(destination)
                    }
                }

                // 0.5초마다 화살표 업데이트 작업 재실행
                handler.postDelayed(this, 500)
            }
        }

        // 처음에는 바로 실행하지 않음
    }


    // 화살표 업데이트 작업 정지
    private fun stopArrowUpdate() {
        handler.removeCallbacks(arrowUpdateRunnable!!)
    }

    // 화살표 표시/숨기기
    fun showArrow(show: Boolean, destination: Location?=null) {
        arrowVisible = show
        arrowView.visibility = if (show) View.VISIBLE else View.INVISIBLE
        if (show && destination != null) {
            startArrowUpdate(destination) // 화살표가 표시되면 업데이트 작업 시작
        } else {
            stopArrowUpdate() // 화살표가 숨겨지면 업데이트 작업 정지
        }
    }

    private fun distance(currentLoc: Location, pointLocation: Location): Float {
        return currentLoc.distanceTo(pointLocation)
    }

    private fun distanceStr(distance: Double): String {
        return if (distance < 1000) {
            "%.2f".format(distance) + " m"
        } else {
            "%.2f".format(distance / 1000) + " km"
        }
    }
}
