package com.jurcikova.ivet.coroutines.stand

import com.jurcikova.ivet.coroutines.stand.entity.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlin.system.measureTimeMillis

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
fun main() {
    val time = measureTimeMillis {
        runBlocking {
            val ordersChannel =
                produce(CoroutineName("Lenka")) {
                    orders(3).forEach { order ->
                        send(order)
                    }
                }

            launch(CoroutineName("Ivet")) {
                makeHamburger(ordersChannel)
            }
            launch(CoroutineName("Dominika")) {
                makeHamburger(ordersChannel)
            }
        }
    }

    log("Order processed in $time milliseconds")
}

@ObsoleteCoroutinesApi
private suspend fun makeHamburger(
    ordersChannel: ReceiveChannel<Order>
) = coroutineScope {
    ordersChannel.consumeEach { order ->
        log("Processing ${order.id}. order")

        val vegetable = cutVegetable(order.id)
        val meat = async { fryMeat(order.id) }
        val bun = async { heatBun(order.id) }
        val hamburger =
            prepareHamburger(
                vegetable,
                meat.await(),
                bun.await()
            )

        log("Serve $hamburger")
    }
}

//business methods
private suspend fun cutVegetable(orderId: Int): Vegetable {
    log("Cutting vegetable")
    delay(1000)
    return Vegetable(orderId)
}

private suspend fun fryMeat(orderId: Int): Meat {
    log("Frying meat")
    delay(2000)
    return Meat(orderId)
}

private suspend fun heatBun(orderId: Int): Bun {
    log("Heating bun")
    delay(500)
    return Bun(orderId)
}

private suspend fun prepareHamburger(vegetable: Vegetable, meat: Meat, bun: Bun): Hamburger {
    log("Preparing hamburger")
    delay(500)
    return Hamburger(vegetable, meat, bun)
}

//utils methods
fun orders(quantity: Int = 1) =
    List(quantity) { index ->
        Order(index + 1)
    }

fun log(message: String) {
    println("[${Thread.currentThread().name}] $message")
}

