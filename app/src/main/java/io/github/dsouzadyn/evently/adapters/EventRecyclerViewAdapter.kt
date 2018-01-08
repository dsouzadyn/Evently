package io.github.dsouzadyn.evently.adapters

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.github.kittinunf.fuel.httpPost
import com.google.gson.Gson
import io.github.dsouzadyn.evently.R


import io.github.dsouzadyn.evently.models.EventContent

/**
 * [RecyclerView.Adapter] that can display a [DummyItem] and makes a call to the
 * specified [OnListFragmentInteractionListener].
 * TODO: Replace the implementation with code for your data type.
 */
class EventRecyclerViewAdapter(private val mValues: List<EventContent.EventItem>, private val mUid: String) : RecyclerView.Adapter<EventRecyclerViewAdapter.ViewHolder>() {

    data class Acknowledgement(val data: String) {
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
        holder.mItem = mValues[position]
        holder.time.text = getTime(mValues[position].start_time, mValues[position].end_time)
        holder.title.text = mValues[position].name
        holder.description.text = mValues[position].description
        holder.price.text = mValues[position].price.toString()
        holder.register.setOnClickListener {
            "http://192.168.1.7:3000/users/$mUid/events".httpPost()
                    .body("id=${mValues[position].id}")
                    .responseObject(Acknowledgement.Deserializer()) {_, _, result ->
                        val (acknowledgement, error) = result
                        if (error == null) {
                            // TODO show a dialog confirmation
                        } else {
                            // TODO show a dialog failed
                        }
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

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val time: TextView
        val title: TextView
        val description: TextView
        val price: TextView
        val register: Button

        var mItem: EventContent.EventItem? = null

        init {
            time = mView.findViewById<View>(R.id.itemTime) as TextView
            title = mView.findViewById<View>(R.id.itemTitle) as TextView
            description = mView.findViewById<View>(R.id.itemDescription) as TextView
            price = mView.findViewById<View>(R.id.itemPrice) as TextView
            register = mView.findViewById<View>(R.id.itemRegisterBtn) as Button

        }

        override fun toString(): String {
            return super.toString() + " '" + title.text + "'"
        }
    }
}
