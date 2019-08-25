package com.shakil.chitchat.extension

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.CheckResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.isActive


@CheckResult
private fun listener(
    scope: CoroutineScope,
    emitter: (Unit) -> Boolean
) = View.OnClickListener {
    if (scope.isActive) { emitter(Unit) }
}

/**
 * Create a flow which emits on [View] click events
 *
 * *Warning:* The created flow uses [View.setOnClickListener] to emmit clicks. Only one flow can
 * be used for a view at a time.
 */
@CheckResult
fun View.clicks(): Flow<Unit> = channelFlow {
    setOnClickListener(listener(this, ::offer))
    awaitClose { setOnClickListener(null) }
}

/**
 * Create a flow of character sequences for text changes on [TextView].
 *
 * *Note:* A value will be emitted immediately on collect.
 */
@CheckResult
fun TextView.textChanges(): Flow<CharSequence> = channelFlow {
    offer(text)
    val listener = TextViewlistener(this, ::offer)
    addTextChangedListener(listener)
    awaitClose { removeTextChangedListener(listener) }
}

@CheckResult
private fun TextViewlistener(
    scope: CoroutineScope,
    emitter: (CharSequence) -> Boolean
) = object : TextWatcher {

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) { }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        if (scope.isActive) { emitter(s) }
    }

    override fun afterTextChanged(s: Editable) { }
}
