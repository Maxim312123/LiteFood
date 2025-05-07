package com.diplomaproject.litefood.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.diplomaproject.litefood.R
import com.diplomaproject.litefood.activities.MainActivity
import com.diplomaproject.litefood.adapters.CartAdapter
import com.diplomaproject.litefood.data.CartProduct
import com.diplomaproject.litefood.data.User
import com.diplomaproject.litefood.databinding.FragmentBasketBinding
import com.diplomaproject.litefood.fragments.view_models.CartViewModel
import com.diplomaproject.litefood.managers.FirebaseRealtimeDatabaseRepository
import com.diplomaproject.litefood.managers.FirebaseRealtimeDatabaseRepository.ShoppingBasketDataCallback
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView


private const val ARG_USER = "User"
private const val TAG = "CartFragment"

class CartFragment : Fragment(), MenuProvider, CartAdapter.OnItemClickListener {

    private lateinit var binding: FragmentBasketBinding

    private lateinit var recyclerView: RecyclerView
    private lateinit var pullToRefresh: SwipeRefreshLayout
    private lateinit var tvAddress: TextView
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var btnToStartOrderRegistration: Button
    private lateinit var tvStartOrderRegistration: TextView
    private lateinit var tvTotalProductsForBuyingPrice: TextView
    private lateinit var tvProductsForBuyingAmount: TextView
    private lateinit var editAddress: MaterialCardView

    private lateinit var cartAdapter: CartAdapter
    private lateinit var firebaseRealtimeDatabaseManager: FirebaseRealtimeDatabaseRepository
    private var productAmountForBuying = 0
    private lateinit var cartViewModel: CartViewModel

    private lateinit var user: User

    private var productsForBuying: MutableList<CartProduct> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        arguments?.let {
            user = it.getParcelable(ARG_USER)!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBasketBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().addMenuProvider(this, viewLifecycleOwner)
        cartViewModel = ViewModelProvider(this).get(CartViewModel::class.java)
        initViews()
        init()
        setUserAddress()
        fetchBasketProducts()
        setupToolbar()
        setupViewListeners()
        setupCartObservers()
    }

    override fun onResume() {
        super.onResume()
        if (bottomNavigationView.visibility == View.GONE) {
            bottomNavigationView.visibility = View.VISIBLE
        }
    }

    private fun setupCartObservers() {
        setupProductAmountObserver()
        setupIsBasketEmptyObserver()
        setupProductAmountForBuyingObserver()
        setupProductForBuyingPriceObserver()
        setupButtonToStartOrderRegistrationObserver()
    }

    private fun setupButtonToStartOrderRegistrationObserver() {
        val mediatorLiveData = MediatorLiveData<Int>()
        mediatorLiveData.addSource(cartViewModel.productsForBuying) { productsForBuying ->
            if (productsForBuying.isEmpty()) {
                btnToStartOrderRegistration.setText(getString(R.string.choose_products))
                binding.constraint.visibility = View.GONE
                tvStartOrderRegistration.visibility = View.GONE
            } else {
                btnToStartOrderRegistration.setText("")
                binding.constraint.visibility = View.VISIBLE
                tvStartOrderRegistration.visibility = View.VISIBLE
            }
        }

        mediatorLiveData.observe(viewLifecycleOwner) {}
    }

    @SuppressLint("StringFormatMatches", "DefaultLocale")
    private fun setupProductForBuyingPriceObserver() {
        cartViewModel.productsForBuying
            .observe(viewLifecycleOwner) { productsForBuying ->
                this.productsForBuying = productsForBuying.toMutableList()
                var totalPrice = 0.0
                productsForBuying.forEach { product ->
                    totalPrice += product.totalPrice
                }
                tvTotalProductsForBuyingPrice.setText(
                    getString(
                        R.string.currency,
                        String.format("%.2f", totalPrice)
                    )
                )
            }
    }

