package com.diplomaproject.litefood.activities

//noinspection SuspiciousImport
import android.R
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.MenuProvider
import com.diplomaproject.litefood.data.Product
import com.diplomaproject.litefood.databinding.ActivityFavoriteProductBinding
import com.diplomaproject.litefood.fragments.EmptyFavoriteProductFragment
import com.diplomaproject.litefood.fragments.FavoriteProductFragment
import com.diplomaproject.litefood.managers.FirebaseRealtimeDatabaseRepository

class FavoriteProductActivity : AppCompatActivity(), MenuProvider {
    private val binding: ActivityFavoriteProductBinding by lazy {
        ActivityFavoriteProductBinding.inflate(
            layoutInflater
        )
    }
    private lateinit var toolbar: Toolbar
    private lateinit var realtimeDatabaseManager: FirebaseRealtimeDatabaseRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initViews()
        init()
        initToolbar()
        fetchFavoriteProducts()
        this.addMenuProvider(this)
    }

    override fun onCreateMenu(menu: Menu, p1: MenuInflater) {}

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        val itemId = menuItem.itemId
        when (itemId) {
            R.id.home -> {
                finish()
                return true
            }
        }
        return false
    }

    private fun fetchFavoriteProducts() {
        realtimeDatabaseManager.retrieveProductsFromFavorite(object :
            FirebaseRealtimeDatabaseRepository.FavoriteProductsDataCallback {
            override fun onDataRetrieved(retrievedProducts: MutableList<Product>) {
                showNecessaryFragment(retrievedProducts)
            }
        })
    }

    private fun initViews() {
        toolbar = binding.toolbar2
    }

    private fun init() {
        realtimeDatabaseManager = FirebaseRealtimeDatabaseRepository()
    }

    private fun initToolbar() {
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setDisplayShowHomeEnabled(true)
            actionBar.setDisplayShowTitleEnabled(false)
        }
    }

    private fun showNecessaryFragment(retrievedProducts: MutableList<Product>) {
        if (retrievedProducts.isEmpty()) {
            supportFragmentManager.beginTransaction()
                .replace(binding.favoriteProductsFragmentContainer.id, EmptyFavoriteProductFragment()).commit()
        } else {
            val favoriteProductFragment = FavoriteProductFragment()

            val bundle = Bundle()
            bundle.putSerializable(
                "favoriteProducts",
                ArrayList(retrievedProducts)
            )
            favoriteProductFragment.arguments = bundle

            supportFragmentManager.beginTransaction()
                .replace(binding.favoriteProductsFragmentContainer.id, favoriteProductFragment)
                .commitAllowingStateLoss()
        }
    }


}