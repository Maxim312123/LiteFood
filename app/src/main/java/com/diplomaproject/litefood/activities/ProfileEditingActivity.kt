package com.diplomaproject.litefood.activities

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.diplomaproject.litefood.R
import com.diplomaproject.litefood.UserViewModel
import com.diplomaproject.litefood.data.User
import com.diplomaproject.litefood.databinding.ActivityProfileEditingBinding
import com.diplomaproject.litefood.dialogs.ChooseGenderDialog
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.Calendar


class ProfileEditingActivity : AppCompatActivity(), ChooseGenderDialog.OnGenderSelectedListener {

    private lateinit var binding: ActivityProfileEditingBinding
    private lateinit var toolbar: MaterialToolbar
    private lateinit var tvName: TextView
    private lateinit var tvNameError: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvEmailError: TextView
    private lateinit var etName: EditText
    private lateinit var etPhoneNumber: EditText
    private lateinit var etDateOfBirth: EditText
    private lateinit var etGender: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPaymentMethod: EditText
    private lateinit var databaseReference: DatabaseReference
    private lateinit var currentUser: FirebaseUser
    private lateinit var userViewModel: UserViewModel;

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileEditingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        setUpToolbar()
        setupViewsListeners()
        bindUserDataToUI()
        clearDateOfBirth()
        setNameTextChangedListener()
        setEmailTextChangedListener()
        clearGender()
        setEmailImeOptionsListener()
    }

    private fun init() {
        toolbar = binding.toolbar

        tvName = binding.tvName
        tvNameError = binding.tvNameError
        etName = binding.etName

        tvEmail = binding.tvEmail
        tvEmailError = binding.tvEmailError
        etEmail = binding.etEmail

        etPhoneNumber = binding.etPhoneNumber
        etDateOfBirth = binding.etDateOfBirth
        etGender = binding.etGender
        etPaymentMethod = binding.etPaymentMethod
        currentUser = FirebaseAuth.getInstance().currentUser!!
        databaseReference = FirebaseDatabase.getInstance().getReference("Users/" + currentUser.uid)
    }

    private fun setUpToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    private fun setupViewsListeners() {
        etPhoneNumber.setOnClickListener {
            val intent = Intent(this, ChangePhoneNumberActivity::class.java)
            startActivity(intent)
        }
        etDateOfBirth.setOnClickListener {
            showDatePickerDialog()
        }
        etGender.setOnClickListener {
            val chooseGenderDialog = ChooseGenderDialog()
            chooseGenderDialog.show(getSupportFragmentManager(), "ChooseGenderDialog")
        }
        etPaymentMethod.setOnClickListener {
            val intent = Intent(this, PaymentMethodsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun bindUserDataToUI() {
        val drawable = ContextCompat.getDrawable(this, R.drawable.remove)
        userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)
        userViewModel.user.observe(this) { user: User ->
            user.name?.let { name -> etName.setText(name) }
            etPhoneNumber.setText(user.phoneNumber)

            user.dateOfBirth?.let { dateOfBirth ->
                etDateOfBirth.setText(dateOfBirth)
                etDateOfBirth.setCompoundDrawablesWithIntrinsicBounds(
                    null, null, drawable, null
                )
            }
            user.gender?.let { gender ->
                etGender.setText(gender)
                etGender.setCompoundDrawablesWithIntrinsicBounds(
                    null, null, drawable, null
                )
            }

            user.email?.let { email ->
                etEmail.setText(email)
            }

            user.paymentMethod?.let { paymentMethod ->
                val mainCard = paymentMethod.get("main")
                val last4Digits = mainCard?.getValue("last4Digits")
                val cardBrand = mainCard?.getValue("cardBrand")

                etPaymentMethod.setText("**** " + last4Digits)

                if (cardBrand != null) {
                    if (cardBrand.equals("Visa")) {
                        etPaymentMethod.setCompoundDrawablesWithIntrinsicBounds(
                            resources.getDrawable(R.drawable.visa24),
                            null,
                            null,
                            null
                        )
                    } else if (cardBrand.equals("MasterCard")) {
                        etPaymentMethod.setCompoundDrawablesWithIntrinsicBounds(
                            resources.getDrawable(R.drawable.mastercard24),
                            null,
                            null,
                            null
                        )
                    }
                }

            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun clearDateOfBirth() {
        etDateOfBirth.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val x = event.x.toInt()
                val y = event.y.toInt()

                val drawableEnd = etDateOfBirth.compoundDrawables[2]
                if (drawableEnd != null) {
                    val drawableWidth = drawableEnd.bounds.width()
                    val drawableHeight = drawableEnd.bounds.height()

                    val drawableLeft =
                        etDateOfBirth.width - drawableWidth - etDateOfBirth.paddingRight
                    val drawableRight = etDateOfBirth.width - etDateOfBirth.paddingRight
                    val drawableTop = (etDateOfBirth.height - drawableHeight) / 2
                    val drawableBottom = drawableTop + drawableHeight

                    if (x in drawableLeft..drawableRight && y in drawableTop..drawableBottom) {
                        etDateOfBirth.text.clear()
                        etDateOfBirth.setCompoundDrawablesWithIntrinsicBounds(
                            null, null, null, null
                        )
                        return@setOnTouchListener true
                    }
                }
            }
            return@setOnTouchListener false
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun clearGender() {
        etGender.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val x = event.x.toInt()
                val y = event.y.toInt()

                val drawableEnd = etGender.compoundDrawables[2]
                if (drawableEnd != null) {
                    val drawableWidth = drawableEnd.bounds.width()
                    val drawableHeight = drawableEnd.bounds.height()

                    val drawableLeft = etGender.width - drawableWidth - etGender.paddingRight
                    val drawableRight = etGender.width - etGender.paddingRight
                    val drawableTop = (etGender.height - drawableHeight) / 2
                    val drawableBottom = drawableTop + drawableHeight

                    if (x in drawableLeft..drawableRight && y in drawableTop..drawableBottom) {
                        etGender.text.clear()
                        etGender.setCompoundDrawablesWithIntrinsicBounds(
                            null, null, null, null
                        )
                        return@setOnTouchListener true
                    }
                }
            }
            return@setOnTouchListener false
        }
    }

    private fun setEmailImeOptionsListener() {
        etEmail.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(etEmail.windowToken, 0)
                true
            } else {
                false
            }
        }
    }

    private fun setNameTextChangedListener() {
        etName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            @SuppressLint("ResourceAsColor")
            override fun afterTextChanged(s: Editable?) {
                val name = s.toString()
                if (!isValidName(name)) {
                    tvNameError.visibility = View.VISIBLE
                    tvName.setTextColor(resources.getColor(R.color.colorPrimary))
                    etName.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
                } else {
                    tvNameError.visibility = View.GONE
                    tvName.setTextColor(resources.getColor(R.color.black))
                    etName.setBackgroundTintList(ColorStateList.valueOf(Color.BLACK));
                }
            }
        })
    }

    private fun setEmailTextChangedListener() {
        etEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            @SuppressLint("ResourceAsColor")
            override fun afterTextChanged(s: Editable?) {
                val email = s.toString()
                if (!isValidEmail(email)) {
                    tvEmailError.visibility = View.VISIBLE
                    tvEmail.setTextColor(resources.getColor(R.color.colorPrimary))
                    etEmail.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
                }
                if (isValidEmail(email) || email.isEmpty()) {
                    tvEmailError.visibility = View.GONE
                    tvEmail.setTextColor(resources.getColor(R.color.black))
                    etEmail.setBackgroundTintList(ColorStateList.valueOf(Color.BLACK));
                }
            }
        })
    }

    private fun isValidName(phoneNumber: String): Boolean {
        val validNamePattern = Regex("^[A-Za-zА-Яа-яЁё]*$")
        return validNamePattern.matches(phoneNumber)
    }

    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this, R.style.Theme_LiteFood_DatePickerDialog, { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate = formatDate(selectedDay, selectedMonth, selectedYear)
                etDateOfBirth.setText(formattedDate)
                val drawable = ContextCompat.getDrawable(this, R.drawable.remove)
                etDateOfBirth.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null)
            }, year, month, day
        )
        datePickerDialog.show()
        val positiveButton = datePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE)
        positiveButton.setTextColor(getResources().getColor(R.color.black))
        val negativeButton = datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE)
        negativeButton.setTextColor(getResources().getColor(R.color.black))
    }

    private fun formatDate(day: Int, month: Int, year: Int): String {
        val formattedDay = if (day < 10) day.toString().padStart(2, '0') else day.toString()
        val formattedMonth =
            if (month + 1 < 10) (month + 1).toString().padStart(2, '0') else (month + 1).toString()
        val formattedYear = year.toString()
        val formattedDate = "$formattedDay/$formattedMonth/$formattedYear"
        return formattedDate
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_profile_editing_activity, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }

            R.id.save_changes -> {
                if (!isHasErrors()) {
                    saveUserData()
                    finish()
                    Toast.makeText(this, "Изменения сохранены", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Ошибка сохранения", Toast.LENGTH_SHORT).show()
                }
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun isHasErrors(): Boolean {
        var hasError = false
        if (tvNameError.isVisible) {
            hasError = true
        }
        if (tvEmailError.isVisible) {
            hasError = true
        }
        return hasError
    }

    private fun saveUserData() {
        userViewModel.user.observe(this) { user: User ->
            val updates = mutableMapOf<String, Any?>()

            collectUpdate(etName, updates, "/name")
            collectUpdate(etDateOfBirth, updates, "/dateOfBirth")
            collectUpdate(etGender, updates, "/gender")
            collectUpdate(etEmail, updates, "/email")

            if (etPaymentMethod.text.isEmpty()) {
                collectUpdate(etPaymentMethod, updates, "/paymentMethod")
            }


            databaseReference.updateChildren(updates).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("Database", "Successfully updated values in the database")
                } else {
                    Log.e("Database", "Failed to update values in the database", task.exception)
                }
            }
        }
    }

    private fun collectUpdate(et: EditText, updates: MutableMap<String, Any?>, dbPath: String) {
        val value = et.text.toString()
        updates[dbPath] = if (value.isNotEmpty()) value else null
    }

    override fun onGenderSelected(gender: String?) {
        val drawable = ContextCompat.getDrawable(this, R.drawable.remove)
        etGender.setText(gender)
        etGender.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null)
    }
}