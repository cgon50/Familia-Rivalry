package com.example.familiarivalry


import android.media.MediaPlayer
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button

/**
 * A simple [Fragment] subclass.
 */
class HomeFragment : Fragment() {

    private lateinit var player: MediaPlayer

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val homeView = inflater.inflate(R.layout.fragment_home, container, false)
        player = MediaPlayer.create(context, R.raw.theme_song)
        player.start()
        // Inflate the layout for this fragment
        homeView.findViewById<Button>(R.id.game).setOnClickListener {
            player.stop()
            fragmentManager?.beginTransaction()
                ?.addToBackStack("backToHomeFromSingle")
                ?.replace(R.id.main_frame, GameFragment.newInstance(myTurn = true, singlePlayer = true, playerOne = true, ois = null, oos = null))
                ?.commit()
        }
        homeView.findViewById<Button>(R.id.p2p).setOnClickListener {
            player.stop()
            fragmentManager?.beginTransaction()
                ?.addToBackStack("backToHomeFromP2p")
                ?.replace(R.id.main_frame, P2pFragment.newInstance())
                ?.commit()
        }
        return homeView
    }

    companion object {
        fun newInstance(): HomeFragment {
            return HomeFragment()
        }
    }
}
