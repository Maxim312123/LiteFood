package com.diplomaproject.litefood.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.MenuProvider
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.diplomaproject.litefood.FirebaseService
import com.diplomaproject.litefood.FoodSections
import com.diplomaproject.litefood.R
import com.diplomaproject.litefood.adapters.CarouselProductAdapter
import com.diplomaproject.litefood.adapters.FoodSectionAdapter
import com.diplomaproject.litefood.data.Product
import com.diplomaproject.litefood.databinding.FragmentMainBinding
import com.diplomaproject.litefood.fragments.view_models.MainFragmentViewModel
import com.diplomaproject.litefood.repository.FirestoreDatabaseRepository
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.carousel.CarouselSnapHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class MainFragment : Fragment(), MenuProvider {
    private lateinit var binding: FragmentMainBinding

    private var toolbar: MaterialToolbar? = null

    private lateinit var rvFoodSections: RecyclerView
    private lateinit var rvSalesLeaderProducts: RecyclerView
    private lateinit var rvVegetarianProducts: RecyclerView
    private lateinit var rvSpicyProducts: RecyclerView
    private lateinit var foodSectionAdapter: FoodSectionAdapter
    private lateinit var salesLeaderCarouselProductAdapter: CarouselProductAdapter
    private lateinit var vegetarianCarouselProductAdapter: CarouselProductAdapter
    private lateinit var spicyCarouselProductAdapter: CarouselProductAdapter

    private val firestoreDatabaseRepository: FirestoreDatabaseRepository by lazy {
        FirebaseService.firestoreDatabaseRepository
    }

    private var completingCoroutines: MutableList<Job> = mutableListOf()

    private val viewModel: MainFragmentViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val fetchFoodSectionsJob = lifecycleScope.launch {
            viewModel.fetchFoodSections()
        }

        val fetchSalesLeaderCarouselProductsJob = lifecycleScope.launch {
            viewModel.fetchSalesLeaderCarouselProducts()
        }

        val fetchVegetarianCarouselProductsJob = lifecycleScope.launch {
            viewModel.fetchVegetarianCarouselProducts()
        }

        val fetchSpicyCarouselProductsJob = lifecycleScope.launch {
            viewModel.fetchSpicyCarouselProducts()
        }

        completingCoroutines.add(fetchFoodSectionsJob)
        completingCoroutines.add(fetchSalesLeaderCarouselProductsJob)
        completingCoroutines.add(fetchVegetarianCarouselProductsJob)
        completingCoroutines.add(fetchSpicyCarouselProductsJob)

        fetchFoodSectionsJob.invokeOnCompletion {
            removeCompletingCoroutine(fetchFoodSectionsJob)
        }
        fetchSalesLeaderCarouselProductsJob.invokeOnCompletion {
            removeCompletingCoroutine(fetchSalesLeaderCarouselProductsJob)
        }

        fetchVegetarianCarouselProductsJob.invokeOnCompletion {
            removeCompletingCoroutine(fetchVegetarianCarouselProductsJob)
        }

        fetchSpicyCarouselProductsJob.invokeOnCompletion {
            removeCompletingCoroutine(fetchSpicyCarouselProductsJob)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        requireActivity().addMenuProvider(this, viewLifecycleOwner)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        initViews()
        setupViewObserves()
    }

    private fun setupViewObserves() {
        viewModel.foodSections.observe(viewLifecycleOwner) { foodSections ->
            foodSectionAdapter =
                FoodSectionAdapter(foodSections.asReversed(), viewModel)
            rvFoodSections.adapter = foodSectionAdapter
        }

        viewModel.selectedFoodSectionPosition.observe(viewLifecycleOwner) { position ->
            if (position != -1) {
                val foodSectionName = FoodSections.getFoodSectionNameByNumber(position)
                val toolbarTitle =
                    resources.getStringArray(R.array.toolbar_title_food_section_name).get(position)

                loadFoodSectionsProducts(foodSectionName, toolbarTitle)
                viewModel.onFoodSectionClicked(-1)
            }
        }

        viewModel.salesLeaderProducts.observe(viewLifecycleOwner) { salesLeaderProducts ->
            salesLeaderCarouselProductAdapter =
                CarouselProductAdapter(viewModel, salesLeaderProducts)
            rvSalesLeaderProducts.adapter = salesLeaderCarouselProductAdapter
        }

        viewModel.clickedSalesLeaderProductPosition.observe(viewLifecycleOwner) { position ->
            if (position != -1) {
                val productRef = salesLeaderCarouselProductAdapter.getProduct(position).productRef

                val job = viewLifecycleOwner.lifecycleScope.launch {
                    viewModel.fetchCarouselProductData(productRef)
                }

                completingCoroutines.add(job)
                job.invokeOnCompletion {
                    removeCompletingCoroutine(job)
                }

                viewModel.onSalesLeaderProductClicked(-1)
            }
        }

        viewModel.carouselProduct.observe(viewLifecycleOwner) { product ->
            if (product != null) {
                parentFragmentManager.beginTransaction()
                    .replace(
                        R.id.fragment_container,
                        ProductDescriptionFragment.newInstance(product)
                    )
                    .addToBackStack(null)
                    .commit()
                viewModel.onFetchedCarouselProductData()
            }
        }


        viewModel.vegetarianProducts.observe(viewLifecycleOwner) { vegetarianProducts ->
            vegetarianCarouselProductAdapter =
                CarouselProductAdapter(viewModel, vegetarianProducts)
            rvVegetarianProducts.adapter = vegetarianCarouselProductAdapter
        }

        viewModel.clickedVegetarianProductPosition.observe(viewLifecycleOwner) { position ->
            if (position != -1) {
                val productRef = vegetarianCarouselProductAdapter.getProduct(position).productRef

                val job = viewLifecycleOwner.lifecycleScope.launch {
                    viewModel.fetchCarouselProductData(productRef)
                }

                completingCoroutines.add(job)
                job.invokeOnCompletion {
                    removeCompletingCoroutine(job)
                }

                viewModel.onVegetarianProductClicked(-1)
            }
        }

        viewModel.clickedSpicyProductPosition.observe(viewLifecycleOwner) { position ->
            if (position != -1) {
                val productRef = spicyCarouselProductAdapter.getProduct(position).productRef

                val job = viewLifecycleOwner.lifecycleScope.launch {
                    viewModel.fetchCarouselProductData(productRef)
                }

                completingCoroutines.add(job)
                job.invokeOnCompletion {
                    removeCompletingCoroutine(job)
                }

                viewModel.onSpicyProductClicked(-1)
            }
        }

        viewModel.spicyProducts.observe(viewLifecycleOwner) { spicyProducts ->
            spicyCarouselProductAdapter =
                CarouselProductAdapter(viewModel, spicyProducts)
            rvSpicyProducts.adapter = spicyCarouselProductAdapter
        }
    }

    private fun setupToolbar() {
        toolbar = binding!!.materialToolbar
        (activity as AppCompatActivity?)!!.setSupportActionBar(toolbar)
    }

    @SuppressLint("RestrictedApi", "WrongConstant")
    private fun initViews() {
        rvFoodSections = binding.rvFoodSections!!

        rvSalesLeaderProducts = binding.rvHitSales!!
        rvVegetarianProducts = binding.rvVegetarianProducts!!
        rvSpicyProducts = binding.rvSpicyProducts!!

        val carouselSnapHelper = CarouselSnapHelper()
        carouselSnapHelper.attachToRecyclerView(rvSalesLeaderProducts)

    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        requireActivity().menuInflater.inflate(R.menu.toolbar_main_fragment, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        val id = menuItem.itemId

        if (id == android.R.id.home) {
            val drawerLayout = requireActivity().findViewById<DrawerLayout>(R.id.drawerLayout)
            drawerLayout.openDrawer(GravityCompat.START)
            return true
        } else if (id == R.id.search) {
            return true
        }
        return false
    }

    private fun loadFoodSectionsProducts(
        foodSectionName: String,
        toolbarTitle: String
    ) {
        var job = viewLifecycleOwner.lifecycleScope.launch {
            val products = firestoreDatabaseRepository.fetchFoodSectionProducts(foodSectionName)

            val shuffledProducts = products.shuffled() as MutableList<Product>
            parentFragmentManager.beginTransaction().replace(
                R.id.fragment_container,
                ProductFragment.newInstance(toolbarTitle, shuffledProducts)
            ).addToBackStack(null).commit()
        }
        completingCoroutines.add(job)
        job.invokeOnCompletion {
            removeCompletingCoroutine(job)
        }
    }

    override fun onStop() {
        super.onStop()
        completingCoroutines.forEach {
            it.cancel()
        }
        completingCoroutines.clear()
    }

    private fun removeCompletingCoroutine(job: Job) {
        completingCoroutines.remove(job)
    }
}