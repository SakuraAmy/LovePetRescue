package com.example.lovepetrescue

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment

class About : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(activity)
            .setTitle("Love Pet Rescue")
            .setMessage("Hello! Welcome to Love Pet Rescue!\n\n" +
                        "This is a mobile app where a user can put their pet up for adoption or a user can event adopt a pet!\n\n" +
                        "Since 1991, this business has rocketed for adopting pets because many companies and corporations " +
                        "have not set up businesses in some locations. Love Pet Rescue have received a total of 12 county awards, 3 city awards, and a national nomination. " +
                        " This business is a shelter which brings no " +
                        "harm or any toxicity to our pets. Our pets are very healthy and very energetic.\n\n" +
                        "Please rescue our animals and our animals would like to meet you!\n" +
                        "\n\nOwned business and app by Amina Mahmood")
            .create()
    }
}