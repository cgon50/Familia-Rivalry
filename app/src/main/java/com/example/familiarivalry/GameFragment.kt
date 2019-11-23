package com.example.familiarivalry

import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.core.content.ContextCompat.getSystemService
import kotlin.random.Random
import kotlinx.android.synthetic.main.fragment_game.*
import party.liyin.easywifip2p.WifiP2PHelper
import java.util.*
import kotlin.math.min


/**
 *

override fun onCreate(savedInstanceState: Bundle?) {
super.onCreate(savedInstanceState)
setContentView(R.layout.activity_main)
gameFragment = GameFragment.newInstance()
peerFragment = P2pFragment.newInstance()
val qnaFilePath = "familiaRivalryQnAnS.txt"
val assetManager = applicationContext.assets
val inputStream = assetManager.open(qnaFilePath)
val unfilteredLines = inputStream.bufferedReader().use { it.readLines() }
var questionString = unfilteredLines[0]
var answers = mutableListOf<String>()
var answerScores = mutableListOf<Int>()
questionBank = mutableListOf()
answerBank = mutableListOf()
answerScoresBank = mutableListOf()
randomForRound = Random(1)
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

board = mutableListOf(answer1, answer2, answer3, answer4, answer5, answer6, answer7, answer8)
strikeViews = mutableListOf(strike1TV, strike2TV, strike3TV)

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

answerET.setOnEditorActionListener { _, actionId, _ ->
if (actionId == EditorInfo.IME_ACTION_SEARCH) {
this.performGuess()
}
true
}

answerET.text.clear()

guessBut.setOnClickListener {
performGuess()
}

roundNumber++
roundTV.text = "Round $roundNumber"

score = 0
numAnswersGuessed = 0

strikeViews.forEach { it.text = "" }
strikes = 0

question = questionBank[questionNumber]
questionTV.text = "$question"
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
}

private fun performGuess() {
hideKeyboard()
var guess = answerET.text.toString().toLowerCase()
answerET.text.clear()
if (guess.isNullOrBlank())
return
var guessSet = guess.split(" ").toHashSet()
//newlines not possible in this, and if they mess up their guess w other stuff oh well.
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
doStrike()
}
else {
if (answersGuessed[correctGuessNumber - 1] == 0) {
answersGuessed[correctGuessNumber - 1] = 1
numAnswersGuessed++
revealAnswer(correctGuessNumber)
if (numAnswersGuessed == answers.size)
endGame()
}
else
doStrike()
}

}

private fun doStrike() {
strikeViews[strikes].text = "X"
strikes++
if (strikes == 3) {
endGame()
}
}

private fun endGame() {
guessBut.setOnClickListener {
answerET.text.clear()
hideKeyboard()
}
answerET.setOnEditorActionListener { _, _, _ ->
answerET.text.clear()
hideKeyboard()
true
}
val newScore = Integer.parseInt(p1ScoreTV.text.toString()) + score

p1ScoreTV.text = "$newScore"
answers.forEachIndexed { index, _ ->
revealAnswer(index + 1)
}

Timer("newRound", false).schedule(
object : TimerTask() {
override fun run() {
runOnUiThread{ playRound(randomForRound.nextInt(0, questionBank.size - 1)) }
}
} , 6000)


}

private fun revealAnswer(index: Int) {
board[index - 1].text = String.format("${answers[index - 1]}: ${answerScores[index - 1]}")
score += answerScores[index - 1]
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
 *
 */
/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [GameFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [GameFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class GameFragment : Fragment() {
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val gameView = inflater.inflate(R.layout.fragment_game, container, false)
        val qnaFilePath = "familiaRivalryQnAnS.txt"
        val assetManager = context?.assets
        val inputStream = assetManager?.open(qnaFilePath)
        val unfilteredLines = inputStream!!.bufferedReader().use { it?.readLines() }
        var questionString = unfilteredLines[0]
        var answers = mutableListOf<String>()
        var answerScores = mutableListOf<Int>()
        questionBank = mutableListOf()
        answerBank = mutableListOf()
        answerScoresBank = mutableListOf()
        randomForRound = Random(1)
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

        answerET.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                this.performGuess()
            }
            true
        }

        answerET.text.clear()

        guessBut.setOnClickListener {
            performGuess()
        }

        roundNumber++
        roundTV.text = "Round $roundNumber"

        score = 0
        numAnswersGuessed = 0

        strikeViews.forEach { it.text = "" }
        strikes = 0

        question = questionBank[questionNumber]
        questionTV.text = "$question"
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
    }

    private fun performGuess() {
        hideKeyboard()
        var guess = answerET.text.toString().toLowerCase()
        answerET.text.clear()
        if (guess.isNullOrBlank())
            return
        var guessSet = guess.split(" ").toHashSet()
//newlines not possible in this, and if they mess up their guess w other stuff oh well.
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
            doStrike()
        }
        else {
            if (answersGuessed[correctGuessNumber - 1] == 0) {
                answersGuessed[correctGuessNumber - 1] = 1
                numAnswersGuessed++
                revealAnswer(correctGuessNumber)
                if (numAnswersGuessed == answers.size)
                    endGame()
            }
            else
                doStrike()
        }

    }

    private fun doStrike() {
        strikeViews[strikes].text = "X"
        strikes++
        if (strikes == 3) {
            endGame()
        }
    }

    private fun endGame() {
        guessBut.setOnClickListener {
            answerET.text.clear()
            hideKeyboard()
        }
        answerET.setOnEditorActionListener { _, _, _ ->
            answerET.text.clear()
            hideKeyboard()
            true
        }
        val newScore = Integer.parseInt(p1ScoreTV.text.toString()) + score

        p1ScoreTV.text = "$newScore"
        answers.forEachIndexed { index, _ ->
            revealAnswer(index + 1)
        }

        Timer("newRound", false).schedule(
            object : TimerTask() {
                override fun run() {
                    activity!!.runOnUiThread{ playRound(randomForRound.nextInt(0, questionBank.size - 1)) }
                }
            } , 6000)


    }

    private fun revealAnswer(index: Int) {
        board[index - 1].text = String.format("${answers[index - 1]}: ${answerScores[index - 1]}")
        score += answerScores[index - 1]
    }

    fun hideKeyboard() {
//        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
//        var view = currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
//        if (view == null) {
//            view = View(context)
//        }
//        imm.hideSoftInputFromWindow(view.windowToken, 0);
    }

    companion object {
        fun newInstance(): GameFragment {
            return GameFragment()
        }
    }

}
