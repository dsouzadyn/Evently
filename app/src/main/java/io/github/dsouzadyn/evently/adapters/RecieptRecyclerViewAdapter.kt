package io.github.dsouzadyn.evently.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import io.github.dsouzadyn.evently.R

import io.github.dsouzadyn.evently.fragments.RecieptFragment.OnListFragmentInteractionListener
import io.github.dsouzadyn.evently.models.Event
import io.github.dsouzadyn.evently.models.EventContent

/**
 * [RecyclerView.Adapter] that can display a [DummyItem] and makes a call to the
 * specified [OnListFragmentInteractionListener].
 * TODO: Replace the implementation with code for your data type.
 */
class RecieptRecyclerViewAdapter(var mEvents: List<Event>, private val mListener: OnListFragmentInteractionListener?) : RecyclerView.Adapter<RecieptRecyclerViewAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_reciept, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if(mEvents != null) {
            holder.mItem = mEvents!![position]
            holder.mIdView.text = mEvents!![position].name
            holder.mContentView.text = mEvents!![position].id
        }
        holder.mView.setOnClickListener {
            //mListener?.onListFragmentInteraction(holder.mItem)
        }
    }

    override fun getItemCount(): Int {
        if (mEvents != null) {
            return mEvents!!.size
        }
        return 0
    }

    fun setEvents(events: List<Event>) {
        this.mEvents = events
        notifyDataSetChanged()
    }

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mIdView: TextView
        val mContentView: TextView
        var mItem: Event? = null

        init {
            mIdView = mView.findViewById<View>(R.id.id) as TextView
            mContentView = mView.findViewById<View>(R.id.content) as TextView
        }

        override fun toString(): String {
            return super.toString() + " '" + mContentView.text + "'"
        }
    }
}
