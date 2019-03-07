package com.jurcikova.ivet.coroutines.stand

import com.jurcikova.ivet.coroutines.stand.entity.Order

fun orders(quantity: Int = 1) =
    List(quantity) { index ->
        Order(index + 1)
    }

fun log(message: String) {
    println("[${Thread.currentThread().name}] $message")
}