package com.diplomaproject.litefood.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.Observable
import androidx.fragment.app.Fragment
import com.diplomaproject.litefood.R
import com.diplomaproject.litefood.VerificationTimerService
import com.diplomaproject.litefood.databinding.FragmentNewPhoneNumberVerificationCodeBinding
import com.diplomaproject.litefood.dialogs.SuccessLoginDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.FirebaseDatabase
import ru.tinkoff.decoro.MaskImpl
import ru.tinkoff.decoro.parser.UnderscoreDigitSlotsParser
import ru.tinkoff.decoro.watchers.MaskFormatWatcher

class NewPhoneNumberVerificationCodeFragment : Fragment() {
    private lateinit var binding: FragmentNewPhoneNumberVerificationCodeBinding
    private lateinit var tvIsInvalidPhoneNumber: TextView
    private lateinit var btnConfirm: Button
    private lateinit var etVerificationCode: EditText
    private lateinit var auth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser
    private lateinit var tvInvalidVerificationCode: TextView
    private lateinit var tvSendVerificationCodeAgain: TextView
    private var propertyChangedCallback: Observable.OnPropertyChangedCallback? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNewPhoneNumberVerificationCodeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        initViews()
        setViewsListeners()
        verificationCodeTextMask()
        verificationCodeTextChangeListener()

        etVerificationCode.setOnFocusChangeListener(object : View.OnFocusChangeListener {
            override fun onFocusChange(v: View?, hasFocus: Boolean) {
                if (hasFocus) {
                    tvInvalidVerificationCode.visibility = View.GONE
                    etVerificationCode.setBackgroundResource(R.drawable.edit_text_new_phone_number_verification_code)
                }
            }

        })

        observeLeftTimeOfSentVerificationCode()
    }

    private fun observeLeftTimeOfSentVerificationCode() {
        propertyChangedCallback?.let {
            VerificationTimerService.leftTimeInSeconds.removeOnPropertyChangedCallback(it)
        }
        propertyChangedCallback = object : Observable.OnPropertyChangedCallback() {
            @SuppressLint("ResourceAsColor", "SetTextI18n")
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                val seconds = VerificationTimerService.leftTimeInSeconds.get() ?: return
                tvSendVerificationCodeAgain.text =
                    getString(R.string.send_verification_code_again_in) + " " + seconds.toString()
                if (seconds.toInt() == 0) {
                    tvSendVerificationCodeAgain.isEnabled = true
                    tvSendVerificationCodeAgain.text =
                        getString(R.string.send_verification_code_again)
                    tvSendVerificationCodeAgain.setTextColor(
                        ContextCompat.getColor(
                            requireActivity(),
                            R.color.golden_yellow
                        )
                    )
                }
            }
        }
        VerificationTimerService.leftTimeInSeconds.addOnPropertyChangedCallback(
            propertyChangedCallback as Observable.OnPropertyChangedCallback
        )
    }

    private fun init() {
        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser!!
        tvSendVerificationCodeAgain = binding.tvSendVerificationCodeAgain
    }

    private fun initViews() {
        tvIsInvalidPhoneNumber = binding.tvIsInvalidPhoneNumber
        btnConfirm = binding.btnConfirm
        etVerificationCode = binding.etVerificationCode
        tvInvalidVerificationCode = binding.tvInvalidVerificationCode
    }


    @SuppressLint("ResourceAsColor")
    private fun setViewsListeners() {
        tvIsInvalidPhoneNumber.setOnClickListener {
            val fragmentManager = parentFragmentManager;
            fragmentManager.popBackStack()
        }

        btnConfirm.setOnClickListener {
            val args = arguments
            val verificationCode = args?.getString("VerificationCode").toString()
            val phoneNumber = args?.getString("PhoneNumber").toString()

            val enteredCode = etVerificationCode.text.toString().replace(Regex("[^0-9+]"), "")
            if (enteredCode.isNotEmpty()) {
                if (enteredCode.length == 6) {
                    val credential =
                        PhoneAuthProvider.getCredential(verificationCode, enteredCode)
                    changePhoneNumber(credential, phoneNumber)
                } else {
                    tvInvalidVerificationCode.visibility = View.VISIBLE
                    tvInvalidVerificationCode.text =
                        requireActivity().getString(R.string.invalid_verification_code)
                    etVerificationCode.setBackgroundResource(R.drawable.et_background_invalid_verification_code)
                }
            } else {
                tvInvalidVerificationCode.visibility = View.VISIBLE
                tvInvalidVerificationCode.text =
                    requireActivity().getString(R.string.edit_text_verification_code_is_empty)
                etVerificationCode.setBackgroundResource(R.drawable.et_background_invalid_verification_code)
            }
        }
        tvSendVerificationCodeAgain.setOnClickListener {
            Toast.makeText(requireActivity(), "Отправлен код снова", Toast.LENGTH_SHORT).show()
            Intent(requireActivity(), VerificationTimerService::class.java).also {
                requireActivity().startService(it)
            }
            tvSendVerificationCodeAgain.text = getString(R.string.send_verification_code_again_in)
            tvSendVerificationCodeAgain.setTextColor(
                ContextCompat.getColor(
                    requireActivity(),
                    R.color.semi_transparent_black
                )
            )
            tvSendVerificationCodeAgain.isEnabled = false
            observeLeftTimeOfSentVerificationCode()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        propertyChangedCallback?.let {
            VerificationTimerService.leftTimeInSeconds.removeOnPropertyChangedCallback(it)
        }
        propertyChangedCallback = null  // Очистка ссылки на колбэк
    }

    private fun changePhoneNumber(credential: PhoneAuthCredential, phoneNumber: String) {
        currentUser.updatePhoneNumber(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val databaseReference =
                        FirebaseDatabase.getInstance().getReference("Users/" + currentUser.uid)
                    databaseReference.child("phoneNumber").setValue(phoneNumber)
                    showSuccessfulLoginDialog()
                } else {
                    tvInvalidVerificationCode.visibility = View.VISIBLE
                    tvInvalidVerificationCode.text =
                        requireActivity().getString(R.string.invalid_verification_code)
                    etVerificationCode.setBackgroundResource(R.drawable.et_background_invalid_verification_code)
                }
            }
    }

    private fun showSuccessfulLoginDialog() {
        val successLoginDialog = SuccessLoginDialog()
        successLoginDialog.show(requireActivity().supportFragmentManager, "SuccessLoginDialog")
        Handler().postDelayed({
            requireActivity().finish()
            successLoginDialog.dismiss()
        }, 3000)
    }

    private fun verificationCodeTextMask() {
        val slots = UnderscoreDigitSlotsParser().parseSlots("_  _  _  _  _  _")
        val formatWatcher = MaskFormatWatcher(
            MaskImpl.createTerminated(slots)
        )
        formatWatcher.installOn(etVerificationCode)
    }

    private fun verificationCodeTextChangeListener() {
        etVerificationCode.addTextChangedListener(object : TextWatcher {
            var textBefore: String? = null
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
                textBefore = s.toString()
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s?.length == 2) {
                    etVerificationCode.setText("")
                }
            }

            override fun afterTextChanged(s: Editable?) {
                if (textBefore!!.length > s!!.length) {
                    if (s.length == 3) {
                        s.replace(1, 3, "")
                    }
                }

                if (tvInvalidVerificationCode.visibility == View.VISIBLE) {
                    if (textBefore != s.toString()) {
                        tvInvalidVerificationCode.visibility = View.GONE
                        etVerificationCode.setBackgroundResource(R.drawable.edit_text_new_phone_number_verification_code)
                    }
                }
            }

        })
    }
}