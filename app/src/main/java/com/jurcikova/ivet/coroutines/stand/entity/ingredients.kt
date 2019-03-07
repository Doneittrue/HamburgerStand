package com.jurcikova.ivet.coroutines.stand.entity

data class Meat(
    val id: Int
)

data class Vegetable(
    val id: Int
)

data class Bun(
    val id: Int
)

data class Hamburger(
    val vegetable: Vegetable,
    val meat: Meat,
    val bun: Bun
)