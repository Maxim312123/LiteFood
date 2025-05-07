package com.diplomaproject.litefood.dialogs

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import com.diplomaproject.litefood.R
import com.diplomaproject.litefood.UserViewModel
import com.diplomaproject.litefood.adapters.PaymentMethodAdapter
import com.diplomaproject.litefood.data.User
import com.diplomaproject.litefood.databinding.BottomSheetAddCreditCardBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.stripe.android.ApiResultCallback
import com.stripe.android.Stripe
import com.stripe.android.model.PaymentMethod
import com.stripe.android.model.PaymentMethodCreateParams
import ru.tinkoff.decoro.MaskImpl
import ru.tinkoff.decoro.parser.UnderscoreDigitSlotsParser
import ru.tinkoff.decoro.slots.PredefinedSlots
import ru.tinkoff.decoro.watchers.FormatWatcher
import ru.tinkoff.decoro.watchers.MaskFormatWatcher

class AddCreditCardBottomSheet(
    private val cardsAdapter: PaymentMethodAdapter,
    private val cardsList: MutableList<HashMap<String, Any>?>
) : BottomSheetDialogFragment() {

    private lateinit var binding: BottomSheetAddCreditCardBinding
    private lateinit var iv_close: ImageView
    private lateinit var et_card_number: EditText
    private lateinit var et_validity_period: EditText
    private lateinit var et_sec_code: EditText
    private lateinit var btn_add: Button
    private lateinit var tvCardNumber: TextView
    private lateinit var tvValidityPeriod: TextView
    private lateinit var tvSecCode: TextView
    private lateinit var tvErrorCardNumber: TextView
    private lateinit var tvErrorValidityPeriod: TextView
    private lateinit var tvErrorSecCode: TextView
    private lateinit var databaseRef: DatabaseReference
    private lateinit var currentUserUid: String
    private lateinit var userViewModel: UserViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = BottomSheetAddCreditCardBinding.inflate(layoutInflater)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        setupListeners()
        setMaskOnCardNumberEditText()
        setMaskOnValidityPeriodEditText()
        observeAllInputCardData()
        observeValidInputCardData()
    }

    private fun init() {
        iv_close = binding.ivClose
        et_card_number = binding.etCardNumber
        et_validity_period = binding.etValidityPeriod
        et_sec_code = binding.etSecCode
        btn_add = binding.btnAdd
        tvCardNumber = binding.tvCardNumber
        tvValidityPeriod = binding.tvValidityPeriod
        tvSecCode = binding.tvSecCode
        tvErrorCardNumber = binding.tvErrorCardNumber
        tvErrorValidityPeriod = binding.tvErrorValidityPeriod
        tvErrorSecCode = binding.tvErrorSecCode
        currentUserUid = FirebaseAuth.getInstance().currentUser?.uid.toString()
        databaseRef = FirebaseDatabase.getInstance().getReference("Users/" + currentUserUid)
        userViewModel = ViewModelProvider(requireActivity()).get(UserViewModel::class.java)
    }

    private fun setupListeners() {
        iv_close.setOnClickListener {
            if (et_card_number.text.isNotEmpty() || et_validity_period.text.isNotEmpty() || et_sec_code.text.isNotEmpty()) {
                showAlertDialog()
            } else {
                dismiss()
            }
        }

        btn_add.setOnClickListener {
            val cardNumber = et_card_number.text.toString().replace(Regex("[^0-9]"), "")

            val expirationDate = et_validity_period.text.toString().replace(Regex("[^0-9]"), "")
            val exp_month = expirationDate.substring(0, 2).toInt()
            val exp_year = expirationDate.substring(2, 4).toInt()

            val secCode = et_sec_code.text.toString()

            createCreditCard(cardNumber, exp_month, exp_year, secCode)
        }
    }

    @SuppressLint("RestrictedApi")
    private fun createCreditCard(
        cardNumber: String,
        expMonth: Int,
        expYear: Int,
        cvv: String
    ) {
        val cardParams = createCardParams(cardNumber, expMonth, expYear, cvv)
        val stripe = createStripeInstance()

        val processingDialog = ProcessingDialog()
        processingDialog.show(childFragmentManager, null)

        val handler = Handler(Looper.getMainLooper())

        var paymentMethods = HashMap<String, HashMap<String, Any>>()
        userViewModel.user.observe(viewLifecycleOwner) { user: User ->
            paymentMethods = user.paymentMethod ?: hashMapOf()
            cardsList.clear()
            cardsList.addAll(paymentMethods.values)
        }

        handler.postDelayed({
            stripe.createPaymentMethod(
                cardParams,
                callback = object : ApiResultCallback<PaymentMethod> {
                    @SuppressLint("NotifyDataSetChanged")
                    override fun onSuccess(paymentMethod: PaymentMethod) {
                        val paymentToken = paymentMethod.id.toString()
                        val cardInfo = paymentMethod.card
                        val last4Digits = cardInfo?.last4.toString()
                        val cardBrand = cardInfo?.brand.toString()

                        //Запись в БД
                        val cardData: HashMap<String, Any> = hashMapOf(
                            "token" to paymentToken,
                            "last4Digits" to last4Digits,
                            "cardBrand" to cardBrand,
                            "isMainPaymentMethod" to false
                        )
                        val size = cardsList.size
                        if (paymentMethods.isEmpty()) {
                            cardData.set("isMainPaymentMethod", true)
                            paymentMethods["main"] = cardData
                        } else if (paymentMethods.isNotEmpty()) {
                            paymentMethods["extra $size"] = cardData
                        }
                        if (!cardsList.contains(cardData)) {
                            cardsList.add(cardData)
                            cardsAdapter.notifyDataSetChanged()
                        }
                        databaseRef.child("/paymentMethod").setValue(paymentMethods)
                        Toast.makeText(requireActivity(), "Карта успешно добавлена", LENGTH_SHORT)
                            .show()
                        dismiss()
                        processingDialog.dismiss()
                    }

                    override fun onError(e: Exception) {
                        Toast.makeText(
                            requireActivity(),
                            "Ошибка: ${e.message}",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                        processingDialog.dismiss()
                    }
                })
        }, 4000)
    }

    @SuppressLint("RestrictedApi")
    private fun createCardParams(
        cardNumber: String,
        expMonth: Int,
        expYear: Int,
        cvv: String
    ): PaymentMethodCreateParams {
        val card = PaymentMethodCreateParams.Card(cardNumber, expMonth, expYear, cvv)
        return PaymentMethodCreateParams.create(card)
    }

    private fun createStripeInstance(): Stripe {
        return Stripe(
            requireActivity(),
            "pk_test_51QdWv903PeswpTC1zCqs1OmYfzS6tfYQk2PcN7nYi9ZEN9CMvmJHMIuv9UvxtKpR4BeDOWnVJeHPBZPtExd18b9v00waRi9mLs"
        )
    }

    private fun setMaskOnCardNumberEditText() {
        val mask = MaskImpl.createTerminated(PredefinedSlots.CARD_NUMBER_STANDART)
        val formatWatcher: FormatWatcher = MaskFormatWatcher(
            mask
        )
        formatWatcher.installOn(et_card_number)
    }

    private fun setMaskOnValidityPeriodEditText() {
        val slots = UnderscoreDigitSlotsParser().parseSlots("__ / __");
        val formatWatcher: FormatWatcher = MaskFormatWatcher(
            MaskImpl.createTerminated(slots)
        )
        formatWatcher.installOn(et_validity_period)
    }


    private fun observeAllInputCardData() {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                val totalLength =
                    et_card_number.text.length + et_validity_period.text.length + et_sec_code.text.length
                setEnableAddingCard(totalLength)
            }

            override fun afterTextChanged(s: Editable?) {

            }
        }

        et_card_number.addTextChangedListener(textWatcher)
        et_validity_period.addTextChangedListener(textWatcher)
        et_sec_code.addTextChangedListener(textWatcher)
    }

    private fun setEnableAddingCard(totalLength: Int) {
        if (totalLength == 29) {
            btn_add.isEnabled = true
            btn_add.setBackgroundColor(Color.BLACK)
        } else {
            btn_add.isEnabled = false
            btn_add.setBackgroundColor(Color.parseColor("#4D000000"))
        }
    }

    private fun observeValidInputCardData() {

        val defaultColor: Int = resources.getColor(R.color.semi_transparent_black)

        et_card_number.setOnFocusChangeListener(object : OnFocusChangeListener {
            override fun onFocusChange(v: View?, hasFocus: Boolean) {
                if (!hasFocus && et_card_number.length() > 0 && et_card_number.length() < 19) {
                    tvCardNumber.setTextColor(Color.RED)
                    et_card_number.setBackgroundTintList(ColorStateList.valueOf(Color.RED))
                    tvErrorCardNumber.visibility = View.VISIBLE
                } else if (hasFocus) {
                    tvCardNumber.setTextColor(defaultColor)
                    et_card_number.setBackgroundTintList(ColorStateList.valueOf(Color.BLACK))
                    tvErrorCardNumber.visibility = View.GONE
                }
            }
        })

        et_validity_period.setOnFocusChangeListener(object : OnFocusChangeListener {
            override fun onFocusChange(v: View?, hasFocus: Boolean) {
                if (!hasFocus && et_validity_period.length() > 0 && et_validity_period.length() < 7) {
                    tvValidityPeriod.setTextColor(Color.RED)
                    et_validity_period.setBackgroundTintList(ColorStateList.valueOf(Color.RED))
                    tvErrorValidityPeriod.visibility = View.VISIBLE
                } else if (hasFocus) {
                    tvValidityPeriod.setTextColor(defaultColor)
                    et_validity_period.setBackgroundTintList(ColorStateList.valueOf(Color.BLACK))
                    tvErrorValidityPeriod.visibility = View.GONE
                }
            }
        })

        et_sec_code.setOnFocusChangeListener(object : OnFocusChangeListener {
            override fun onFocusChange(v: View?, hasFocus: Boolean) {
                if (!hasFocus && et_sec_code.length() > 0 && et_sec_code.length() < 3) {
                    tvSecCode.setTextColor(Color.RED)
                    et_sec_code.setBackgroundTintList(ColorStateList.valueOf(Color.RED))
                    tvErrorSecCode.visibility = View.VISIBLE
                } else if (hasFocus) {
                    tvSecCode.setTextColor(defaultColor)
                    et_sec_code.setBackgroundTintList(ColorStateList.valueOf(Color.BLACK))
                    tvErrorSecCode.visibility = View.GONE
                }
            }
        })
    }

    @SuppressLint("MissingInflatedId")
    private fun showAlertDialog() {
        val inflater: LayoutInflater = layoutInflater
        val dialogView: View = inflater.inflate(R.layout.dialog_confirm_closing, null)

        val builder = AlertDialog.Builder(requireActivity())
        builder.setView(dialogView)

        val positiveButton = dialogView.findViewById<Button>(R.id.btn_positive)
        val negativeButton = dialogView.findViewById<Button>(R.id.btn_negative)

        val dialog = builder.create()
        dialog.setCancelable(false)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        positiveButton.setOnClickListener {
            dismiss()
            dialog.dismiss()
        }

        negativeButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

}