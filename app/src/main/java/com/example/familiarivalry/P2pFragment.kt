package com.example.familiarivalry

import android.net.wifi.p2p.WifiP2pDevice
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import party.liyin.easywifip2p.WifiP2PHelper
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.ServerSocket
import java.net.Socket
import java.util.*


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [P2pFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [P2pFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class P2pFragment : Fragment() {
    private lateinit var helper: WifiP2PHelper
    private lateinit var listView: ListView
    private var devices = mutableListOf<WifiP2pDevice>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var p2pView = inflater.inflate(R.layout.fragment_p2p, container, false)
        helper = WifiP2PHelper(context)
        helper.disconnectAll()
        helper.requestConnInfo()
        listView = p2pView.findViewById<ListView>(R.id.peerList)
        listView.setOnItemClickListener { _,_,position,_ ->
            helper.connectToPeer(devices[position])
        }
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        helper.setConnectInfoListener { address, isGroupOwner, groupFormed ->
            Log.d(
                "info",
                "========\nAddress:$address\nisGroupOwner:$isGroupOwner\ngroupFormed:$groupFormed\n========"
            )
            if (isGroupOwner != null && groupFormed) {
                if (isGroupOwner) {
                    val server = ServerSocket(9876)
                        val socket = server.accept()
                        println("client has accepted")
                        val ois = ObjectInputStream(socket.getInputStream())
                        val oos = ObjectOutputStream(socket.getOutputStream())
                        fragmentManager?.beginTransaction()
                            ?.addToBackStack("backToHomeFromSingle")
                            ?.replace(R.id.main_frame, GameFragment.newInstance(myTurn = true, singlePlayer = false, playerOne = true, ois = ois, oos = oos))
                            ?.commit()
                } else {
                    println("starting socket")
                    val socket = Socket(address, 9876)
                    println("we got the socket connected")
                    val oos = ObjectOutputStream(socket.getOutputStream())
                    val ois = ObjectInputStream(socket.getInputStream())

                    fragmentManager?.beginTransaction()
                        ?.addToBackStack("backToHomeFromSingle")
                        ?.replace(R.id.main_frame, GameFragment.newInstance(myTurn = false, singlePlayer = false, playerOne = false, ois = ois, oos = oos))
                        ?.commit()
                }
            }
        }
            helper.setConnectListener(object : WifiP2PHelper.ConnectListener {
                override fun connectState(state: Boolean) {
                    println("Connect State: $state")
                    if (state) {
                        listView.adapter = ArrayAdapter<String>(
                            context!!, android.R.layout.simple_expandable_list_item_1,
                            java.util.ArrayList()
                        )
                        helper.requestConnInfo()
                    }
                }

                override fun connectDone(state: Boolean) {
                    println("Connect Done: $state")
                }
            })

            helper.setPeerListener {
               if (it != null) {
                    devices = it
                    var names = mutableListOf<String>()
                    for (device in devices) {
                        names.add(device.deviceName)
                    }
                    listView.adapter = ArrayAdapter<String>(
                        context!!,
                        android.R.layout.simple_expandable_list_item_1,
                        names
                    )
                }
            }

            p2pView.findViewById<SwipeRefreshLayout>(R.id.refresh).setOnRefreshListener {
                helper.startDiscovery()
                Timer("stopRefresh", false).schedule(
                    object : TimerTask() {
                        override fun run() {
                            activity!!.runOnUiThread {
                                p2pView.findViewById<SwipeRefreshLayout>(R.id.refresh)
                                    .isRefreshing = false
                            }
                        }
                    }, 1000)

            }
            helper.easyStart()
            helper.startDiscovery()
        return p2pView
    }

    companion object {
        fun newInstance(): P2pFragment {
            return P2pFragment()
        }
    }
}
