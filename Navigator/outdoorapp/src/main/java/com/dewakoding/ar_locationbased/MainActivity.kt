package com.dewakoding.ar_locationbased

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.dewakoding.arlocationbased.model.Place
import com.dewakoding.arlocationbased.ui.ARActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.util.Locale



class MainActivity : ARActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // EXIT 인텐트를 처리
        if (intent.getBooleanExtra("EXIT", false)) {
            finish()
            return
        }
        initBottomSheetDialog()

        val list = ArrayList<Place>()
        list.add(
            Place("0",
                "서일대학교",
                37.5861321,127.0974750,
                description = "서일대학교",
                photoUrl = "https://images.unsplash.com/photo-1606787366850-de6330128bfc?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=2940&q=80")
        )
        list.add(
            Place("1",
                "서일대 정문",
                37.587400492780354,127.09764806004256,
                description = "서일대 정문",
                photoUrl = "https://images.unsplash.com/photo-1606787366850-de6330128bfc?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=2940&q=80")
        )
        list.add(
            Place("2",
                "서일대 후문",
                37.58596619582185,127.09700083523845,
                description = "서일대 후문",
                photoUrl = "https://images.unsplash.com/photo-1498837167922-ddd27525d352?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=2940&q=80")
        )
        list.add(
            Place("3",
                "누리관",
                37.58626585226993,127.09690781761574,
                description = "누리관",
                photoUrl = "https://images.unsplash.com/photo-1606787366850-de6330128bfc?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=2940&q=80")
        )
        list.add(
            Place("4",
                "도서관",
                37.58579447999466,127.09764030115177,
                description = "도서관",
                photoUrl = "https://images.unsplash.com/photo-1606787366850-de6330128bfc?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=2940&q=80")
        )
        list.add(
            Place("5",
                "서일관",
                37.586229110072246,127.0977597497562,
                description = "서일관",
                photoUrl = "https://images.unsplash.com/photo-1606787366850-de6330128bfc?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=2940&q=80")
        )
        list.add(
            Place("6",
                "학생식당",
                37.586727163041616,127.09745187481984,
                description = "학생식당",
                photoUrl = "https://images.unsplash.com/photo-1606787366850-de6330128bfc?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=2940&q=80")
        )
        list.add(
            Place("7",
                "세종관",
                37.58696516980662,127.09836361430176,
                description = "세종관",
                photoUrl = "https://images.unsplash.com/photo-1606787366850-de6330128bfc?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=2940&q=80")
        )
        list.add(
            Place("8",
                "홍학관",
                37.58724040037369,127.0978488189536,
                description = "홍학관",
                photoUrl = "https://images.unsplash.com/photo-1606787366850-de6330128bfc?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=2940&q=80")
        )
        list.add(
            Place("9",
                "배양관",
                37.58563275209649,127.09709380823332,
                description = "배양관",
                photoUrl = "https://images.unsplash.com/photo-1606787366850-de6330128bfc?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=2940&q=80")
        )
        list.add(
            Place("10",
                "호천관",
                37.58768617261914,127.0981126438929,
                description = "호천관",
                photoUrl = "https://images.unsplash.com/photo-1606787366850-de6330128bfc?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=2940&q=80")
        )
        // You want to display places within a radius of 10.000 meters / 100 km.
        ARInitData(list, 100000.00)

    }
    override fun onARPointSelected(place: Place) {
        //Toast.makeText(applicationContext, place.name, Toast.LENGTH_SHORT).show()
        showBottomSheetDialog(place)
    }

    lateinit var dialog:BottomSheetDialog
    lateinit var bottomSheetDialogView:View

    private fun initBottomSheetDialog() {
        dialog = BottomSheetDialog(this)
        bottomSheetDialogView = layoutInflater.inflate(R.layout.bottom_sheet_dialog, null)
        val btnClose = bottomSheetDialogView.findViewById<Button>(R.id.idBtnDismiss)
        btnClose.setOnClickListener {
            // on below line we are calling a dismiss
            // method to close our dialog.
            arOverlayView?.showArrow(false)
            dialog.dismiss()
        }
        dialog.setCancelable(true)
        dialog.setContentView(bottomSheetDialogView)
    }
    private fun showBottomSheetDialog(place: Place) {
        val textViewTitle = bottomSheetDialogView.findViewById<TextView>(R.id.textViewTitle)
        val textViewDescription = bottomSheetDialogView.findViewById<TextView>(R.id.textViewDescription)
        val textViewDistance = bottomSheetDialogView.findViewById<TextView>(R.id.textViewDistance)
        val imageViewLocationType = bottomSheetDialogView.findViewById<ImageView>(R.id.imageViewLocationType)
        textViewTitle.text = place.name
        textViewDescription.text = place.description
        textViewDistance.text = distanceStr(place.distance!!.toDouble())
        if(place.name == ATM){
            imageViewLocationType.setImageDrawable(resources.getDrawable(R.drawable.atm_locations_50))
        } else {
            imageViewLocationType.setImageDrawable(resources.getDrawable(R.drawable.bank_building_64))
        }

        val buttonDirection = bottomSheetDialogView.findViewById<Button>(R.id.buttonDirection)
        buttonDirection.setOnClickListener {
            // on below line we are calling a direction of the locator
            if (place.distance!!.toDouble()> 500) {
                openGoogleMap(place)
            }
            // method to close our dialog.
            dialog.dismiss()
        }

        dialog.setContentView(bottomSheetDialogView)
        dialog.show()
    }
    fun distanceStr(distance: Double): String {
        if (distance < 1000) {
            return (("%.2f".format(distance)) + " m")
        } else {
            return (("%.2f".format(distance / 1000)) + " km")
        }
    }
    private fun openGoogleMap(place: Place) {
        val myData = java.lang.String.format(
            Locale.KOREAN,
            "http://maps.google.com/maps?daddr=${place.location.latitude},${place.location.longitude}"
        )

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(myData))
        intent.setPackage("com.google.android.apps.maps")
        startActivity(intent)
    }

    companion object {
        const val ATM = "ATM"
        const val BRANCH = "Branch"
        const val BLUE = "BLUE"
    }
}