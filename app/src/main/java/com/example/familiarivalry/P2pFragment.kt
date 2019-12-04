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
        helper.setConnectInfoListener { address, isGroupOwner , groupFormed ->
            Log.d("ASDF","ASDF")
            Log.d("info", "========\nAddress:$address\nisGroupOwner:$isGroupOwner\ngroupFormed:$groupFormed\n========")
            if(isGroupOwner != null && groupFormed) {
                if(isGroupOwner) {
                    //  we are the player 1!
                    val server = ServerSocket(9981)

                    println("Server running on port ${server.localPort}")
                    println(server.localSocketAddress)
                    val client = server.accept()
                    println("Client connected : ${client.inetAddress.hostAddress}")
                    // somehow waits for player 2 to connect and send a message
                    val scanner = Scanner(client.inputStream)
                    while (scanner.hasNextLine()) {
                        p2pView.findViewById<TextView>(R.id.communicate).text = scanner.nextLine()
                        break
                    }

//                    client.outputStream.write("Hello from ur dad lol".toByteArray())
//                    client.outputStream.flush()
//                    client.getOutputStream().write("Response from the server!".toByteArray())
//                    fragmentManager?.beginTransaction()
//                        ?.addToBackStack("backToHomeFromSingle")
//                        // We have the host as player 1.
//                        ?.replace(R.id.main_frame, GameFragment.newInstance(myTurn = true, singlePlayer = false, playerOne = true))
//                        ?.commit()

                    server.close()
                } else {
                    // we are player 2!
                    Log.d("192.168.49.1", address)
                    Log.d("EQUIVALENT??", "192.168.49.1".equals(address).toString())
                    val client = Socket(address, 9981)
                    client.outputStream.write("Hello from the client!".toByteArray())
                    client.outputStream.flush()

                    // somehow waits for player 2 to connect and send a message
//                    val scanner = Scanner(client.inputStream)
//                    while (scanner.hasNextLine()) {
//                        p2pView.findViewById<TextView>(R.id.communicate).text = scanner.nextLine()
//                        break
//                    }


//                    val scanner = Scanner(client.getInputStream())
//                    while (scanner.hasNextLine()) {
//                        p2pView.findViewById<TextView>(R.id.communicate).text = scanner.nextLine()
//                        break
//                    }
//                    fragmentManager?.beginTransaction()
//                        ?.addToBackStack("backToHomeFromSingle")
//                        // We have the host as player 1.
//                        ?.replace(R.id.main_frame, GameFragment.newInstance(myTurn = false, singlePlayer = false, playerOne = false))
//                        ?.commit()
                    client.close()
                }
            }
        }
        helper.setConnectListener(object : WifiP2PHelper.ConnectListener{

            override fun connectState(state: Boolean) {
                Log.d("ASDF","CONNECTSTATE")
                println("Connect State: $state")
                if(state) {
                    listView.adapter = ArrayAdapter<String>(context!!, android.R.layout.simple_expandable_list_item_1,
                        java.util.ArrayList())
                    helper.requestConnInfo()
                    //if state is true, start game :)

                }
            }

            override fun connectDone(state: Boolean) {
                Log.d("ASDF","CONNECTDONE")
                println("Connect Done: $state")
            }

        })
        helper.setPeerListener {
            Log.d("ASDF","LISTENER")
            if (it != null) {
                devices = it
                var names = mutableListOf<String>()
                for (device in devices) {
                    names.add(device.deviceName)
                }
                listView.adapter = ArrayAdapter<String>(context!!, android.R.layout.simple_expandable_list_item_1, names)
            }
            it.forEach({})
        }
        p2pView.findViewById<SwipeRefreshLayout>(R.id.refresh).setOnRefreshListener {
            helper.startDiscovery()
            Timer("stopRefresh", false).schedule(
                object : TimerTask() {
                    override fun run() {
                        activity!!.runOnUiThread{ p2pView.findViewById<SwipeRefreshLayout>(R.id.refresh).isRefreshing = false }
                    }
                } , 1000)

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
