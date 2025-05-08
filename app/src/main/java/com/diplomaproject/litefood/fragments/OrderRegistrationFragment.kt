package com.diplomaproject.litefood.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.diplomaproject.litefood.R
import com.diplomaproject.litefood.UserViewModel
import com.diplomaproject.litefood.adapters.ProductForBuyingAdapter
import com.diplomaproject.litefood.data.Address
import com.diplomaproject.litefood.data.CartProduct
import com.diplomaproject.litefood.databinding.FragmentOrderRegistrationBinding
import com.diplomaproject.litefood.fragments.view_models.OrderRegistrationFragmentViewModel
import com.diplomaproject.litefood.repository.FirebaseRealtimeDatabaseRepository
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip

private const val ARG_PRODUCTS_FOR_BUYING = "Products"

class OrderRegistrationFragment : Fragment(), MenuProvider {

    private var _binding: FragmentOrderRegistrationBinding? = null
    private val binding get() = _binding!!

    private lateinit var firebaseRealTimeDatabaseManager: FirebaseRealtimeDatabaseRepository
    private lateinit var viewModel: OrderRegistrationFragmentViewModel
    private lateinit var userViewModel: UserViewModel

    private lateinit var cardViewEditAddress: MaterialCardView
    private lateinit var toolbar: MaterialToolbar
    private lateinit var tvMainAddress: TextView
    private lateinit var chipPayInCash: Chip
    private lateinit var chipPayByCard: Chip
    private lateinit var tvCardNumber: TextView
    private lateinit var ivPaymentSystem: ImageView
    private lateinit var recyclerView: RecyclerView
    private lateinit var productForBuyingAdapter: ProductForBuyingAdapter

    private var products: MutableList<CartProduct> = mutableListOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { arguments ->
            arguments.getParcelable<CartProduct>(CartFragment.KEY_PRODUCT)?.let { products.add(it) }
            products = arguments.getParcelableArrayList(ARG_PRODUCTS_FOR_BUYING)!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderRegistrationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().addMenuProvider(this, viewLifecycleOwner)
        initViews()
        init()
        initActionBar()
        setupViewListeners()
        initObservers()
        bindDataToUI()
    }

