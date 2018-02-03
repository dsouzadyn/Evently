package io.github.dsouzadyn.evently

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import io.github.dsouzadyn.evently.fragments.RecieptFragment
import io.github.dsouzadyn.evently.models.DayContent
import io.github.dsouzadyn.evently.models.EventContent
import org.jetbrains.anko.indeterminateProgressDialog


class MainActivity : AppCompatActivity(), DayFragment.OnListFragmentInteractionListener, HomeFragment.OnFragmentInteractionListener {

    override fun onFragmentInteraction(uri: Uri) {

    }

    private val DAY_ONE = "1"
    private val DAY_TWO = "2"
    private val DAY_THREE = "3"
    private val MY_EVENTS = "4"
    private val SIGNIN_OK = 420

    var events : List<Event>? = null
    var error: FuelError? = null


    data class Event(val id: String, val name: String, val description: String, val capacity: Int,
                     val start_time: String, val end_time: String, val price: Float) {
        class Deserializer: ResponseDeserializable<List<Event>> {
            override fun deserialize(content: String): List<Event>? = Gson().fromJson(content, Array<Event>::class.java).toList()
        }
    }




    override fun onListFragmentInteraction(item: DayContent.DayItem) {
        val sharedPref = getSharedPreferences(getString(R.string.settings_file), Context.MODE_PRIVATE)
        val uid = sharedPref.getString(getString(R.string.uid_key), "")
        when {
            item.id == DAY_ONE -> {
//                EventContent.addItem(
//                        EventContent.createEventItem(1, "Day 1"))
                EventContent.ITEMS.clear()
                EventContent.ITEM_MAP.clear()
                var i = 0
                if (events != null) {
                    events?.filterIndexed { index, value ->
                        //val date = LocalDateTime.parse(value.start_time)
                        Log.d("DATE", value.start_time)
                        value.start_time.contains("2018-02-22")
                    }?.forEach { e ->
                        Log.d("i", i.toString())
                        EventContent.addItem(
                                EventContent.createEventItem(
                                        i,
                                        e.id,
                                        e.name,
                                        e.description,
                                        e.capacity,
                                        e.start_time,
                                        e.end_time,
                                        e.price
                                ))
                    }
                    if(EventContent.ITEMS.size > 0)
                        navigateToFragment(EventFragment.newInstance(1, uid))
                }
            }
            item.id == DAY_TWO -> {
                EventContent.ITEMS.clear()
                var i = 0
                if (events != null) {
                    events?.filterIndexed { index, value ->
                        //val date = LocalDateTime.parse(value.start_time)
                        Log.d("DATE", value.start_time)
                        value.start_time.contains("2018-02-23")
                    }?.forEach { e ->
                        EventContent.addItem(
                                EventContent.createEventItem(
                                        i++,
                                        e.id,
                                        e.name,
                                        e.description,
                                        e.capacity,
                                        e.start_time,
                                        e.end_time,
                                        e.price
                                ))
                    }
                    if(EventContent.ITEMS.size > 0)
                        navigateToFragment(EventFragment.newInstance(1, uid))
                }
            }
            item.id == DAY_THREE -> {
                // Launch the day 3 fragment
                EventContent.ITEMS.clear()
                var i = 0
                if (events != null) {
                    events?.filterIndexed { index, value ->

                        Log.d("DATE", value.start_time)
                        value.start_time.contains("2018-02-24")
                    }?.forEach { e ->
                        EventContent.addItem(
                                EventContent.createEventItem(
                                        i++,
                                        e.id,
                                        e.name,
                                        e.description,
                                        e.capacity,
                                        e.start_time,
                                        e.end_time,
                                        e.price
                                ))
                    }
                    if (EventContent.ITEMS.size > 0)
                        navigateToFragment(EventFragment.newInstance(1, uid))
                }
            }
            item.id == MY_EVENTS -> {
                // TODO Launch the my events activity
                navigateToFragment(RecieptFragment.newInstance(1, uid))
            }
            else -> Log.e("ERROR", "Something went wrong")
        }
    }

    // TODO Implement logout button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sharedPref = getSharedPreferences(getString(R.string.settings_file), Context.MODE_PRIVATE)
        val token = "Bearer " + sharedPref.getString(getString(R.string.token_key), "")

        Log.d("MAIN_ACTIVITY", token)
        if (token == "Bearer ") {
            val loginIntent = Intent(this, LoginActivity::class.java)
            startActivityForResult(loginIntent, SIGNIN_OK)
        } else {
            val progressDialog = indeterminateProgressDialog("Fetching events...")
            FuelManager.instance.baseHeaders = mapOf("Authorization" to token)
            progressDialog.show()
            "http://192.168.1.6:1337/event".httpGet().responseObject(Event.Deserializer()) { _, _, result ->
                events = result.component1()
                error = result.component2()
                if(error == null) {
                    progressDialog.dismiss()
                    if(events != null) {
                        for (event in events!!) {
                            Log.d("EVENT", event.name)
                        }
                    }
                } else {
                    progressDialog.dismiss()
                    Log.e("ERROR", error!!.message)
                }
            }
            val transaction = supportFragmentManager.beginTransaction()
            transaction.setCustomAnimations(R.anim.abc_grow_fade_in_from_bottom, R.anim.abc_shrink_fade_out_from_bottom)
            transaction.replace(R.id.fragmentContainer, HomeFragment.newInstance("",""))
            transaction.commit()
        }
    }



    override fun onResume() {
        super.onResume()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == SIGNIN_OK) {
            val progressDialog = indeterminateProgressDialog("Fetching events...")
            val sharedPref = getSharedPreferences(getString(R.string.settings_file), Context.MODE_PRIVATE)
            val token = "Bearer " + sharedPref.getString(getString(R.string.token_key), "")
            FuelManager.instance.baseHeaders = mapOf("Authorization" to token)
            progressDialog.show()
            "http://192.168.1.6:1337/event".httpGet().responseObject(Event.Deserializer()) { _, _, result ->
                events = result.component1()
                error = result.component2()
                if(error == null) {
                    progressDialog.dismiss()
                    if(events != null) {
                        for (event in events!!) {
                            Log.d("EVENT", event.name)
                        }
                    }
                } else {
                    progressDialog.dismiss()
                    Log.e("ERROR", error!!.message)
                }
            }
            val transaction = supportFragmentManager.beginTransaction()
            transaction.setCustomAnimations(R.anim.abc_grow_fade_in_from_bottom, R.anim.abc_shrink_fade_out_from_bottom)
            transaction.replace(R.id.fragmentContainer, HomeFragment.newInstance("",""))
            transaction.commit()
        }
    }

    private fun navigateToFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.setCustomAnimations(R.anim.abc_grow_fade_in_from_bottom, R.anim.abc_shrink_fade_out_from_bottom)
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
