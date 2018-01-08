package io.github.dsouzadyn.evently

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.github.kittinunf.fuel.httpGet
import com.google.gson.Gson
import io.github.dsouzadyn.evently.fragments.DayFragment
import io.github.dsouzadyn.evently.fragments.EventFragment
import io.github.dsouzadyn.evently.models.DayContent
import io.github.dsouzadyn.evently.models.EventContent
import org.jetbrains.anko.indeterminateProgressDialog


class MainActivity : AppCompatActivity(), DayFragment.OnListFragmentInteractionListener {

    private val DAY_ONE = "1"
    private val DAY_TWO = "2"
    private val DAY_THREE = "3"
    private val MY_EVENTS = "4"

    var events : Events? = null
    var error: FuelError? = null

    data class Event(val _id: String, val name: String, val description: String, val capacity: Int,
                     val start_time: String, val end_time: String, val price: Float)

    data class Events(val data: List<Event>) {
        class Deserializer: ResponseDeserializable<Events> {
            override fun deserialize(content: String) = Gson().fromJson(content, Events::class.java)
        }
    }


    override fun onListFragmentInteraction(item: DayContent.DayItem) {
        when {
            item.id == DAY_ONE -> {
//                EventContent.addItem(
//                        EventContent.createEventItem(1, "Day 1"))
                EventContent.ITEMS.clear()
                EventContent.ITEM_MAP.clear()
                var i = 0
                if (events != null) {
                    events!!.data.filterIndexed { index, value ->
                        //val date = LocalDateTime.parse(value.start_time)
                        Log.d("DATE", value.start_time)
                        value.start_time.contains("2017-02-12")
                    }.forEach { e ->
                        Log.d("i", i.toString())
                        EventContent.addItem(
                                EventContent.createEventItem(
                                        i,
                                        e._id,
                                        e.name,
                                        e.description,
                                        e.capacity,
                                        e.start_time,
                                        e.end_time,
                                        e.price
                                ))
                    }
                    navigateToFragment(EventFragment.newInstance(1))
                }
            }
            item.id == DAY_TWO -> {
                EventContent.ITEMS.clear()
                var i = 0
                if (events != null) {
                    events!!.data.filterIndexed { index, value ->
                        //val date = LocalDateTime.parse(value.start_time)
                        Log.d("DATE", value.start_time)
                        value.start_time.contains("2017-02-13")
                    }.forEach { e ->
                        EventContent.addItem(
                                EventContent.createEventItem(
                                        i++,
                                        e._id,
                                        e.name,
                                        e.description,
                                        e.capacity,
                                        e.start_time,
                                        e.end_time,
                                        e.price
                                ))
                    }
                    navigateToFragment(EventFragment.newInstance(1))
                }
            }
            item.id == DAY_THREE -> {
                // Launch the day 3 fragment
                EventContent.ITEMS.clear()
                var i = 0
                if (events != null) {
                    events!!.data.filterIndexed { index, value ->

                        Log.d("DATE", value.start_time)
                        value.start_time.contains("2017-02-14")
                    }.forEach { e ->
                        EventContent.addItem(
                                EventContent.createEventItem(
                                        i++,
                                        e._id,
                                        e.name,
                                        e.description,
                                        e.capacity,
                                        e.start_time,
                                        e.end_time,
                                        e.price
                                ))
                    }
                    navigateToFragment(EventFragment.newInstance(1))
                }
            }
            item.id == MY_EVENTS -> {
                // TODO Launch the events fragment
            }
            else -> Log.e("ERROR", "Something went wrong")
        }
    }

    // TODO Implement logout button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val progressDialog = indeterminateProgressDialog("Fetching events...")
        val sharedPref = getSharedPreferences(getString(R.string.settings_file), Context.MODE_PRIVATE)
        val token = sharedPref.getString(getString(R.string.token_key), "")
        Log.d("MAIN_ACTIVITY", token)
        if (token == "") {
            val loginIntent = Intent(this, LoginActivity::class.java)
            startActivity(loginIntent)
        } else {
            FuelManager.instance.baseHeaders = mapOf("Authorization" to token)
            progressDialog.show()
            "http://192.168.1.7:3000/api/events".httpGet().responseObject(Events.Deserializer()) { _, _, result ->
                events = result.component1()
                error = result.component2()
                if(error == null) {
                    progressDialog.dismiss()
                    for (event in events!!.data) {
                        Log.d("EVENT", event.name)
                    }
                } else {
                    progressDialog.dismiss()
                    Log.e("ERROR", error!!.message)
                }
            }
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragmentContainer, DayFragment.newInstance(1))
            transaction.commit()
        }
    }

    fun navigateToFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragmentContainer, fragment).addToBackStack("events")
        transaction.commit()
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount == 0) {
            super.onBackPressed()
        } else {
            supportFragmentManager.popBackStack()
        }

    }
}
