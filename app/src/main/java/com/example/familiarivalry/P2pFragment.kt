package com.example.familiarivalry

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.net.wifi.p2p.WifiP2pDevice
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import party.liyin.easywifip2p.WifiP2PHelper

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
        listView = p2pView.findViewById<ListView>(R.id.peerList)
        listView.setOnItemClickListener { parent,_,position,_ ->
            val item = parent.getItemAtPosition(position) as WifiP2pDevice
            helper.connectToPeer(item)
        }
        helper.setConnectInfoListener { address, isGroupOwner, groupFormed ->
            println("========\nAddress:$address\nisGroupOwner:$isGroupOwner\ngroupFormed:$groupFormed\n========")
        }
        helper.setConnectListener(object : WifiP2PHelper.ConnectListener{
            override fun connectState(state: Boolean) {
                println("Connect State: $state")
            }

            override fun connectDone(state: Boolean) {
                println("Connect Done: $state")
            }

        })
        helper.setPeerListener {
            listView.adapter = ArrayAdapter<WifiP2pDevice>(context!!, android.R.layout.simple_expandable_list_item_1, it)
        }

        helper.startDiscovery()

        return p2pView
    }

    companion object {
        fun newInstance(): P2pFragment {
            return P2pFragment()
        }
    }
}