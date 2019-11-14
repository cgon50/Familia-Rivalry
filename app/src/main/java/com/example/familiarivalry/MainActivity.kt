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
import kotlin.math.min

class MainActivity : AppCompatActivity() {

    private lateinit var question: String
    private lateinit var answers: List<String>
    private lateinit var answerScores: List<Int>
    private lateinit var answerSets: List<Set<String>>
    private lateinit var board: List<TextView>
    private lateinit var strikeViews: List<TextView>
    private lateinit var questionBank: List<String>
    private lateinit var answerBank: List<List<String>>
    private lateinit var answerScoresBank: List<List<Int>>
    private var strikes = 0
    private var score = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
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
        
        var isNotScore = true
        for (i in 2 until unfilteredLines.size) {
            var curString = unfilteredLines[i]
            var curInt = curString.toIntOrNull()
            if (curString == " ") {
                (questionBank as MutableList<String>).add(questionString)
                answers.removeAt(answers.size - 1)
                (answerBank as MutableList<List<String>>).add(answers)
                (answerScoresBank as MutableList<List<Int>>).add(answerScores)

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
                if (curInt != null)
                    answerScores.add(curInt)
                isNotScore = true
            }
            
        }

        (questionBank as MutableList<String>).add(questionString)
        (answerBank as MutableList<List<String>>).add(answers)
        (answerScoresBank as MutableList<List<Int>>).add(answerScores)

        board = mutableListOf(answer1, answer2, answer3, answer4, answer5, answer6, answer7, answer8)
        strikeViews = mutableListOf(strike1TV, strike2TV, strike3TV)

        playRound()
    }

    private fun playRound() {

        answerET.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                this.performGuess()
            }
            true
        }

        guessBut.setOnClickListener {
            performGuess()
        }

        score = 0

        strikeViews.forEach { it.text = "" }
        strikes = 0

        question = "What is your favorite vowel?"
        questionTV.text = "$question"
        answers = mutableListOf("a", "e", "it is i", "o", "raw shrimp", "raw chicken")
        answerSets = mutableListOf()
        answers.forEach { (answerSets as MutableList<Set<String>>).add(it.split(" ").toHashSet()) }

        answerScores = mutableListOf(20, 15, 10, 8, 8, 5)

        for (i in 1..answers.size) {
            board[i - 1].text = "$i"
        }
        for (i in answers.size until board.size) {
            board[i].text = ""
        }
    }

    private fun performGuess() {
        hideKeyboard()
        var guess = answerET.text.toString()
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
            revealAnswer(correctGuessNumber)
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
        guessBut.setOnClickListener { hideKeyboard() }
        answerET.setOnEditorActionListener { _, _, _ ->
            hideKeyboard()
            true
        }
        val newScore = Integer.parseInt(p1ScoreTV.text.toString()) + score

        p1ScoreTV.text = "$newScore"

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
}
