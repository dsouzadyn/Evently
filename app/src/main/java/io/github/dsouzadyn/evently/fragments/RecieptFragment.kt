package io.github.dsouzadyn.evently.fragments

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.github.kittinunf.fuel.httpGet
import com.google.gson.Gson
import io.github.dsouzadyn.evently.R
import io.github.dsouzadyn.evently.adapters.RecieptRecyclerViewAdapter
import io.github.dsouzadyn.evently.database
import io.github.dsouzadyn.evently.models.Event
import kotlinx.android.synthetic.main.fragment_reciept_list.view.*
import net.glxn.qrgen.android.QRCode
import org.jetbrains.anko.db.classParser
import org.jetbrains.anko.db.parseList
import org.jetbrains.anko.db.select
import org.jetbrains.anko.support.v4.defaultSharedPreferences
import org.jetbrains.anko.support.v4.indeterminateProgressDialog


/**
 * A fragment representing a list of Items.
 *
 *
 * Activities containing this fragment MUST implement the [OnListFragmentInteractionListener]
 * interface.
 */
/**
 * Mandatory empty constructor for the fragment manager to instantiate the
 * fragment (e.g. upon screen orientation changes).
 */
class RecieptFragment : Fragment() {

    // TODO: Customize parameters
    private var mColumnCount = 1
    private var mUid = ""
    private var mUname = ""

    private var mListener: OnListFragmentInteractionListener? = null
    data class User(val email: String= "", val id: String = "", val roll_number: Int, val semester: Int, val username: String = "", val confirmed: Boolean) {
        class Deserializer: ResponseDeserializable<User> {
            override fun deserialize(content: String): User? = Gson().fromJson(content, User::class.java)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mColumnCount = arguments.getInt(ARG_COLUMN_COUNT)
            mUid = arguments.getString(ARG_UID)
            mUname = arguments.getString(ARG_UNAME)
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.fragment_reciept_list, container, false)
        val rv = view.recieptList
        val totalPriceView = view.totalPrice
        val qrImageView = view.qrImage
        val confirmationView = view.confirmationText
        val helloView = view.hello
        // Set the adapter
        if (rv is RecyclerView) {
            val context = rv.getContext()
            if (mColumnCount <= 1) {
                rv.layoutManager = LinearLayoutManager(context)
            } else {
                rv.layoutManager = GridLayoutManager(context, mColumnCount)
            }

            val events = context.database.use {
                select(Event.TABLE_NAME).exec { parseList(classParser<Event>()) }
            }
            var total = 0.0f
            for (i in 0 until events.size) {
                total += events[i].price
            }
            helloView.text = "Hello, $mUname"
            totalPriceView.text = "₹ $total"
            val qrBmp = QRCode.from("${getString(R.string.base_api_url)}/user/$mUid/confirm").withSize(172, 172).bitmap()
            qrImageView.setImageBitmap(qrBmp)
            val sharedPref = activity.getSharedPreferences(getString(R.string.settings_file), Context.MODE_PRIVATE)
            val token = "Bearer " + sharedPref.getString(getString(R.string.token_key), "")
            val progressDialog = indeterminateProgressDialog("Fetching status")
            progressDialog.show()
            FuelManager.instance.baseHeaders = mapOf("Authorization" to token)
            "${getString(R.string.base_api_url)}/user/$mUid/status".httpGet().responseObject(User.Deserializer()) { _, _, result ->
                val (user, error) = result
                Log.d("CONF", user?.roll_number.toString())
                if (error == null && user?.confirmed == true) {
                    confirmationView.text = "CONF"
                    val editor = sharedPref.edit()
                    editor.putString(getString(R.string.conf_key), "CONF")
                    editor.apply()
                } else if(error == null && user?.confirmed == false) {
                    confirmationView.text = "UNCF"
                    val editor = sharedPref.edit()
                    editor.putString(getString(R.string.conf_key), "")
                    editor.apply()
                } else {
                    val conf = sharedPref.getString(getString(R.string.conf_key), "")
                    if (conf != "") {
                        confirmationView.text = conf
                    }
                }
                progressDialog.dismiss()
            }

            rv.adapter = RecieptRecyclerViewAdapter(events, mListener)
        }
        return view
    }


    override fun onAttach(context: Context?) {
        super.onAttach(context)

    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments](http://developer.android.com/training/basics/fragments/communicating.html) for more information.
     */
    interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onListFragmentInteraction(item: Event)
    }
    companion object {
        // TODO: Customize parameter argument names
        private val ARG_COLUMN_COUNT = "column-count"
        private val ARG_UID = "uid"
        private val ARG_UNAME = "uname"
        // TODO: Customize parameter initialization
        fun newInstance(columnCount: Int, uid: String, uname: String): RecieptFragment {
            val fragment = RecieptFragment()
            val args = Bundle()
            args.putInt(ARG_COLUMN_COUNT, columnCount)
            args.putString(ARG_UID, uid)
            args.putString(ARG_UNAME, uname)
            fragment.arguments = args
            return fragment
        }
    }
}
