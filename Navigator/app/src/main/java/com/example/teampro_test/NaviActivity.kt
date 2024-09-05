package com.example.teampro_test

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.dewakoding.ar_locationbased.MainActivity
import com.example.teampro_test.databinding.ActivityNaviBinding

private const val TAG_CALENDER = "calender_fragment"
private const val TAG_HOME = "home_fragment"
private const val TAG_MY_PAGE = "my_page_fragment"
private const val TAG_OUTSIDE = "outside_fragment"
private const val TAG_INSIDE = ""
private const val TAG_NAVI_HOME = ""

class NaviActivity : AppCompatActivity() {

    private lateinit var binding : ActivityNaviBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNaviBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setFragment(TAG_HOME, HomeFragment())

        binding.navigation.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.calenderFragment -> setFragment(TAG_CALENDER, CalenderFragment())
                R.id.homeFragment -> setFragment(TAG_HOME, HomeFragment())
                R.id.outsideFragment-> {
                    setFragment(TAG_OUTSIDE, OutsideFragment())
                }
                R.id.navihomeFragment -> setFragment(TAG_NAVI_HOME, NaviHomeFragment())
            }
            true
        }
    }

    private fun setFragment(tag: String, fragment: Fragment) {
        val manager: FragmentManager = supportFragmentManager
        val fragTransaction = manager.beginTransaction()

        if (manager.findFragmentByTag(tag) == null) {
            fragTransaction.add(R.id.mainFrameLayout, fragment, tag)
        }

        val calender = manager.findFragmentByTag(TAG_CALENDER)
        val home = manager.findFragmentByTag(TAG_HOME)
        val outside = manager.findFragmentByTag(TAG_OUTSIDE)
        val navihome = manager.findFragmentByTag(TAG_NAVI_HOME)

        if (calender != null) {
            fragTransaction.hide(calender)
        }

        if (home != null) {
            fragTransaction.hide(home)
        }

        if (outside != null) {
            fragTransaction.hide(outside)
        }

        if (navihome != null) {
            fragTransaction.hide(navihome)
        }

        if (tag == TAG_CALENDER) {
            if (calender != null) {
                fragTransaction.show(calender)
            }
            stopMainActivity()
        } else if (tag == TAG_HOME) {
            if (home != null) {
                fragTransaction.show(home)
            }
            stopMainActivity()
        } else if (tag == TAG_OUTSIDE) {
            if (outside != null) {
                fragTransaction.show(outside)
            }
            startMainActivity()
        } else if (tag == TAG_NAVI_HOME) {
            if (navihome != null) {
                fragTransaction.show(navihome)
            }
            stopMainActivity()
        }

        fragTransaction.commitAllowingStateLoss()
    }

    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun stopMainActivity() {
        val mainActivity = MainActivity::class.java
        val intent = Intent(this, mainActivity)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        sendBroadcast(intent)
    }
}