    private fun bindTermsOfUseAndPolicy() {
        val termsOfUseAndPolicy = getString(R.string.terms_of_use_and_policy)
        val spannableString = SpannableString(termsOfUseAndPolicy)

        val linkColor = resources.getColor(R.color.golden_yellow)

        val termsOfUseStart = termsOfUseAndPolicy.indexOfFirst { it == 'У' }
        val termsOfUseEnd = termsOfUseStart + 23

        val termsOfUseClickableSpan: ClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://doc-hosting.flycricket.io/litefood-terms-of-use/cfe0a265-56e1-45e0-b4b4-cca77ded0baf/terms")
                )
                startActivity(intent)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = linkColor
            }
        }
        spannableString.setSpan(
            termsOfUseClickableSpan,
            termsOfUseStart,
            termsOfUseEnd,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        val privacyPolicyStart = termsOfUseAndPolicy.indexOfFirst { it == 'П' }
        val privacyPolicyEnd: Int = termsOfUseAndPolicy.length - 1

        val privacyPolicyClickableSpan: ClickableSpan = object : ClickableSpan() {
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
            privacyPolicyClickableSpan,
            privacyPolicyStart,
            privacyPolicyEnd,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        binding.tvTermsOfUseAndPolicy.setText(spannableString)
        binding.tvTermsOfUseAndPolicy.setMovementMethod(LinkMovementMethod())
    }

    @SuppressLint("DefaultLocale")
    private fun bindDataToUI() {
        bindTotalProductAmount()
        bindTotalProductSum()
        bindTermsOfUseAndPolicy()
    }

    private fun bindTotalProductSum() {
        var totalPrice = 0.0
        products.forEach { product ->
            totalPrice += product.totalPrice
        }

        binding.tvTotalSum.text = getString(R.string.currency, String.format("%.2f", totalPrice))
    }

    private fun bindTotalProductAmount() {
        var totalAmount = 0

        products.forEach { product ->
            totalAmount += product.amount
        }

        binding.tvProductAmount.text = getString(R.string.quantity, totalAmount)
    }

    private fun initObservers() {
        viewModel.navigateToAddressFragment.observe(viewLifecycleOwner) {
            if (it) {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, AddressFragment.newInstance())
                    .addToBackStack(null).commit()
                viewModel.onNavigatedToAddressFragment()
            }
        }

        viewModel.fetchMainUserAddress()

        viewModel.mainAddress.observe(viewLifecycleOwner) { mainAddress ->
            tvMainAddress.text = mainAddress?.let {
                "${mainAddress.city}, ${mainAddress.street}, ${mainAddress.houseNumber}"
            } ?: "Адрес не добавлен"

        }

        viewModel.fetchUserMainPaymentMethod()
        viewModel.mainPaymentMethod.observe(viewLifecycleOwner) { creditCard ->
            creditCard.let {
                binding.tvAddressIsNotAdded.text = "${creditCard?.last4Digits}"
            }
        }
    }

    private fun hashMapToAddress(hashMap: HashMap<String, HashMap<String, String>>): Address {
        val mainHashMap = hashMap.get("main")
        val city = mainHashMap?.get("city") ?: ""
        val street = mainHashMap?.get("street") ?: ""
        val houseNumber = mainHashMap?.get("houseNumber") ?: ""
        return Address(city, street, houseNumber)
    }

    override fun onStart() {
        super.onStart()
    }

    private fun setupViewListeners() {
        chipPayInCash.setOnClickListener {
            val chip = it as Chip
            chip.isChecked = true
            chipPayByCard.isChecked = false
            tvCardNumber.visibility = View.GONE
            ivPaymentSystem.visibility = View.GONE
            binding.layoutAllPaymentMethods.visibility = View.GONE
        }

        chipPayByCard.setOnClickListener {
            val chip = it as Chip
            chip.isChecked = true
            chipPayInCash.isChecked = false
            tvCardNumber.visibility = View.VISIBLE
            ivPaymentSystem.visibility = View.VISIBLE
            binding.layoutAllPaymentMethods.visibility = View.VISIBLE
        }

        cardViewEditAddress.setOnClickListener {
            viewModel.navigateToAddressFragment()
        }
    }

    override fun onResume() {
        super.onResume()
    }

    private fun init() {
        firebaseRealTimeDatabaseManager = FirebaseRealtimeDatabaseRepository()
        viewModel =
            ViewModelProvider(this).get(OrderRegistrationFragmentViewModel::class.java)
        userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)
        productForBuyingAdapter = ProductForBuyingAdapter(products)
        recyclerView.adapter = productForBuyingAdapter
    }

    private fun initActionBar() {
        toolbar.title = "Оформление заказа"
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        (activity as AppCompatActivity).supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun initViews() {
        toolbar = binding.toolbar
        cardViewEditAddress = binding.layoutEditAddress.cardViewEditAddress
        tvMainAddress = binding.layoutEditAddress.tvMainAddress
        chipPayInCash = binding.chipPayInCash
        chipPayByCard = binding.chipPayByCard
        tvCardNumber = binding.tvCardNumber
        ivPaymentSystem = binding.ivPaymentSystem
        recyclerView = binding.recyclerView
    }


    override fun onCreateMenu(p0: Menu, p1: MenuInflater) {}

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            android.R.id.home -> {
                parentFragmentManager.popBackStack()
                return true
            }
        }
        return false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance(products: MutableList<CartProduct>) = OrderRegistrationFragment().apply {
            arguments = Bundle().apply {
                putParcelableArrayList(ARG_PRODUCTS_FOR_BUYING, ArrayList(products))
            }
        }
    }
}