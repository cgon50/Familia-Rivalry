package com.example.familiarivalry

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.util.*
import kotlin.concurrent.schedule
import kotlin.math.min
import kotlin.random.Random
import androidx.core.app.ComponentActivity
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.net.wifi.p2p.WifiP2pDevice
import party.liyin.easywifip2p.WifiP2PHelper


class MainActivity : AppCompatActivity() {

    private lateinit var gameFragment: GameFragment
    private lateinit var peerFragment:P2pFragment
    private lateinit var homeFragment:HomeFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        gameFragment = GameFragment.newInstance()
//        peerFragment = P2pFragment.newInstance()
        homeFragment = HomeFragment.newInstance()
        WifiP2PHelper.requestPermission(this)

        // Set up the initial fragment
        supportFragmentManager
            .beginTransaction()
            .add(R.id.main_frame, homeFragment, "Home")
            .commit()
    }


    fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view = currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(this)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0);
    }
}
