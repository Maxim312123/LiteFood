package com.diplomaproject.litefood.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.diplomaproject.litefood.R
import com.diplomaproject.litefood.databinding.FragmentEmptyAddressesBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomnavigation.BottomNavigationView


class EmptyAddressesFragment : Fragment() {

    private lateinit var permissionsLauncher: ActivityResultLauncher<Array<String>>

    private lateinit var binding: FragmentEmptyAddressesBinding
    private lateinit var btnAddAddress: Button
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var mapsFragment: MapsFragment

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEmptyAddressesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        registerPermissionListener()
        fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())
        setupViewListeners()
    }

    private fun registerPermissionListener() {
        permissionsLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true || permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
                getLastLocation()
            } else {
                Toast.makeText(requireActivity(), "Разрешение отклонено", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val bottomNavView =
            requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        if (bottomNavView.visibility == View.GONE) {
            bottomNavView.visibility = View.VISIBLE
        }
    }

    private fun initViews() {
        btnAddAddress = binding.btnAddAddress
    }

    private fun setupViewListeners() {
        btnAddAddress.setOnClickListener {
            checkLocationPermission()
        }
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            getLastLocation()
        } else {
            permissionsLauncher.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude

                    mapsFragment = MapsFragment.newInstance(latitude, longitude)

                    (parentFragment as AddressFragment).parentFragmentManager.beginTransaction()
                        .addToBackStack("MapsFragment")
                        .replace(R.id.fragment_container, mapsFragment).commit()

                    requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigationView)
                        .visibility = View.GONE
                } else {
                    Toast.makeText(
                        requireActivity(),
                        "Местоположение не доступно",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

}