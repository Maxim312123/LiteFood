package com.diplomaproject.litefood.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.diplomaproject.litefood.R
import com.diplomaproject.litefood.adapters.FavoriteProductAdapter
import com.diplomaproject.litefood.data.Product
import com.diplomaproject.litefood.data.CartProduct
import com.diplomaproject.litefood.databinding.FragmentFavoriteProductBinding
import com.diplomaproject.litefood.managers.FirebaseRealtimeDatabaseRepository


class FavoriteProductFragment : Fragment(), MenuProvider,
    FavoriteProductAdapter.OnItemCLickListener {

    companion object {
        @JvmStatic
        fun newInstance() = FavoriteProductFragment()
    }

    private lateinit var binding: FragmentFavoriteProductBinding

    private lateinit var recyclerView: RecyclerView
    private lateinit var favoriteProductAdapter: FavoriteProductAdapter
    private lateinit var realtimeDatabaseManager: FirebaseRealtimeDatabaseRepository
    private var userFavoriteProducts: MutableList<Product> = mutableListOf()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFavoriteProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        init()
        extractFavoriteProductsFromBundle()
        loadFavoriteProducts()
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.toolbar_favorite_product_fragment, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.delete_all -> {
                realtimeDatabaseManager.clearFavoriteProducts()
                favoriteProductAdapter.clearList()
            }
        }
        return false
    }

    override fun onPopupMenuClick(favoriteProduct: Product, position: Int) {
        deleteFavoriteProduct(favoriteProduct, position)
    }

    override fun onButtonClick(favoriteProduct: Product, position: Int) {
//        var shoppingBasketProduct = ShoppingBasketProduct(favoriteProduct)
//        if (favoriteProduct.isAddedToBasket) {
//            realtimeDatabaseManager.writeToShoppingBasket(shoppingBasketProduct)
//        } else {
//            realtimeDatabaseManager.deleteProductFromShoppingBasket(shoppingBasketProduct)
//        }
    }

    private fun initViews() {
        recyclerView = binding.recyclerView
    }

    private fun init() {
        favoriteProductAdapter = FavoriteProductAdapter(requireActivity(), this)
        recyclerView.adapter = favoriteProductAdapter
        realtimeDatabaseManager = FirebaseRealtimeDatabaseRepository()
        requireActivity().addMenuProvider(this, viewLifecycleOwner)
    }

    private fun extractFavoriteProductsFromBundle() {
        arguments?.let { bundle ->
            userFavoriteProducts = bundle.getParcelableArrayList("favoriteProducts")!!
        }
    }

    private fun loadFavoriteProducts() {
        realtimeDatabaseManager.fetchProductsFromCart(object :
            FirebaseRealtimeDatabaseRepository.ShoppingBasketDataCallback {
            override fun onDataRetrieved(retrievedProducts: MutableList<CartProduct>) {

                userFavoriteProducts.forEach { product ->
                    val isAddedToShoppingBasket =
                        retrievedProducts.any { basketProduct -> basketProduct.id == product.id }
                    if (isAddedToShoppingBasket) {
                        product.isAddedToBasket = true
                    }
                }
                favoriteProductAdapter.updateList(userFavoriteProducts)
            }
        })
    }

    private fun deleteFavoriteProduct(
        favoriteProduct: Product,
        position: Int
    ) {
        realtimeDatabaseManager.deleteFromFavoriteProducts(favoriteProduct)
        favoriteProductAdapter.deleteItem(position)
    }

}