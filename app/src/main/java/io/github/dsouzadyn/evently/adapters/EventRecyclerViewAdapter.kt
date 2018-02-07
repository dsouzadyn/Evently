package io.github.dsouzadyn.evently.adapters

import android.content.Context
import android.graphics.Color
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.github.kittinunf.fuel.httpPatch
import com.github.kittinunf.fuel.httpPost
import com.google.gson.Gson
import io.github.dsouzadyn.evently.MainActivity
import io.github.dsouzadyn.evently.R
import io.github.dsouzadyn.evently.database

import io.github.dsouzadyn.evently.models.Event


import io.github.dsouzadyn.evently.models.EventContent
import org.jetbrains.anko.alert
import org.jetbrains.anko.db.insert
import org.jetbrains.anko.indeterminateProgressDialog
import org.jetbrains.anko.noButton
import org.jetbrains.anko.yesButton

/**
 * [RecyclerView.Adapter] that can display a [DummyItem] and makes a call to the
 * specified [OnListFragmentInteractionListener].
 * TODO: Replace the implementation with code for your data type.
 */
class EventRecyclerViewAdapter(private val mValues: List<EventContent.EventItem>, private val mUid: String) : RecyclerView.Adapter<EventRecyclerViewAdapter.ViewHolder>() {



    data class Acknowledgement(val n: Int, val nModified: Int, val ok: Int) {
        class Deserializer: ResponseDeserializable<Acknowledgement> {
            override fun deserialize(content: String) = Gson().fromJson(content, Acknowledgement::class.java)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_event, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val context = holder.mView.context

        holder.mItem = mValues[position]
        holder.time.text = getTime(mValues[position].start_time, mValues[position].end_time)
        holder.title.text = mValues[position].name
        holder.description.text = mValues[position].description
        holder.price.text = mValues[position].price.toString()
        holder.etype.text = mValues[position].type
        holder.subtype.text = mValues[position].subtype
        holder.location.text = mValues[position].location
        val d = getDay(mValues[position].start_time)
        if (d == "2018-02-22")
            holder.day.text = "Day 1"
        else if (d == "2018-02-23")
            holder.day.text = "Day 2"
        else
            holder.day.text = "Day 3"

        if(mValues[position].type == "TECHNICAL") {
            holder.mView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorRed))
        } else {
            holder.mView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorAccent))
        }

        holder.register.setOnClickListener {
            val sharedPref = context.getSharedPreferences(context.getString(R.string.settings_file), Context.MODE_PRIVATE)
            val conf = sharedPref.getString(context.getString(R.string.conf_key), "")
            if(conf == "") {
                context.alert("Do you wan't to confirm your registration for " + mValues[position].name + " ?", "Confirmation") {
                    yesButton {
                        val progress = context.indeterminateProgressDialog("Confirming...")
                        progress.show()
                        "${context.getString(R.string.base_api_url)}/event/${mValues[position].id}/register".httpPatch()
                                .body("users=$mUid")
                                .responseObject(Acknowledgement.Deserializer()) { _, _, result ->
                                    val (acknowledgement, error) = result
                                    if (error == null) {
                                        progress.dismiss()
                                        if (acknowledgement?.nModified == 1) {
                                            context.alert("Successfully registered").show()
                                            context.database.use {
                                                insert(
                                                        Event.TABLE_NAME,
                                                        Event.COLUMN_ID to mValues[position].id,
                                                        Event.COLUMN_NAME to mValues[position].name,
                                                        Event.COLUMN_PRICE to mValues[position].price,
                                                        Event.COLUMN_UID to mUid,
                                                        Event.COLUMN_LOCATION to mValues[position].location
                                                )
                                            }
                                        } else {
                                            progress.dismiss()
                                            context.alert("Something went wrong!").show()
                                        }
                                    } else {
                                        context.alert(error.localizedMessage).show()
                                    }
                                }
                    }
                    noButton { }
                }.show()
            } else {
                context.alert("Unfortunately you're booking is confirmed and no more registrations are possible!").show()
            }
        }
        holder.mView.setOnClickListener {
            //mListener?.onListFragmentInteraction(holder.mItem!!)
        }
    }

    override fun getItemCount(): Int {
        return mValues.size
    }

    private fun getTime(start_time: String, end_time: String): String {
        return "${start_time.subSequence(11, 16)}-${end_time.subSequence(11, 16)}"
    }

    private fun getDay(start_time: String): String {
        return "${start_time.subSequence(0,10)}"
    }

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val time: TextView
        val title: TextView
        val location: TextView
        val description: TextView
        val etype: TextView
        val subtype: TextView
        val price: TextView
        val register: Button
        val day: TextView

        var mItem: EventContent.EventItem? = null

        init {
            time = mView.findViewById<View>(R.id.itemTime) as TextView
            title = mView.findViewById<View>(R.id.itemTitle) as TextView
            description = mView.findViewById<View>(R.id.itemDescription) as TextView
            price = mView.findViewById<View>(R.id.itemPrice) as TextView
            etype = mView.findViewById<View>(R.id.itemType) as TextView
            subtype = mView.findViewById<View>(R.id.itemSubType) as TextView
            register = mView.findViewById<View>(R.id.itemRegisterBtn) as Button
            location = mView.findViewById<View>(R.id.itemLocation) as TextView
            day = mView.findViewById<View>(R.id.itemDay) as TextView
        }

        override fun toString(): String {
            return super.toString() + " '" + title.text + "'"
        }
    }
}
