package io.github.dsouzadyn.evently.models

/**
 * Created by laptop64 on 1/16/18.
 */
data class Event(val id: String, val uid: String, val name: String, val price: Long, val location: String) {
    companion object {
        val COLUMN_ID = "id"
        val TABLE_NAME = "events"
        val COLUMN_UID = "uid"
        val COLUMN_NAME = "name"
        val COLUMN_PRICE = "price"
        val COLUMN_LOCATION = "location"
    }
}