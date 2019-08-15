package com.shakil.chitchat


import androidx.lifecycle.Transformations.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import java.io.IOException
import java.lang.System.currentTimeMillis


/*
fun main() = runBlocking<Unit> {

    flow{
        emit(1)
    }.collect{
        println("data $it")
    }

}
*/

fun requestFlow(i: Int): Flow<String> = flow {
    emit("$i: First")
    delay(500 * i.toLong()) // wait 500 ms
    emit("$i: Second")
}


fun main() = runBlocking<Unit> {

    val startTime = currentTimeMillis() // remember the start time
    (1..3).asFlow().onEach { delay(100) } // a number every 100 ms
        .flatMapMerge { requestFlow(it) }
        .collect { value -> // collect and print
            println("$value at ${currentTimeMillis() - startTime} ms from start")
        }

}


/*
fun main() = runBlocking<Unit> {

    flow{
        emit(1)
        delay(100)
        emit(2)
        delay(100)
        emit(3)
        delay(100)
    }.sample(100).collect{
        println("data $it")
    }

}
*/

/*
fun main() = runBlocking<Unit> {

    flow{
        emit(1)
        delay(100)
        emit(2)
        delay(100)
        emit(3)
        delay(100)
    }.sample(100).collect{
        println("data $it")
    }

}
*/


/*fun main() = runBlocking<Unit> {

    flowOf(1,2,3).scan(emptyList<Int>(),{ acc, value ->
        println("acc $acc value $value")
        acc + value
    }).collect{
        println("$it")
    }

}*/

/*fun main() = runBlocking<Unit> {

    flow {
        repeat(10) {
            emit(it)
            delay(50)
        }
    }.sample(100).collect {
            println(it)
    }



}*/


/*fun main() = runBlocking<Unit> {

    flow {
         emit(1)
         delay(99)
         emit(2)
         delay(99)
         emit(3)
         delay(1001)
         emit(4)
         delay(1002)
         emit(5)
     }.debounce(1000).collect {
                println(it)   // print  3 , 5
     }



}*/


/*fun main() = runBlocking<Unit> {

    val flow = flowOf(1, 2, 3).onEach{
            delay(1000)
            if(it == 3) throw IOException("error on flow 1")
            println("flow 1 $it")
        }.catch { e ->  emit(3) }

    val flow2 = flowOf("a", "b", "c").onEach{
        delay(2000)
        println("flow 2 $it")
    }


    flow.zip(flow2) { i, s -> i.toString() + s }.collect {
        println(it) // Will print "1a 2a 2b 2c"
    }

}*/

/*fun main() = runBlocking<Unit> {

    val flow = flowOf(1, 2,3).onEach{delay(10)}
    val flow2 = flowOf("a", "b", "c").onEach{delay(15)}


    flow.combine(flow2) { i, s -> i.toString() + s }.collect {
         println(it) // Will print "1a 2a 2b 2c"
     }

}*/

/*fun main() = runBlocking<Unit> {

    flowData()
        .transformLatest { value ->

             emit("new 1 for $value")
             delay(1000)
             emit("new 2 for $value")
             delay(1000)
             emit("new 3 for $value")

        }
        .collect {
            println("data $it")

            flowData()
        .flatMapLatest { value ->
            flow{
                emit(value)
                delay(200)
                emit(value)
            }
        }
        .collect {
            println("data $it")
    }



}*/


