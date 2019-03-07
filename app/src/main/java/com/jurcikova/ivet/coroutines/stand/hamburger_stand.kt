package com.jurcikova.ivet.coroutines.stand

import com.jurcikova.ivet.coroutines.stand.entity.Bun
import com.jurcikova.ivet.coroutines.stand.entity.Meat
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.delay
import kotlinx.coroutines.selects.select

class HamburgerStand(scope: CoroutineScope) : CoroutineScope by scope {

    data class MicrowaveRequest(
        val bun: Bun,
        val channel: Channel<Bun>
    )

    data class FryingMachineRequest(
        val meat: Meat,
        val channel: Channel<Meat>
    )

    @ObsoleteCoroutinesApi
    private val microwave1 =
        actor<MicrowaveRequest>(CoroutineName("1.microwave")) {
            consumeEach { request ->
                delay(3000) //HEATING
                log("Stop heating ${request.bun.id}. bun")
                request.channel.send(request.bun)
            }
        }

    @ObsoleteCoroutinesApi
    private val microwave2 =
        actor<MicrowaveRequest>(CoroutineName("2.microwave")) {
            consumeEach { request ->
                delay(1000) //HEATING
                log("Stop heating ${request.bun.id}. bun")
                request.channel.send(request.bun)
            }
        }


    @ObsoleteCoroutinesApi
    private val fryingMachine1 =
        actor<FryingMachineRequest>(CoroutineName("1.frying_machine")) {
            consumeEach { request ->
                delay(1000) //FRYING
                log("Stop frying ${request.meat.id}. meat")
                request.channel.send(request.meat)
            }
        }

    @ObsoleteCoroutinesApi
    private val fryingMachine2 =
        actor<FryingMachineRequest>(CoroutineName("2.frying_machine")) {
            consumeEach { request ->
                delay(1000) //FRYING
                log("Stop frying ${request.meat.id}. meat")
                request.channel.send(request.meat)
            }
        }

    @ObsoleteCoroutinesApi
    suspend fun heatBun(bun: Bun): Bun {
        val chan = Channel<Bun>()

        return select<Bun> {
            microwave1.onSend(MicrowaveRequest(bun, chan)) {
                log("Start heating ${bun.id}. bun in the 1. microwave")
                chan.receive()
            }
            microwave2.onSend(MicrowaveRequest(bun, chan)) {
                log("Start heating ${bun.id}. bun in the 2. microwave")
                chan.receive()
            }
        }
    }

    @ObsoleteCoroutinesApi
    suspend fun fryMeat(meat: Meat) =
        select<Meat> {
            val chan = Channel<Meat>()

            fryingMachine1.onSend(FryingMachineRequest(meat, chan)) {
                log("Start frying ${meat.id}. meat in the 1. frying machine ")
                chan.receive()
            }
            fryingMachine2.onSend(FryingMachineRequest(meat, chan)) {
                log("Start frying ${meat.id}. meat in the 2. frying machine")
                chan.receive()
            }

        }

    @ObsoleteCoroutinesApi
    fun close() {
        microwave1.close()
        microwave2.close()
        fryingMachine1.close()
        fryingMachine2.close()
    }
}