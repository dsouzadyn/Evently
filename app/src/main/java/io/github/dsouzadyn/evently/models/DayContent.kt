package io.github.dsouzadyn.evently.models

import java.util.ArrayList
import java.util.HashMap


object DayContent {

    val ITEMS: MutableList<DayItem> = ArrayList()

    val ITEM_MAP: MutableMap<String, DayItem> = HashMap()



    init {
        addItem(createDayItem(1, "DAY 1"))
        addItem(createDayItem(2, "DAY 2"))
        addItem(createDayItem(3, "DAY 3"))
        addItem(createDayItem(4, "MY EVENTS"))
    }

    private fun addItem(item: DayItem) {
        ITEMS.add(item)
        ITEM_MAP.put(item.id, item)
    }

    private fun createDayItem(position: Int, content: String): DayItem {
        return DayItem(position.toString(), content, makeDetails(position))
    }

    private fun makeDetails(position: Int): String {
        val builder = StringBuilder()
        builder.append("Details about Item: ").append(position)
        for (i in 0 until position) {
            builder.append("\nMore details information here.")
        }
        return builder.toString()
    }

    class DayItem(val id: String, val content: String, val details: String) {

        override fun toString(): String {
            return content
        }
    }
}
