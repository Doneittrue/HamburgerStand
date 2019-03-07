package com.jurcikova.ivet.coroutines.stand

import com.jurcikova.ivet.coroutines.stand.entity.*
import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis

fun main() {
    val time = measureTimeMillis {
        runBlocking {
            launch(CoroutineName("Ivet")) {
                makeHamburger(orders())
            }
        }
    }

    log("Order processed in $time milliseconds")
}

private fun makeHamburger(orders: List<Order>) {
    runBlocking {
        orders.forEach { order ->
            log("Processing ${order.id}. order")

            val vegetable = cutVegetable(order.id)
            val meat = async { fryMeat(order.id) }
            val bun = async { heatBun(order.id) }
            val hamburger = prepareHamburger(vegetable, meat.await(), bun.await())

            log("Serve $hamburger")
        }
    }
}

//business methods
private suspend fun cutVegetable(orderId: Int): Vegetable {
    log("Start cutting vegetable")
    delay(1000)
    log("Stop cutting vegetable")
    return Vegetable(orderId)
}

private suspend fun fryMeat(orderId: Int): Meat {
    log("Start frying meat")
    delay(2000)
    log("Stop frying meat")
    return Meat(orderId)
}

private suspend fun heatBun(orderId: Int): Bun {
    log("Start heating bun")
    delay(500)
    log("Stop heating bun")
    return Bun(orderId)
}

private suspend fun prepareHamburger(vegetable: Vegetable, meat: Meat, bun: Bun): Hamburger {
    log("Start preparing hamburger")
    delay(500)
    log("Stop preparing hamburger")
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