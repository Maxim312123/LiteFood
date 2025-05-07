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
import com.diplomaproject.litefood.R
import com.diplomaproject.litefood.UserViewModel
import com.diplomaproject.litefood.activities.MainActivity
import com.diplomaproject.litefood.data.User
import com.diplomaproject.litefood.databinding.FragmentAddressBinding
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView

class AddressFragment : Fragment(), MenuProvider {

    private lateinit var binding: FragmentAddressBinding

    private lateinit var toolbar: MaterialToolbar

    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val userViewModel = UserViewModel()
        userViewModel.user.observe(
            this
        ) { user: User? ->
            if (user != null) {
                if (user.addresses != null) {
                    childFragmentManager.beginTransaction()
                        .replace(
                            R.id.fragment_container_addresses,
                            FullAddressesFragment.newInstance(user!!)
                        )
                        .commit()
                } else {
                    childFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container_addresses, EmptyAddressesFragment())
                        .commit()
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddressBinding.inflate(inflater, container, false)
        requireActivity().addMenuProvider(this, viewLifecycleOwner)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        setupActionBar()
    }

    private fun setupActionBar() {
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        (activity as AppCompatActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
        }
    }

    private fun initViews() {
        toolbar = binding.toolbar
        bottomNavigationView = (activity as MainActivity).findViewById(R.id.bottomNavigationView)
    }


    companion object {
        fun newInstance() = AddressFragment()
    }

    override fun onCreateMenu(p0: Menu, p1: MenuInflater) {}

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            android.R.id.home -> {
                parentFragmentManager.popBackStack()
                true
            }

            else -> {
                false
            }
        }
    }

}