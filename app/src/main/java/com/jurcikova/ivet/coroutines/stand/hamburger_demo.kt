package com.jurcikova.ivet.coroutines.stand

import com.jurcikova.ivet.coroutines.stand.entity.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce
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
        runBlocking(CoroutineName("")) {
            val hamburgerStand = HamburgerStand(this + Dispatchers.Default)
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

            processOrders(hamburgerStand, orders(quantity.await()))

            hamburgerStand.close()
        }
    }

    log("Orders processed in $time milliseconds")
}

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
suspend fun processOrders(hamburgerStand: HamburgerStand, orders: List<Order>) {
    coroutineScope {
        val ordersChannel =
            produce(CoroutineName("Lenka")) {
                orders.forEach { order ->
                    send(order)
                }
            }

        launch(CoroutineName("Ivet")) {
            makeHamburger(hamburgerStand, ordersChannel)
        }

        launch(CoroutineName("Dominika")) {
            makeHamburger(hamburgerStand, ordersChannel)
        }
    }
}

@ObsoleteCoroutinesApi
private suspend fun makeHamburger(
    stand: HamburgerStand,
    ordersChannel: ReceiveChannel<Order>
) = coroutineScope {
    for (order in ordersChannel) {
        log("Processing ${order.id}. order")

        val vegetable = cutVegetable(order.id)
        val meat = async { stand.fryMeat(Meat(order.id)) }
        val bun = async { stand.heatBun(Bun(order.id)) }
        val hamburger =
            prepareHamburger(vegetable, meat.await(), bun.await())

        log("Serve $hamburger")
    }
}

//business methods
private suspend fun cutVegetable(orderId: Int): Vegetable {
    log("Start cutting vegetable")
    delay(1000)
    log("Stop cutting vegetable")
    return Vegetable(orderId)
}

private suspend fun prepareHamburger(vegetable: Vegetable, meat: Meat, bun: Bun): Hamburger {
    log("Start preparing hamburger")
    delay(500)
    log("Stop preparing hamburger")
    return Hamburger(vegetable, meat, bun)
}

//utils methods
fun addHamburger() = Random.nextBoolean()
