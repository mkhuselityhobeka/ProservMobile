package com.twoid.proserv_mobile


import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import com.twoid.proserv_mobile.model.DemographicRegistration
import com.twoid.proserv_mobile.serviceBuilder.ServiceBuilder
import com.twoid.proserv_mobile.services.DemographicRegistrationService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class RegistrationActivity : AppCompatActivity() {


        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
             setContentView(R.layout.activity_registration)

            var fingerprintCaptureIntent = Intent(this, FingerprintCaptureActivity::class.java)
            val register = findViewById<Button>(R.id.registerDemographics)
            var facilatorCheckBox: CheckBox = findViewById(R.id.checkbox_facilitator)
            var editTextemployeeNumber:TextInputEditText = findViewById(R.id.employeeNumber)

            var editTextestudentNumber:TextInputEditText = findViewById(R.id.student_number)

            editTextemployeeNumber.isEnabled = false

            Toast.makeText(this,"Employee number label is not active , click checkbox to enable it",Toast.LENGTH_LONG).show()

        /****
         *
         * facilitator checkbox onclick event listener
         */
            facilatorCheckBox.setOnCheckedChangeListener { compoundButton, isChecked ->

               if (isChecked){

                        Toast.makeText(this,"Student number label is not active",Toast.LENGTH_LONG).show()
                        editTextestudentNumber.isEnabled = false
                        editTextemployeeNumber.isEnabled = true

                    }

                if (!isChecked){

                    Toast.makeText(this,"Student number label is active",Toast.LENGTH_LONG).show()
                    editTextestudentNumber.isEnabled = true
                    editTextemployeeNumber.isEnabled = false
                }

            }

        /******
         *
         * register button click event validation
         */
            register.setOnClickListener {

                val demographicRegistration = DemographicRegistration()

                var editTextName:TextInputEditText = findViewById(R.id.firstName)
                var firstname = editTextName.text.toString()

                var editTextLastname:TextInputEditText = findViewById(R.id.lastName)
                var lastname = editTextLastname.text.toString()
                var employeeNumber = editTextemployeeNumber.text.toString()

                var studentNumber  = editTextestudentNumber.text.toString()

                demographicRegistration.personName = firstname
                demographicRegistration.personSurname = lastname
                demographicRegistration.studentNumber = studentNumber
                demographicRegistration.employeeNumber = employeeNumber

                val demographicRegistrationService = ServiceBuilder.buildService(DemographicRegistrationService::class.java)
                val requestCall = demographicRegistrationService.addDemographicData(demographicRegistration)

                requestCall.enqueue(object: Callback<DemographicRegistration> {

                    override fun onResponse(

                        call: Call<DemographicRegistration>,
                        response: Response<DemographicRegistration>){
                        if (response.isSuccessful){
                            finish()

                            var demographicRegistrationResponseBody = response.body()
                            println("response body " + demographicRegistrationResponseBody)

                            Toast.makeText(this@RegistrationActivity,"successfully registered demographic",Toast.LENGTH_SHORT).show()
                        }else{
                            Toast.makeText(this@RegistrationActivity,"failed to register demographics",Toast.LENGTH_SHORT).show()

                        }
                    }

                    override fun onFailure(call: Call<DemographicRegistration>, t: Throwable) {
                        Toast.makeText(this@RegistrationActivity,"failed to register demographics",Toast.LENGTH_SHORT).show()
                    }

                })




//                  if(firstname.isEmpty()) {
//                           editTextName.error = "Name cannot be empty"
//                           editTextName.requestFocus()
//
//                  }else if (lastname!!.isEmpty()) {
//                           editTextLastname.error = "Surname cannot be empty"
//                            editTextLastname.requestFocus()
//
//                  }else if (employeeNumber!!.isEmpty()) {
//                            editTextemployeeNumber!!.error = "Please enter employee number"
//                            editTextemployeeNumber!!.requestFocus()
//
//                  }else if(employeeNumber!!.length > 10){
//                            editTextemployeeNumber!!.error = "Employee number number cannot exceed 10 characters"
//                            editTextemployeeNumber!!.requestFocus()
//
//                  }else if (studentNumber!!.isEmpty()) {
//                            editTextestudentNumber!!.error = "Please enter student number"
//                            editTextestudentNumber!!.requestFocus()
//
//                  }else if (studentNumber!!.length > 10){
//
//                            editTextestudentNumber!!.error = "student number cannot exceed 10 characters"
//                            editTextestudentNumber!!.requestFocus()
//
//                  }else{
//
//                        startActivity(fingerprintCaptureIntent)
//                    }
            }
       }
}
