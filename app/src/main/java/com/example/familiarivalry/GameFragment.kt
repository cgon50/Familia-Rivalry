package com.example.familiarivalry

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import kotlin.random.Random
import kotlinx.android.synthetic.main.fragment_game.*
import party.liyin.easywifip2p.WifiP2PHelper
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.*
import kotlin.math.min

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [GameFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [GameFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class GameFragment : Fragment() {
    private lateinit var gameView: View
    private lateinit var question: String
    private lateinit var answers: List<String>
    private lateinit var answerScores: List<Int>
    private lateinit var answerSets: List<Set<String>>
    private lateinit var board: List<TextView>
    private lateinit var strikeViews: List<TextView>
    private lateinit var questionBank: List<String>
    private lateinit var answerBank: List<List<String>>
    private lateinit var answerScoresBank: List<List<Int>>
    private lateinit var randomForRound: Random
    private lateinit var answersGuessed: MutableList<Int>
    private lateinit var wifiHelper: WifiP2PHelper
    private var strikes = 0
    private var score = 0
    private var roundNumber = 0
    private var numAnswersGuessed = 0
    private var player = MediaPlayer()
    private var singlePlayer = true
    private var myTurn = true
    private var player1 = true
    private var player1RoundWinner = true
    private lateinit var ois: ObjectInputStream
    private lateinit var oos: ObjectOutputStream

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val turn = savedInstanceState?.getBoolean(MY_TURN_BOOL_STRING)
        if (turn != null)
            myTurn = turn

        val single = savedInstanceState?.getBoolean(SINGLE_PLAYER_BOOL_STRING)
        if (single != null)
            singlePlayer = single

        val playerOne = savedInstanceState?.getBoolean(PLAYER_ONE_BOOL_STRING)
        if (playerOne != null)
            player1 = playerOne
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        gameView = inflater.inflate(R.layout.fragment_game, container, false)
        val qnaFilePath = "familiaRivalryQnAnS.txt"
        val assetManager = context?.assets
        val inputStream = assetManager?.open(qnaFilePath)
        val unfilteredLines = inputStream!!.bufferedReader().use { it?.readLines() }
        var questionString = unfilteredLines[0]
        gameView.findViewById<TextView>(R.id.questionTV).text = "hello "

        var answers = mutableListOf<String>()
        var answerScores = mutableListOf<Int>()
        questionBank = mutableListOf()
        answerBank = mutableListOf()
        answerScoresBank = mutableListOf()
        randomForRound = Random(1)
        player = MediaPlayer.create(context, R.raw.wrong_answer_buzzer)

        var isNotScore = true
        for (i in 2 until unfilteredLines.size) {
            var curString = unfilteredLines[i]
            var curInt = curString.toIntOrNull()
            if (curString == " ") {
                addQuestion(questionString, answers, answerScores)
                answers = mutableListOf()
                answerScores = mutableListOf()

                questionString = unfilteredLines[i - 1]
                isNotScore = true
            }
            else if (isNotScore) {
                answers.add(curString)
                isNotScore = false
            }
            else {
                if (curInt != null) {
                    answerScores.add(curInt)
                }
                isNotScore = true
            }

        }

        addQuestion(questionString, answers, answerScores)
        board = mutableListOf(gameView.findViewById(R.id.answer1), gameView.findViewById(R.id.answer2),
            gameView.findViewById(R.id.answer3), gameView.findViewById(R.id.answer4),
            gameView.findViewById(R.id.answer5), gameView.findViewById(R.id.answer6),
            gameView.findViewById(R.id.answer7), gameView.findViewById(R.id.answer8))
        strikeViews = mutableListOf(gameView.findViewById(R.id.strike1TV),
            gameView.findViewById(R.id.strike2TV), gameView.findViewById(R.id.strike3TV))

        Timer("newRound", false).schedule(
            object : TimerTask() {
                override fun run() {
                    playRound(randomForRound.nextInt(0, questionBank.size - 1))
                }
            } , 5)
        // Inflate the layout for this fragment
        return gameView
    }

    private fun addQuestion (questionString: String, answers: MutableList<String>, answerScores: MutableList<Int>) {
        (questionBank as MutableList<String>).add(questionString)
        answers.removeAt(answers.size - 1)
        while (answers.size > 8) {
            answers.removeAt(answers.size - 1)
        }
        while (answerScores.size > 8) {
            answerScores.removeAt(answerScores.size - 1)
        }
        (answerBank as MutableList<List<String>>).add(answers)
        (answerScoresBank as MutableList<List<Int>>).add(answerScores)
    }

    private fun playRound(questionNumber: Int) {

        roundNumber++
        roundTV.text = "Round $roundNumber"

        score = 0
        numAnswersGuessed = 0

        strikeViews.forEach { it.text = "" }
        strikes = 0

        question = questionBank[questionNumber]
        gameView.findViewById<TextView>(R.id.questionTV).text = "$question"
        answers = answerBank[questionNumber]
        answerSets = mutableListOf()
        answersGuessed = mutableListOf()
        answers.forEach {
            (answerSets as MutableList<Set<String>>).add(it.toLowerCase().split(" ").toHashSet())
            answersGuessed.add(0)
        }

        answerScores = answerScoresBank[questionNumber]

        for (i in 1..answers.size) {
            board[i - 1].text = "$i"
        }
        for (i in answers.size until board.size) {
            board[i].text = ""
        }
        if (myTurn) {
            turnInfoTV.text = "Your turn!"
            enableGuessing(steal = false)
        }
        else {
            turnInfoTV.text = "Waiting for opponent guess!"
            disableGuessing()
            Timer("waitForGuess", false).schedule(
                object : TimerTask() {
                    override fun run() {
                        activity!!.runOnUiThread{waitForOpponentGuess(steal = false)}
                    }
                } , 5)
        }
    }

    private fun performGuess(guess: String, steal: Boolean) {

        if (guess.isNullOrBlank())
            return
        if (!singlePlayer && myTurn)
            sendGuessToOtherDevice(guess)

        var guessSet = guess.split(" ").toHashSet()
        // Newlines not possible in this, and if they mess up their guess w other stuff oh well.
        var correctGuessNumber = -1
        val numWordsInGuess = guessSet.size
        for (i in 1..answerSets.size) {
            val overlap = guessSet.intersect(answerSets[i - 1]).size
            if (overlap == min(numWordsInGuess, answerSets[i - 1].size)) {
                if (correctGuessNumber != -1) {
                    correctGuessNumber = -1
                    break
                }
                else {
                    correctGuessNumber = i
                }
            }
        }

        if (correctGuessNumber == -1) {
            doStrike(guess, steal)
        }
        else {
            player = MediaPlayer.create(context, R.raw.right_answer_ding)
            player.start()
            if (answersGuessed[correctGuessNumber - 1] == 0) {
                answersGuessed[correctGuessNumber - 1] = 1
                numAnswersGuessed++
                revealAnswer(correctGuessNumber, steal)
                if (steal) {
                    player1RoundWinner = (player1 && myTurn) || (!player1 && !myTurn)
                    var textToShow = "You stole the points!"
                    if (player1RoundWinner != player1) {
                        textToShow = "Your opponent stole the points! Better luck next round!"
                    }
                    turnInfoTV.text = textToShow
                    endGame(false) // end the game for real, he stole it!
                }
                else if (numAnswersGuessed == answers.size) {
                    player1RoundWinner = (player1 && myTurn) || (!player1 && !myTurn)
                    var textToShow = "You swept the board!"
                    if (player1RoundWinner != player1) {
                        textToShow = "Your opponent swept the board! Better luck next round!"
                    }
                    turnInfoTV.text = textToShow
                    endGame(steal = false) //no chance to steal
                }
                else {
                    if (!myTurn) {
                        //Game still going, gotta wait for next guess.
                        turnInfoTV.text = "Opponent guess was correct, waiting for next guess!"
                        Timer("waitForGuess", false).schedule(
                            object : TimerTask() {
                                override fun run() {
                                    activity!!.runOnUiThread{waitForOpponentGuess(steal = false)}
                                }
                            } , 5)
                    }
                }
            }
            else
                doStrike(guess, steal)
        }

    }

    private fun doStrike(guess: String, steal: Boolean) {
        player = MediaPlayer.create(context, R.raw.wrong_answer_buzzer)
        player.start()
        player.setOnCompletionListener {
            player.stop()
            player.reset()
        }
        if (!myTurn) {
            //Let the waiting player know of the wrong guess
            if (Looper.myLooper() == null) {
                Looper.prepare()
            }
            turnInfoTV.text = "Opponent guess $guess is incorrect!"
        }
        if (steal) {
            player1RoundWinner = (player1 && !myTurn) || (!player1 && myTurn)
            myTurn = !myTurn // winner goes first next round.
            var textToShow = "You did not steal the points, better luck next round!"
            if (player1RoundWinner == player1) {
                textToShow = "Your opponent did not steal the points! You win this round!"
            }
            turnInfoTV.text = textToShow
            endGame(steal = false)
        }
        else {
            strikeViews[strikes].text = "X"
            strikes++
            if (strikes == 3) {
                endGame(steal = !singlePlayer) //only steal in a multiplayer match
            }
            else if (!myTurn) {
                //Game is still going, wait for the next guess!

                Timer("waitForGuess", false).schedule(
                    object : TimerTask() {
                        override fun run() {
                            activity!!.runOnUiThread{waitForOpponentGuess(steal = false)}
                        }
                    } , 5)
            }
        }

    }

    private fun sendGuessToOtherDevice(guess: String) {
        //logic to send message to other device
        oos.writeObject(guess)
        Log.d("SENT", "$guess sent to opponent")
    }

    private fun waitForOpponentGuess(steal: Boolean) {
        var guess = "example"
        Log.d("RECEIVED", "$guess received from opponent")
        hideKeyboard()
        turnInfoTV.text = "${turnInfoTV.text} Waiting for opponent's guess!"
        while (true) {
            guess = ois.readObject() as String
            break
        }

        //wait until guess: String is sent through socket.
        performGuess(guess, steal)
    }

    private fun enableGuessing(steal: Boolean) {
        answerET.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                hideKeyboard()
                var guess = answerET.text.toString().toLowerCase()
                answerET.text.clear()
                hideKeyboard()
                this.performGuess(guess, steal)
            }
            true
        }

        answerET.text.clear()

        guessBut.setOnClickListener {
            hideKeyboard()
            var guess = answerET.text.toString().toLowerCase()
            answerET.text.clear()
            hideKeyboard()
            this.performGuess(guess, steal)
        }
    }

    private fun disableGuessing() {
        guessBut.setOnClickListener {
            answerET.text.clear()
            hideKeyboard()
        }

        answerET.text.clear()

        answerET.setOnEditorActionListener { _, _, _ ->
            answerET.text.clear()
            hideKeyboard()
            true
        }
    }

    private fun endGame(steal: Boolean) {
        disableGuessing()

            if (steal) {
                myTurn = !myTurn //Chance to steal
                if (!myTurn) {
                    //wait for other person's guess
                    Timer("waitForGuess", false).schedule(
                        object : TimerTask() {
                            override fun run() {
                                activity!!.runOnUiThread{waitForOpponentGuess(steal = true)}
                            }
                        } , 5)
                }
                else {
                    enableGuessing(true)
                    if (Looper.myLooper() == null) {
                        Looper.prepare()
                    }
                    turnInfoTV.text = "${turnInfoTV.text} Now's your chance to steal, make a guess!"
                }
            }
            else {
                //actually end the game
                if (singlePlayer) {
                    player1RoundWinner = true
                }
                var newScore: Int
                if (player1RoundWinner) {
                    newScore = Integer.parseInt(p1ScoreTV.text.toString()) + score
                    p1ScoreTV.text = "$newScore"
                }
                else {
                    newScore = Integer.parseInt(p2ScoreTV.text.toString()) + score
                    p2ScoreTV.text = "$newScore"
                }


                answers.forEachIndexed { index, _ ->
                    revealAnswer(index + 1, true)
                }

                Timer("newRound", false).schedule(
                    object : TimerTask() {
                        override fun run() {
                            activity!!.runOnUiThread{ playRound(randomForRound.nextInt(0, questionBank.size - 1)) }
                        }
                    } , 6000)
            }


    }

    private fun revealAnswer(index: Int, steal: Boolean) {
        board[index - 1].text = String.format("${answers[index - 1]}: ${answerScores[index - 1]}")
        if (!steal)
            score += answerScores[index - 1]
    }

    private fun hideKeyboard() {
        (activity as MainActivity).hideKeyboard()
    }

    companion object {
        private const val MY_TURN_BOOL_STRING = "myTurn"
        private const val SINGLE_PLAYER_BOOL_STRING = "singlePlayer"
        private const val PLAYER_ONE_BOOL_STRING = "playerOne"

        fun newInstance(myTurn: Boolean, singlePlayer: Boolean, playerOne: Boolean,
                        ois: ObjectInputStream?, oos: ObjectOutputStream?) = GameFragment().apply {
            arguments = Bundle(3).apply {
                putBoolean(MY_TURN_BOOL_STRING, myTurn)
                putBoolean(SINGLE_PLAYER_BOOL_STRING, singlePlayer)
                putBoolean(PLAYER_ONE_BOOL_STRING, playerOne)
            }
            this.myTurn = myTurn
            this.singlePlayer = singlePlayer
            this.player1 = playerOne
            if (ois != null)
                this.ois = ois
            if (oos != null)
                this.oos = oos
        }
    }

}
