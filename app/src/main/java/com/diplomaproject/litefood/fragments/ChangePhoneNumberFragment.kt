package com.diplomaproject.litefood.fragments

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.databinding.Observable
import androidx.fragment.app.Fragment
import com.diplomaproject.litefood.R
import com.diplomaproject.litefood.VerificationTimerService
import com.diplomaproject.litefood.databinding.FragmentChangePhoneNumberBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import ru.tinkoff.decoro.MaskImpl
import ru.tinkoff.decoro.parser.UnderscoreDigitSlotsParser
import ru.tinkoff.decoro.watchers.FormatWatcher
import ru.tinkoff.decoro.watchers.MaskFormatWatcher
import java.util.concurrent.TimeUnit

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class ChangePhoneNumberFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var binding: FragmentChangePhoneNumberBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var phoneNumberInputLayout: TextInputLayout
    private lateinit var phoneNumberInputEditText: TextInputEditText
    private lateinit var tvPolicy: TextView
    private lateinit var btNext: Button
    private lateinit var sentVerificationCodService: VerificationTimerService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        setViewListeners()
        setMask()
        setupUserAgreementLink()
        setTextChangeListenerOnPhoneInput()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChangePhoneNumberBinding.inflate(inflater, container, false);
        return binding.root
    }

    private fun init() {
        firebaseAuth = FirebaseAuth.getInstance()
        tvPolicy = binding.tvPolicy
        btNext = binding.btNext
        phoneNumberInputLayout = binding.textInputLayout
        phoneNumberInputEditText = binding.etPhoneNumber
        sentVerificationCodService = VerificationTimerService()
    }

    private fun setViewListeners() {
        btNext.setOnClickListener {
            sendVerificationCode()
        }
    }

    private fun setTextChangeListenerOnPhoneInput() {
        phoneNumberInputEditText.addTextChangedListener(object : TextWatcher {
            var textBefore: String? = null
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                textBefore = s.toString()
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                if (s != null) {
                    if (textBefore?.length!! != s.length) {
                        phoneNumberInputLayout.error = null
                    }
                }
            }

        })
    }

    private fun sendVerificationCode() {
        val phoneNumber = phoneNumberInputEditText.text.toString()
        val phoneNumberFormatted = phoneNumber.replace(Regex("[^0-9+]"), "")

        val currentUser = FirebaseAuth.getInstance().currentUser
        val currentUserPhoneNumber = currentUser?.phoneNumber

        if (phoneNumber.isEmpty()) {
            phoneNumberInputLayout.error = "Введите номер телефона"
        } else {
            if (phoneNumberFormatted.length == 13) {
                if (phoneNumberFormatted != currentUserPhoneNumber) {
                    val operatorCode = phoneNumberFormatted.substring(4, 6)
                    val validCodeOperators = arrayListOf("25", "33", "44", "29")
                    if (validCodeOperators.contains(operatorCode)) {
//                        if(isPhoneNumberAuthorized(phoneNumber)){
//                            phoneNumberInputLayout.error="Аккаунт с таким номером уже существует"
//                        }
                        //    else {
                        if (VerificationTimerService.isServiceRunning) {
                            VerificationTimerService.leftTimeInSeconds.addOnPropertyChangedCallback(
                                object : Observable.OnPropertyChangedCallback() {
                                    override fun onPropertyChanged(
                                        sender: Observable?,
                                        propertyId: Int
                                    ) {
                                        val seconds =
                                            VerificationTimerService.leftTimeInSeconds.get()
                                                ?: return
                                        phoneNumberInputLayout.error =
                                            "Не прошло время между отправками SMS: " + seconds
                                        if (seconds.toInt() == 0) {
                                            phoneNumberInputLayout.error = null
                                        }
                                    }
                                })
                        } else {
                            val progressDialog = ProgressDialog(activity)
                            progressDialog.setMessage("Пожалуйста, подождите...")
                            progressDialog.setCanceledOnTouchOutside(false)
                            progressDialog.setCancelable(false)
                            progressDialog.show()


                            val phoneAuthOptions = activity?.let {
                                PhoneAuthOptions.newBuilder(firebaseAuth)
                                    .setPhoneNumber(phoneNumberFormatted)
                                    .setTimeout(60L, TimeUnit.SECONDS)
                                    .setActivity(it)
                                    .setCallbacks(object :
                                        PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                                        override fun onVerificationCompleted(p0: PhoneAuthCredential) {
                                        }

                                        override fun onVerificationFailed(p0: FirebaseException) {
                                            Toast.makeText(
                                                activity,
                                                "Ошибка", Toast.LENGTH_SHORT
                                            ).show()
                                            progressDialog.dismiss()
                                        }

                                        @RequiresApi(Build.VERSION_CODES.O)
                                        override fun onCodeSent(
                                            p0: String,
                                            p1: PhoneAuthProvider.ForceResendingToken
                                        ) {
                                            val fragmentManager = getParentFragmentManager();
                                            val newPhoneNumberVerificationCodeFragment =
                                                NewPhoneNumberVerificationCodeFragment()

                                            val bundle = Bundle().apply {
                                                putString("VerificationCode", p0)
                                                putString("PhoneNumber", phoneNumber)
                                            }
                                            newPhoneNumberVerificationCodeFragment.arguments =
                                                bundle

                                            fragmentManager.beginTransaction().replace(
                                                R.id.fragment_container_change_phone_number,
                                                newPhoneNumberVerificationCodeFragment
                                            )
                                                .addToBackStack(null)
                                                .commit()
                                            progressDialog.dismiss()


                                            Intent(
                                                requireActivity(),
                                                VerificationTimerService::class.java
                                            ).also {
                                                requireActivity().startService(it)
                                            }
                                        }
                                    })
                            }

                            if (phoneAuthOptions != null) {
                                PhoneAuthProvider.verifyPhoneNumber(phoneAuthOptions.build())
                            }

                        }
                        // }
                    } else {
                        phoneNumberInputLayout.error = "Такого мобильного оператора нет"
                    }
                } else {
                    phoneNumberInputLayout.error = "Этот номер уже подтвержден"
                }
            } else {
                phoneNumberInputLayout.error = "Неверный формат номера телефона"
            }
        }
    }

    private fun isPhoneNumberAuthorized(phoneNumber: String): Boolean {
        val databaseReference = FirebaseDatabase.getInstance().getReference("Users")
        var phoneExists = false
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (userSnapshot in dataSnapshot.children) {
                    val phone = userSnapshot.child("phoneNumber").getValue(String::class.java)
                    if (phone == phoneNumber) {
                        phoneExists = true
                        break
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                println("Ошибка при доступе к базе данных: ${databaseError.message}")
            }
        })
        return phoneExists
    }

    private fun setupUserAgreementLink() {
        val userAgreement = resources.getString(R.string.change_phone_number_policy)
        var spannableString = SpannableString(userAgreement)

        val startConditions = userAgreement.indexOf("Условиями использования")
        val endConditions = startConditions + "Условиями использования".length

        val linkColor = resources.getColor(R.color.golden_yellow)

        val termsOfUserClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data =
                        Uri.parse("https://doc-hosting.flycricket.io/litefood-terms-of-use/cfe0a265-56e1-45e0-b4b4-cca77ded0baf/terms")
                }
                startActivity(intent)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = true
                ds.color = linkColor
            }
        }

        spannableString.setSpan(
            termsOfUserClickableSpan,
            startConditions,
            endConditions,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        val startPolicy = userAgreement.indexOf("Политикой конфиденциальности")
        val endPolicy = startPolicy + "Политикой конфиденциальности".length

        val policyClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://doc-hosting.flycricket.io/litefood-privacy-policy/4f282f4c-f9c2-4cc9-876d-15e10045ba61/privacy")
                )
                startActivity(intent)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = linkColor
            }
        }

        spannableString.setSpan(
            policyClickableSpan,
            startPolicy,
            endPolicy,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        tvPolicy.text = spannableString
        tvPolicy.movementMethod = LinkMovementMethod()
    }

    private fun setMask() {
        val slots = UnderscoreDigitSlotsParser().parseSlots("+375 (__) ___-__-__")
        val formatWatcher: FormatWatcher = MaskFormatWatcher(
            MaskImpl.createTerminated(slots)
        )
        formatWatcher.installOn(phoneNumberInputEditText)
    }


    companion object {
        fun newInstance(param1: String, param2: String) =
            ChangePhoneNumberFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}