package com.diplomaproject.litefood.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.diplomaproject.litefood.R
import com.diplomaproject.litefood.activities.MainActivity
import com.diplomaproject.litefood.adapters.ProductAdapter
import com.diplomaproject.litefood.data.Product
import com.diplomaproject.litefood.databinding.FragmentProductBinding
import com.diplomaproject.litefood.repository.FirebaseRealtimeDatabaseRepository
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch


private const val ARG_PRODUCTS = "Products"
private const val ARG_CATEGORY_NAME = "Category name"

class ProductFragment : Fragment(), MenuProvider, ProductAdapter.OnProductCardViewClickListener {

    private lateinit var binding: FragmentProductBinding

    private lateinit var toolbar: MaterialToolbar
    private lateinit var recyclerView: RecyclerView

    private lateinit var productAdapter: ProductAdapter
    private lateinit var products: MutableList<Product>
    private lateinit var categoryName: String

    private lateinit var bottomNavigationView: BottomNavigationView

    private val firebaseRealtimeDatabaseManager = FirebaseRealtimeDatabaseRepository()

    private var savedRecyclerViewScrollPosition = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            categoryName = it.getString(ARG_CATEGORY_NAME).toString()
            products = it.getParcelableArrayList(ARG_PRODUCTS)!!
        }
        fetchProducts()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProductBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        setupToolbar()
        requireActivity().addMenuProvider(this, viewLifecycleOwner)
    }

    private fun fetchProducts() {
        lifecycleScope.launch {
            val favoriteProducts =
                firebaseRealtimeDatabaseManager.retrieveProductsFromFavorite().await()

            val shoppingBasketProducts =
                firebaseRealtimeDatabaseManager.fetchProductsFromCart().await()

            products.forEach { product ->
                val isInBasket = shoppingBasketProducts.any { basketProduct ->
                    basketProduct.id == product.id
                }
                if (isInBasket) {
                    product.isAddedToBasket = true
                }
                val isInFavorite = favoriteProducts?.any { favoriteProduct ->
                    favoriteProduct.id == product.id
                }
                if (isInFavorite == true) {
                    product.isFavoriteProduct = true
                }
            }

            productAdapter = ProductAdapter(requireActivity(), this@ProductFragment)
            productAdapter.updateProducts(products)
            recyclerView.adapter = productAdapter
        }
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as MainActivity).toggleBottomNavigationViewVisibility(true)
        recyclerView.post {
            recyclerView.scrollToPosition(savedRecyclerViewScrollPosition)
        }
    }

    private fun init() {
        recyclerView = binding.recyclerView
        if (::productAdapter.isInitialized) {
            productAdapter.updateProducts(products)
            recyclerView.adapter = productAdapter
        }
        bottomNavigationView =
            (activity as MainActivity).findViewById(R.id.bottomNavigationView)
    }

    private fun setupToolbar() {
        toolbar = binding.materialToolbar
        toolbar.setTitle(categoryName)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        (activity as AppCompatActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
        }
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {}

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            android.R.id.home -> {
                parentFragmentManager.popBackStack(
                    null,
                    FragmentManager.POP_BACK_STACK_INCLUSIVE
                )
                return true
            }
        }
        return false
    }

    override fun onProductCardViewClick(position: Int) {
        savedRecyclerViewScrollPosition = recyclerView.computeVerticalScrollOffset()
        val clickedProduct = productAdapter.getProductByPosition(position)
        parentFragmentManager.beginTransaction()
            .replace(
                R.id.fragment_container,
                ProductDescriptionFragment.newInstance(clickedProduct)
            )
            .addToBackStack(null)
            .commit()
        (requireActivity() as MainActivity).toggleBottomNavigationViewVisibility(false)
    }

    companion object {
        @JvmStatic
        fun newInstance(categoryName: String, products: MutableList<Product>) =
            ProductFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_CATEGORY_NAME, categoryName)
                    putParcelableArrayList(ARG_PRODUCTS, ArrayList(products))
                }
            }
    }

}