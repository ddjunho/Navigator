## App video
![video](https://github.com/RAAMKHOT/AR-Location-Android/blob/main/AR_LocationBased_9mb.gif)

## Usage

To use this library, just extend the ARActivity class in your activity class. To add points, just make ArrayList of Place, and call ARInitData with ArrayList of Place as the parameter. You must add radius (in meter) as parameter too. You can access the point that user clicked inside onARPointSelected's method.

```bash
class MainActivity : ARActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       
        val list = ArrayList<Place>()
        list.add(
            Place("1", "Coffee Shop", -6.174870735058176, 106.82620041234728, description = "Promotion available here")
        )
        list.add(
            Place("2", "ATM", -6.122310891453182, 106.83357892611079, description = "Good Resto")
        )
        // You want to display places within a radius of 50 meters.
        ARInitData(list, 50.00)
    }

    override fun onARPointSelected(place: Place) {
        Toast.makeText(applicationContext, place.name, Toast.LENGTH_SHORT).show()
    }
}
```
## ToDo
- [x] Filter Place with radius of distance
- [ ] Load url image on cardview
- [ ] Customize the cardview

## LINK This app to Other App
1.       
    button.setOnClickListener(DoubleTapSafeClickListener {
        openArBaseLocationApp("com.dewakoding.ar_locationbased")
    })

2. 
    private fun openArBaseLocationApp(packageName: String) {
        //com.dewakoding.ar_locationbased
        val intent = Intent(Intent.ACTION_MAIN)
        intent.component = ComponentName(packageName, "${packageName}.MainActivity")
        startActivity(intent)
    }



