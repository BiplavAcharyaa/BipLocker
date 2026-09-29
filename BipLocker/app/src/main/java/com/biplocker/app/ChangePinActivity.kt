package com.biplocker.app

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

class ChangePinActivity : SecureActivity() {

    private lateinit var storage: LocalStorage
    private lateinit var screenTitle: TextView
    private lateinit var messageText: TextView
    private lateinit var changeButton: Button
    private lateinit var fieldsCard: View
    private lateinit var padCard: View
    private lateinit var fieldViews: Array<TextView>
    private lateinit var groupViews: Array<View>

    private val values = arrayOf(StringBuilder(), StringBuilder(), StringBuilder())
    private var active = 0
    private var setupMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_pin)

        storage = LocalStorage(this)
        setupMode = !storage.hasPin()

        screenTitle = findViewById(R.id.screenTitle)
        messageText = findViewById(R.id.messageText)
        changeButton = findViewById(R.id.changeButton)
        fieldsCard = findViewById(R.id.fieldsCard)
        padCard = findViewById(R.id.padCard)
        fieldViews = arrayOf(
            findViewById<TextView>(R.id.currentField),
            findViewById<TextView>(R.id.newField),
            findViewById<TextView>(R.id.confirmField)
        )
        groupViews = arrayOf(
            findViewById<View>(R.id.currentFieldGroup),
            findViewById<View>(R.id.newFieldGroup),
            findViewById<View>(R.id.confirmFieldGroup)
        )

        if (setupMode) {
            groupViews[0].visibility = View.GONE
            screenTitle.setText(R.string.set_pin)
            changeButton.setText(R.string.save_pin)
            active = 1
        }

        fieldViews.forEachIndexed { index, view ->
            view.setOnClickListener {
                active = index
                refresh()
            }
        }

        val pad: PinPad = findViewById(R.id.pinPad)
        pad.onDigit = { digit -> appendDigit(digit) }
        pad.onBackspace = { removeDigit() }
        pad.onSubmit = { advanceOrSubmit() }
        changeButton.setOnClickListener { submit() }
        findViewById<View>(R.id.backButton).setOnClickListener { finish() }

        refresh()
        fieldsCard.fadeIn(0L)
        padCard.fadeIn(90L)
    }

    private fun firstIndex(): Int = if (setupMode) 1 else 0

    private fun appendDigit(digit: Char) {
        val current = values[active]
        if (current.length >= LocalStorage.PIN_MAX_LENGTH) return
        current.append(digit)
        messageText.text = ""
        refresh()
    }

    private fun removeDigit() {
        val current = values[active]
        if (current.isNotEmpty()) current.setLength(current.length - 1)
        messageText.text = ""
        refresh()
    }

    private fun advanceOrSubmit() {
        if (active < 2) {
            active++
            refresh()
        } else {
            submit()
        }
    }

    private fun refresh() {
        fieldViews.forEachIndexed { index, view ->
            view.text = pinDots(values[index].length, "")
            view.isSelected = index == active
            view.setBackgroundResource(
                if (index == active) R.drawable.bg_input_active else R.drawable.bg_input
            )
        }
    }

    private fun clearAll() {
        values.forEach { it.setLength(0) }
        active = firstIndex()
        refresh()
    }

    private fun fail(messageRes: Int, focus: Int) {
        messageText.setText(messageRes)
        fieldsCard.shake()
        active = focus
        refresh()
    }

    private fun submit() {
        val current = values[0].toString()
        val next = values[1].toString()
        val confirm = values[2].toString()

        if (!setupMode) {
            if (current.isEmpty()) {
                fail(R.string.enter_current_pin, 0)
                return
            }
            if (!storage.verifyPin(current)) {
                values[0].setLength(0)
                fail(R.string.current_pin_incorrect, 0)
                return
            }
        }
        if (next.isEmpty()) {
            fail(R.string.pin_empty, 1)
            return
        }
        if (next.length < LocalStorage.PIN_MIN_LENGTH) {
            messageText.text = getString(R.string.pin_too_short, LocalStorage.PIN_MIN_LENGTH)
            fieldsCard.shake()
            active = 1
            refresh()
            return
        }
        if (next != confirm) {
            values[2].setLength(0)
            fail(R.string.pins_do_not_match, 2)
            return
        }
        storage.setPin(next)
        AppSession.unlocked = true
        clearAll()
        Toast.makeText(this, if (setupMode) R.string.pin_saved else R.string.pin_changed, Toast.LENGTH_SHORT).show()
        finish()
    }

    override fun onDestroy() {
        values.forEach { it.setLength(0) }
        super.onDestroy()
    }
}
