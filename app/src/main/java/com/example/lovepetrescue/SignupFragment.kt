package com.example.lovepetrescue

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.media.Image
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_signup.*
//import models.SignUpUser
import java.util.*

class SignupFragment: Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_signup, container, false)

        val selectPhoto = view.findViewById<Button>(R.id.selectPhotoButton)

        val name = view.findViewById<EditText>(R.id.signup_fullname_field)
        val nameEmpty = view.findViewById<TextView>(R.id.signup_no_name)

        val email = view.findViewById<EditText>(R.id.signup_email_adress_field)
        val emailEmpty = view.findViewById<TextView>(R.id.signup_no_email)

        val passwordFirst = view.findViewById<EditText>(R.id.signup_password_field)
        val passwordFirstEmpty = view.findViewById<TextView>(R.id.signup_no_password_first)

        val passwordSecond = view.findViewById<EditText>(R.id.signup_password_confirmation_field)
        val passwordSecondEmpty = view.findViewById<TextView>(R.id.signup_no_password_second)

        val login = view.findViewById<Button>(R.id.signup_create_account_click)

        login.setOnClickListener {
            requireFragmentManager()
                .beginTransaction()
                .replace(R.id.auth_fragmentContainer, LoginFragment.newInstance())
                .commit()
        }

        selectPhoto.setOnClickListener {
            Log.d("MainActivity", "Try to show photo selector")

            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }

        nameEmpty.visibility = View.INVISIBLE
        emailEmpty.visibility = View.INVISIBLE
        passwordFirstEmpty.visibility = View.INVISIBLE
        passwordSecondEmpty.visibility = View.INVISIBLE
        view.findViewById<Button>(R.id.signup_validation_button).setOnClickListener{
            if(name.text.isEmpty()){
                nameEmpty.visibility = View.VISIBLE
            }
            if(email.text.isEmpty()){
                emailEmpty.visibility = View.VISIBLE
            }
            if(passwordFirst.text.isEmpty()){
                passwordFirstEmpty.visibility = View.VISIBLE
            }
            if(passwordSecond.text.isEmpty()){
                passwordSecondEmpty.visibility = View.VISIBLE
            }
            else if(!email.text.toString().isEmailValid())
            {
                AlertDialog.Builder(context)
                    .setTitle("Invalid Email")
                    .setMessage("Please try again with your email address. Could you check it again? ")

                    // Specifying a listener allows you to take an action before dismissing the dialog.
                    // The dialog is automatically dismissed when a dialog button is clicked.
                    .setPositiveButton(R.string.alert_dialog_validation_fun) { dialog, which ->
                        // Continue with delete operation
                    }

                    // A null listener allows the button to dismiss the dialog and take no further action.
                    .show()
            }
            else if(passwordFirst.text.toString()!=passwordSecond.text.toString())
            {
                AlertDialog.Builder(context)
                    .setTitle("Passwords don't match")
                    .setMessage("You didn't only drink water, don't you? Could you type your passwords again?")

                    // Specifying a listener allows you to take an action before dismissing the dialog.
                    // The dialog is automatically dismissed when a dialog button is clicked.
                    .setPositiveButton(R.string.alert_dialog_validation_fun) { dialog, which ->
                        // Continue with delete operation
                    }

                    // A null listener allows the button to dismiss the dialog and take no further action.
                    .show()
            }
            else
            {
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(email.text.toString(), passwordFirst.text.toString())
                    .addOnCompleteListener {
                         if (!it.isSuccessful) return@addOnCompleteListener

                         //else if successful
                        Log.d("MainActivity", "Successfully created user with uid: ${it.result?.user?.uid}")

                        uploadImageToFirebaseStorage()
                        val intent = Intent(activity, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        //saveToFirebaseDatabase()
                        activity?.finish()
                    }
                    .addOnFailureListener {
                        Log.d("MainActivity", "Failed to create user: ${it.message}")
                        //Toast.makeText(this, "Failed to create user: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
        return view
    }

    fun String.isEmailValid(): Boolean {
        return !TextUtils.isEmpty(this) && android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
    }

    var selectedPhotoUri : Uri? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            // proceed and check what the selected image was....
            Log.d("MainActivity", "Photo was selected")

            selectedPhotoUri = data.data
            
            val bitmap = MediaStore.Images.Media.getBitmap(context?.contentResolver, selectedPhotoUri)

            profileCircleImageView.setImageBitmap(bitmap)

            selectPhotoButton.alpha = 0f
//            val bitmapDrawable = BitmapDrawable(bitmap)
//            selectPhotoButton.setBackgroundDrawable(bitmapDrawable)
        }
    }

    private fun uploadImageToFirebaseStorage() {
        if(selectedPhotoUri == null) return

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener { it ->
                Log.d("MainActivity", "Successfully uploaded image: ${it.metadata?.path}")

                ref.downloadUrl.addOnSuccessListener {
                    it.toString()
                    Log.d("MainActivity", "File Location: $it")

                    saveUserToFirebaseDatabase(it.toString())
                }
            }
            .addOnFailureListener {

            }
    }

    //@SuppressLint("LongLogTag")
    private fun saveUserToFirebaseDatabase(profileImageUrl: String) {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        //var username = signup_fullname_field.text.toString()
        val user = User(uid, signup_fullname_field.text.toString(), profileImageUrl)

        ref.setValue(user)
            .addOnSuccessListener {
                Log.d("MainActivity", "Finally we saved the user to Firebase Database")

//                val intent = Intent(activity, MainActivity::class.java)
//                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
//                startActivity(intent)
            }
    }

    companion object {
        fun newInstance(): SignupFragment {
            return SignupFragment()
        }
    }
}