package com.example.familiarivalry

import android.net.wifi.p2p.WifiP2pDevice
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import party.liyin.easywifip2p.WifiP2PHelper
import java.util.*

import java.net.*
import android.os.StrictMode
import android.widget.TextView
import java.io.*
import java.lang.System.out
import android.system.Os.socket




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
    private var isGroupOwner = false
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
        Log.d("ASDF","CREATION")
        helper = WifiP2PHelper(context)
        helper.disconnectAll()
        helper.requestConnInfo()
        listView = p2pView.findViewById<ListView>(R.id.peerList)
        listView.setOnItemClickListener { _,_,position,_ ->
            helper.connectToPeer(devices[position])
        }
        // TODO CHANGE THIS!! PERF ISSUES
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        helper.setConnectInfoListener { address, isGroupOwner, groupFormed ->
            Log.d("ASDF", "ASDF")
            Log.d(
                "info",
                "========\nAddress:$address\nisGroupOwner:$isGroupOwner\ngroupFormed:$groupFormed\n========"
            )
            if (isGroupOwner != null && groupFormed) {
                if (isGroupOwner) {
                    val server = ServerSocket(9876)
//                    while (true) {
                        val socket = server.accept()
                        println("client has accepted")
                        val ois = ObjectInputStream(socket.getInputStream())
//                        val message = ois.readObject()
//                        println("server recieved: " + message)
                        val oos = ObjectOutputStream(socket.getOutputStream())
                        fragmentManager?.beginTransaction()
                            ?.addToBackStack("backToHomeFromSingle")
                            ?.replace(R.id.main_frame, GameFragment.newInstance(myTurn = true, singlePlayer = false, playerOne = true, ois = ois, oos = oos))
                            ?.commit()
//                        oos.writeObject("Hello client!")
//                        ois.close()
//                        oos.close()
//                        socket.close()
//                        //terminate the server if client sends exit request
//                        if (message.equals("exit")) break
//                    }
//                    println("Shutting down Socket server!!");
//                    //close the ServerSocket object
//                    server.close();
                } else {
                    println("starting socket")
                    val socket = Socket(address, 9876)
                    println("we got the socket connected")
                    val oos = ObjectOutputStream(socket.getOutputStream())
//                    println("sending request to server!")
//                    oos.writeObject("hello from the client!")
//                    oos.writeObject("exit")
                    val ois = ObjectInputStream(socket.getInputStream())

                    fragmentManager?.beginTransaction()
                        ?.addToBackStack("backToHomeFromSingle")
                        ?.replace(R.id.main_frame, GameFragment.newInstance(myTurn = false, singlePlayer = false, playerOne = false, ois = ois, oos = oos))
                        ?.commit()

                    // reading
//                    println("reading")
//                    val message = ois.readObject() as String
//                    println("Message: $message")
//                    //close resources
//                    ois.close()
//                    oos.close()
//                    Thread.sleep(100)
                }
            }
        }
            helper.setConnectListener(object : WifiP2PHelper.ConnectListener {

                override fun connectState(state: Boolean) {
                    Log.d("ASDF", "CONNECTSTATE")
                    println("Connect State: $state")
                    if (state) {
                        listView.adapter = ArrayAdapter<String>(
                            context!!, android.R.layout.simple_expandable_list_item_1,
                            java.util.ArrayList()
                        )
                        helper.requestConnInfo()
                        //if state is true, start game :)

                    }
                }

                override fun connectDone(state: Boolean) {
                    Log.d("ASDF", "CONNECTDONE")
                    println("Connect Done: $state")
                }

            })

            helper.setPeerListener {
                Log.d("ASDF", "LISTENER")
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

//        helper.requestConnInfo()
        return p2pView
    }


    fun getIpv4HostAddress(): String {
        NetworkInterface.getNetworkInterfaces()?.toList()?.map { networkInterface ->
            networkInterface.inetAddresses?.toList()?.find {
                !it.isLoopbackAddress && it is Inet4Address
            }?.let { return it.hostAddress }
        }
        return ""
    }

    companion object {
        fun newInstance(): P2pFragment {
            return P2pFragment()
        }
    }
}
