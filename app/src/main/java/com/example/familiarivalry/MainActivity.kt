package com.example.familiarivalry

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import party.liyin.easywifip2p.WifiP2PHelper


class MainActivity : AppCompatActivity() {

    private lateinit var gameFragment: GameFragment
    private lateinit var peerFragment: P2pFragment
    private lateinit var homeFragment: HomeFragment

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

    override fun onBackPressed() {
        AlertDialog.Builder(this)
            .setMessage("Are you sure you want to exit?\nYou will not be able to return to the game.")
            .setCancelable(false)
            .setPositiveButton("Yes") { _, _ -> super@MainActivity.onBackPressed() }
            .setNegativeButton("No", null)
            .show()
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
