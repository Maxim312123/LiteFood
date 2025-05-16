package com.diplomaproject.litefood.fragments

import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import com.diplomaproject.litefood.R
import com.diplomaproject.litefood.activities.MainActivity
import com.diplomaproject.litefood.data.Address
import com.diplomaproject.litefood.databinding.FragmentMapsBinding
import com.diplomaproject.litefood.repository.FirebaseRealtimeDatabaseRepository
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.appbar.MaterialToolbar

private const val ARG_USER = "User"
private const val ARG_LATITUDE = "Latitude"
private const val ARG_LONGITUDE = "Longitude"

private const val VALUE_FOR_ZOOM = 15f

class MapsFragment : Fragment(), MenuProvider {

    private lateinit var binding: FragmentMapsBinding

    private var currentLatitude: Double? = null
    private var currentLongitude: Double? = null
    private lateinit var toolbar: MaterialToolbar
    private lateinit var onMapReadyCallback: OnMapReadyCallback
    private lateinit var googleMap: GoogleMap
    private lateinit var etAddress: EditText
    private lateinit var etApartmentNumber: EditText
    private lateinit var etComment: EditText
    private lateinit var btnSubmit: Button
    private lateinit var realtimeDatabaseManager: FirebaseRealtimeDatabaseRepository
    //   private lateinit var user: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { arguments ->
            // user = arguments.getParcelable(ARG_USER)!!
            currentLatitude = arguments.getDouble(ARG_LATITUDE)
            currentLongitude = arguments.getDouble(ARG_LONGITUDE)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMapsBinding.inflate(inflater, container, false)
        requireActivity().addMenuProvider(this, viewLifecycleOwner)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        initViews()
        initMap()
        setupViewListeners()
    }

    private fun init() {
        realtimeDatabaseManager = FirebaseRealtimeDatabaseRepository()
    }

    private fun initViews() {
        toolbar = binding.toolbar
        initToolbar()

        etAddress = binding.editTextAddress
        etApartmentNumber = binding.editTextApartmentNumber
        etComment = binding.editTextComment
        btnSubmit = binding.buttonSubmit
    }

    private fun initToolbar() {
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        (activity as AppCompatActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
        }
    }

    private fun initMap() {
        onMapReadyCallback = OnMapReadyCallback { mGoogle ->
            googleMap = mGoogle
            setCurrentUserLocation()

            googleMap.setOnMapClickListener { latLng ->
                setNewUserLocation(latLng)
            }

        }

        val mapFragment =
            childFragmentManager.findFragmentById(R.id.fragmentMap) as SupportMapFragment?
        mapFragment?.getMapAsync(onMapReadyCallback)
    }

    private fun setCurrentUserLocation() {
        val currentLocation = LatLng(currentLatitude!!, currentLongitude!!)
        googleMap.addMarker(
            MarkerOptions().position(currentLocation).title("Твоя текущая позиция")
        )
        googleMap.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                currentLocation,
                VALUE_FOR_ZOOM
            )
        )

        updateTextViewAddress()
    }

    private fun setNewUserLocation(latLng: LatLng) {
        googleMap.clear()

        googleMap.addMarker(MarkerOptions().position(latLng))

        googleMap.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                latLng,
                VALUE_FOR_ZOOM
            )
        )

        currentLatitude = latLng.latitude
        currentLongitude = latLng.longitude
        updateTextViewAddress()
    }


    private fun updateTextViewAddress() {
        val address = getAddressFromCoordinates()
        if (address != null) {
            val city = address.city
            val street = address.street
            val houseNumber = address.houseNumber

            if (city.isNotEmpty() && street.isNotEmpty()) {
                etAddress.setText("$city, $street, $houseNumber")
            } else {
                etAddress.text.clear()
            }
        }
    }

    private fun getAddressFromCoordinates(): Address? {
        val geocoder = Geocoder(requireActivity() as MainActivity)
        val addresses = geocoder.getFromLocation(currentLatitude!!, currentLongitude!!, 1)
        if (!addresses.isNullOrEmpty()) {
            val receivedAddress = addresses[0]

            val city = receivedAddress.locality ?: ""
            val street = receivedAddress.thoroughfare ?: ""
            val houseNumber = receivedAddress.subThoroughfare ?: ""
            //val apartmentNumber = etApartmentNumber.text.toString().toInt()
            //val comment = etComment.text.toString()

            val address = Address(city, street, houseNumber)

            return address
        }
        return null
    }

    private fun setupViewListeners() {
        binding.imageButtonZoomIn.setOnClickListener {
            zoomIn()
        }

        binding.imageButtonZoomOut.setOnClickListener {
            zoomOut()
        }

        btnSubmit.setOnClickListener {
            val address = getAddressFromCoordinates()
            if (address != null) {
                val city = address.city
                val street = address.street
                val houseNumber = address.houseNumber

                if (city.isNotEmpty() && street.isNotEmpty()) {
                    realtimeDatabaseManager.saveUserAddress(address)
                    parentFragmentManager.popBackStackImmediate("AddressFragment", 0);
                }
            }

        }
    }

    private fun zoomIn() {
        val currentZoom = googleMap.cameraPosition.zoom
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(currentZoom + 1))
    }

    private fun zoomOut() {
        val currentZoom = googleMap.cameraPosition.zoom
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(currentZoom - 1))
    }

    override fun onCreateMenu(p0: Menu, p1: MenuInflater) {}

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            android.R.id.home -> {
                parentFragmentManager.popBackStack()
                true
            }

            else -> super.onOptionsItemSelected(menuItem)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(currentLatitude: Double, currentLongitude: Double) =
            MapsFragment().apply {
                arguments = Bundle().apply {
                    //putParcelable(ARG_USER, user)
                    putDouble(ARG_LATITUDE, currentLatitude)
                    putDouble(ARG_LONGITUDE, currentLongitude)
                }
            }
    }

}