    private fun setupProductAmountForBuyingObserver() {
        cartViewModel.productsForBuying
            .observe(viewLifecycleOwner) { productsForBuying ->
                tvProductsForBuyingAmount.setText(
                    getString(
                        R.string.quantity,
                        productsForBuying.size
                    )
                )
            }
    }

    private fun setupIsBasketEmptyObserver() {
        val mediatorLiveData = MediatorLiveData<Int>()

        mediatorLiveData.addSource(cartViewModel.productAmount) { productAmount ->
            if (productAmount == 0) {
                parentFragmentManager.beginTransaction().replace(
                    R.id.fragment_container, EmptyShoppingBasketFragment.newInstance()
                ).commit()
            }
        }

        mediatorLiveData.observe(viewLifecycleOwner) {}
    }

    private fun setupProductAmountObserver() {
        cartViewModel.productAmount.observe(viewLifecycleOwner) { amount ->
            binding.tvProductAmount.setText(getString(R.string.quantity, amount))
        }
    }

    @SuppressLint("MissingPermission")
    private fun setupViewListeners() {
        pullToRefresh.setOnRefreshListener {
            firebaseRealtimeDatabaseManager.fetchProductsFromCart(object :
                ShoppingBasketDataCallback {
                override fun onDataRetrieved(retrievedProducts: MutableList<CartProduct>) {
                    cartAdapter.updateList(retrievedProducts)
                    pullToRefresh.isRefreshing = false
                }
            })
        }

        editAddress.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AddressFragment.newInstance())
                .addToBackStack("AddressFragment").commit()
            hideBottomNavigationView()
        }

        btnToStartOrderRegistration.setOnClickListener {
            productsForBuying.sortByDescending { it.timeStamp }
            if (cartViewModel.isProductsForBuyingEmpty() == false) {
                parentFragmentManager.beginTransaction()
                    .replace(
                        R.id.fragment_container,
                        OrderRegistrationFragment.newInstance(productsForBuying)
                    )
                    .addToBackStack("OrderRegistrationFragment").commit()
                hideBottomNavigationView()
            }
        }

    }

    private fun init() {
        firebaseRealtimeDatabaseManager = FirebaseRealtimeDatabaseRepository()
    }

    @SuppressLint("SetTextI18n")
    private fun initViews() {
        recyclerView = binding.recyclerView
        tvProductsForBuyingAmount = binding.tvProductsForBuyingAmount
        tvTotalProductsForBuyingPrice = binding.tvTotalProductsForBuyingPrice
        tvStartOrderRegistration = binding.tvStartOrderRegistration
        pullToRefresh = binding.pullToRefresh
        tvAddress = binding.layoutEditAddress.tvMainAddress
        bottomNavigationView = (activity as MainActivity).findViewById(R.id.bottomNavigationView)
        btnToStartOrderRegistration = binding.btmToStartOrderRegistration
        editAddress = binding.layoutEditAddress.cardViewEditAddress

    }

    private fun setUserAddress() {
        val addresses = user.addresses
        if (addresses != null) {
            val mainAddress = addresses.get("main")
            if (mainAddress != null) {
                tvAddress.text =
                    "${mainAddress.get("city")}, ${mainAddress.get("street")}, ${mainAddress.get("houseNumber")} "
            }
        } else {
            tvAddress.setText(R.string.address_is_not_addded)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun fetchBasketProducts() {
        var basketProducts: MutableList<CartProduct> = convertBasketToList()

        cartAdapter = CartAdapter(cartViewModel, basketProducts, this)
        recyclerView.adapter = cartAdapter

        cartViewModel.setProductAmount(11)
    }

    private fun convertBasketToList(): MutableList<CartProduct> {
        val userBasket = user.basket
        var basketProducts: MutableList<CartProduct> = mutableListOf()

        userBasket?.forEach { entry ->
            val product = entry.value as? CartProduct
            if (product != null) {
                basketProducts.add(product)
                if (product.forBuying) {
                    productAmountForBuying++
                }
            }
        }
        return basketProducts
    }

    @SuppressLint("SetTextI18n")
    private fun setupToolbar() {
        val toolbar = (activity as MainActivity).findViewById<MaterialToolbar>(R.id.materialToolbar)
        toolbar.title = getString(R.string.basket_toolbar_title)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {}

    override fun onMenuItemSelected(p0: MenuItem): Boolean {
        return true
    }

    companion object {
        @JvmStatic
        fun newInstance(user: User) = CartFragment().apply {
            arguments = Bundle().apply {
                putParcelable(ARG_USER, user)
            }
        }

        const val KEY_PRODUCT = "Product"
    }

    @SuppressLint("CommitTransaction")
    override fun onCardClick(position: Int) {
        val clickedProduct = cartAdapter.getProductByPosition(position)
        parentFragmentManager.beginTransaction().replace(
            R.id.fragment_container,
            ProductDescriptionFragment.newInstance(clickedProduct)
        ).addToBackStack("ProductDescriptionFragment").commit()
        bottomNavigationView.visibility = View.GONE
    }

    override fun onOverFlowMenuClick(view: View, position: Int) {
        val product = cartAdapter.getProductByPosition(position)
        val popupMenu = PopupMenu(requireActivity(), view)

        popupMenu.getMenuInflater()
            .inflate(R.menu.popup_menu_shopping_basket_product, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {
            override fun onMenuItemClick(menuItem: MenuItem?): Boolean {
                val itemId = menuItem?.itemId
                when (itemId) {
                    R.id.delete_shopping_basket_product -> {
                        deleteProduct(product, position)
                        return true
                    }
                }
                return false
            }

        })

        popupMenu.show()
    }

    override fun onIncreaseAmountProductClick(
        product: CartProduct,
        position: Int
    ) {
        var productAmount = product.amount
        if (productAmount < 99) {
            productAmount++
            firebaseRealtimeDatabaseManager.changeProductAmount(product.id, productAmount) {

            }
            cartAdapter.changProductAmount(position, productAmount)
            cartViewModel.updateProductForBuyingAmount(product.id, productAmount)
        }
    }

    override fun onDecreaseAmountProductClick(
        product: CartProduct,
        position: Int
    ) {
        var productAmount = product.amount
        if (productAmount in 2..99) {
            productAmount--
            firebaseRealtimeDatabaseManager.changeProductAmount(product.id, productAmount) {

            }
            cartAdapter.changProductAmount(position, productAmount)
            cartViewModel.updateProductForBuyingAmount(product.id, productAmount)
        }
    }

    override fun onCheckboxClick(position: Int, isChecked: Boolean) {
        val product = cartAdapter.getProductByPosition(position)
        product.forBuying = isChecked
        firebaseRealtimeDatabaseManager.changeProductIsForBuying(product.id, isChecked)

        if (!isChecked) {
            cartViewModel.removeProductFromForBuying(product)
        } else {
            cartViewModel.addProductForBuying(product)
        }
    }

    override fun onButtonBuyProductClick(position: Int) {
        val clickedProduct = cartAdapter.getProductByPosition(position)

        val orderRegistrationFragment = OrderRegistrationFragment().apply {
            arguments = Bundle().apply {
                putParcelable(KEY_PRODUCT, clickedProduct)
            }
        }

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, orderRegistrationFragment)
            .addToBackStack("OrderRegistrationFragment")
            .commit()
        hideBottomNavigationView()
    }

    private fun hideBottomNavigationView() {
        bottomNavigationView.visibility = View.GONE
    }

    private fun deleteProduct(
        product: CartProduct,
        position: Int
    ) {
        firebaseRealtimeDatabaseManager.deleteProductFromCart(product.id)
        cartAdapter.removeProductByPosition(position)
        cartViewModel.setProductAmount(cartAdapter.getBasketSize())

        if (product.forBuying) {
            cartViewModel.removeProductFromForBuying(product)
        }
    }


}