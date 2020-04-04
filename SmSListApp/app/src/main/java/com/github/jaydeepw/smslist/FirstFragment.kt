package com.github.jaydeepw.smslist

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Telephony
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.app.ActivityCompat
import androidx.navigation.fragment.findNavController
import java.util.*
import kotlin.collections.ArrayList

class Conversation(val number: String, val message: List<Message>)
class Message(val number: String, val body: String, val date: Date)

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    companion object {
        const val PERMISSIONS_REQUEST_READ_SMS = 100
        const val TAG = "FirstFragment"
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.button_first).setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }

        requestPermission()
    }

    fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            ActivityCompat.checkSelfPermission(context!!,
                Manifest.permission.READ_SMS
            ) != PackageManager.PERMISSION_GRANTED
        ) { // Needs permission

            requestPermissions(arrayOf(Manifest.permission.READ_SMS),
                PERMISSIONS_REQUEST_READ_SMS
            )

        } else { // Permission has already been granted
            getSmsConversation(activity!!){ conversations ->
                conversations?.forEach { conversation ->
                    println("Number: ${conversation.number}")
                    if (conversation.message.isNotEmpty()) {
                        println("Message One: ${conversation.message[0].body}")
                    }

                    if (conversation.message.size > 1) {
                        println("Message Two: ${conversation.message[1].body}")
                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {

            PERMISSIONS_REQUEST_READ_SMS -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "PERMISSIONS_REQUEST_READ_SMS Granted")
                } else {
                    // toast("Permission must be granted  ")
                    Log.i(TAG, "PERMISSIONS_REQUEST_READ_SMS NOT Granted")
                }
            }
        }
    }

    private fun getSmsConversation(context: Context, number: String? = null, completion: (conversations: List<Conversation>?) -> Unit) {
        val cursor = context.contentResolver.query(Telephony.Sms.CONTENT_URI, null, null, null, null)

        val numbers = ArrayList<String>()
        val messages = ArrayList<Message>()
        var results = ArrayList<Conversation>()

        while (cursor != null && cursor.moveToNext()) {
            val smsDate = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.DATE))
            val number = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS))
            val body = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.BODY))

            numbers.add(number)
            messages.add(Message(number, body, Date(smsDate.toLong())))
        }

        cursor?.close()

        numbers.forEach { number ->
            if (results.find { it.number == number } == null) {
                val msg = messages.filter { it.number == number }
                results.add(Conversation(number = number, message = msg))
            }
        }

        if (number != null) {
            results = results.filter { it.number == number } as ArrayList<Conversation>
        }

        Log.d(TAG, "results.size: ${results.size}")
        completion(results)
    }
}
