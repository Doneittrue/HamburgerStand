package com.jurcikova.ivet.coroutines.stand

import com.jurcikova.ivet.coroutines.stand.entity.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlin.random.Random
import kotlin.system.measureTimeMillis

sealed class QuantityCounterMsg {
    object IncQuantity : QuantityCounterMsg()
    object DcrQuantity : QuantityCounterMsg()
    class GetQuantity(val response: CompletableDeferred<Int>) : QuantityCounterMsg()
}

@ObsoleteCoroutinesApi
fun CoroutineScope.quantityCounterActor() =
    actor<QuantityCounterMsg> {
        var quantity = 0

        channel.consumeEach { message ->
            when (message) {
                is QuantityCounterMsg.IncQuantity -> quantity++
                is QuantityCounterMsg.DcrQuantity -> if (quantity > 0) quantity--
                is QuantityCounterMsg.GetQuantity -> message.response.complete(quantity)
            }
        }
    }

suspend fun massiveOrder(people: Int, requests: Int, action: suspend () -> Unit) {
    coroutineScope {
        List(people) {
            launch(CoroutineName("Person $it")) {
                repeat(requests) {
                    action()
                }
            }
        }
    }

    log("Created ${people * requests} requests")
}

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
fun main() {
    val time = measureTimeMillis {
        runBlocking(CoroutineName("Ivet")) {
            val quantityCounter = quantityCounterActor()

            massiveOrder(people = 8, requests = 3) {
                if (addHamburger()) {
                    quantityCounter.send(QuantityCounterMsg.IncQuantity)
                } else {
                    quantityCounter.send(QuantityCounterMsg.DcrQuantity)
                }
            }

            val quantity = CompletableDeferred<Int>().apply {
                quantityCounter.send(QuantityCounterMsg.GetQuantity(this))
            }

            quantityCounter.close()

            log("Hamburgers to make: ${quantity.await()}")

            processOrders(orders(quantity = quantity.await()))
        }
    }

    log("Orders processed in $time milliseconds")
}

@ExperimentalCoroutinesApi
suspend fun processOrders(orders: List<Order>) {
    coroutineScope {
        val ordersChannel =
            produce(CoroutineName("Lenka")) {
                orders.forEach { order ->
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

private suspend fun makeHamburger(
    ordersChannel: ReceiveChannel<Order>
) = coroutineScope {
    for (order in ordersChannel) {
        log("Processing ${order.id}. order")

        val vegetable = cutVegetable(order.id)
        val meat = async { fryMeat(order.id) }
        val bun = async { heatBun(order.id) }
        val hamburger =
            prepareHamburger(vegetable, meat.await(), bun.await())

        log("Serve $hamburger")
    }
}

//business methods
private suspend fun cutVegetable(orderId: Int): Vegetable {
    log("Cutting vegetable")
    delay(100)
    return Vegetable(orderId)
}

private suspend fun fryMeat(orderId: Int): Meat {
    log("Frying meat")
    delay(300)
    return Meat(orderId)
}

private suspend fun heatBun(orderId: Int): Bun {
    log("Heating bun")
    delay(200)
    return Bun(orderId)
}

private suspend fun prepareHamburger(vegetable: Vegetable, meat: Meat, bun: Bun): Hamburger {
    log("Preparing hamburger")
    delay(100)
    return Hamburger(vegetable, meat, bun)
}

//utils methods
private fun orders(quantity: Int = 1) =
    List(quantity) { index ->
        Order(index + 1)
    }

private fun log(message: String) {
    println("[${Thread.currentThread().name}] $message")
}

private fun addHamburger() = Random.nextBoolean()

