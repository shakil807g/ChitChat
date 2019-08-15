package com.shakil.chitchat.extension

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

fun View.clicks(): Flow<Unit> =
    callbackFlow {
        setOnClickListener {
            offer(Unit)
        }
        awaitClose { setOnClickListener(null) }
    }




fun EditText.textChanges(): Flow<CharSequence?> =
    callbackFlow {
        val wacther =  object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                offer(s)
            }

        }
        addTextChangedListener(wacther)
        awaitClose {
            removeTextChangedListener(wacther)
        }
    }