package com.diplomaproject.litefood.fragments

import android.annotation.SuppressLint
import android.location.Geocoder
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import com.diplomaproject.litefood.R
import com.diplomaproject.litefood.data.Address
import com.diplomaproject.litefood.databinding.FragmentMapsBinding
import com.diplomaproject.litefood.repository.FirebaseRealtimeDatabaseRepository
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.appbar.MaterialToolbar
import java.util.Locale

private const val ARG_USER = "User"
private const val ARG_LATITUDE = "Latitude"
private const val ARG_LONGITUDE = "Longitude"

private const val VALUE_FOR_ZOOM = 15f

class MapsFragment : Fragment(), MenuProvider {

    private lateinit var binding: FragmentMapsBinding

    private lateinit var currentUserLatLng: LatLng

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
    private val belarusBounds = LatLngBounds(
        LatLng(51.0, 23.2),
        LatLng(55.5, 30.8)
    )

    private var isFullAddress = MutableLiveData<Boolean>(false)
    //   private lateinit var user: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { arguments ->
            // user = arguments.getParcelable(ARG_USER)!!
            currentLatitude = arguments.getDouble(ARG_LATITUDE)
            currentLongitude = arguments.getDouble(ARG_LONGITUDE)

            currentUserLatLng = LatLng(currentLatitude!!, currentLongitude!!)
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
        setupObservers()
        etAddress.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                var input = s.toString()

                if (input.isEmpty()) {
                    changeSubmitButtonState(false)
                    binding.tvAddressError.visibility = View.VISIBLE
                } else {
                    val regex = Regex("\\d+")

                    val strAddress = etAddress.text.toString()

                    val number = regex.find(strAddress)

                    if (number == null) {
                        changeSubmitButtonState(false)
                        binding.tvAddressError.visibility = View.VISIBLE
                    } else {
                        changeSubmitButtonState(true)
                        binding.tvAddressError.visibility = View.GONE
                    }
                }
            }

        })
    }

    @SuppressLint("ResourceAsColor")
    private fun setupObservers() {
        isFullAddress.observe(viewLifecycleOwner) {
            changeSubmitButtonState(it)
        }
    }

    private fun changeSubmitButtonState(isEnabled: Boolean) {
        btnSubmit.isEnabled = isEnabled

        if (isEnabled) {
            btnSubmit.setBackgroundResource(R.drawable.background_normal_button)
        } else {
            btnSubmit.setBackgroundResource(R.drawable.bgd_btn_disable)
        }
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
            googleMap.setMinZoomPreference(6.0f)
            googleMap.setMaxZoomPreference(20.0f)
            googleMap.setLatLngBoundsForCameraTarget(belarusBounds)
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
            ),
        )

        updateTextViewAddress()
    }

    private fun setNewUserLocation(latLng: LatLng) {
        if (isWithinBelarus(latLng)) {
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
        } else {
            Toast.makeText(
                requireActivity(),
                "Нельзя поставить маркер за границами Беларуси",
                Toast.LENGTH_SHORT
            ).show()
        }
    }


    private fun updateTextViewAddress() {
        val address = getAddressFromCoordinates()
        if (address != null) {
            val city = address.city
            val street = address.street
            val houseNumber = address.houseNumber

            if (houseNumber.isEmpty()) {
                binding.tvAddressError.visibility = View.VISIBLE
                isFullAddress.value = false
            } else {
                binding.tvAddressError.visibility = View.GONE
                isFullAddress.value = true
            }

            if (city.isNotEmpty() && street.isNotEmpty()) {
                etAddress.setText("$city, $street, $houseNumber")
            } else {
                etAddress.text.clear()
                binding.tvAddressError.visibility = View.VISIBLE
                isFullAddress.value = false
            }
        }
    }

    private fun getAddressFromCoordinates(): Address? {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        val addresses = geocoder.getFromLocation(currentLatitude!!, currentLongitude!!, 1)
        if (!addresses.isNullOrEmpty()) {
            val receivedAddress = addresses[0]

            val city = receivedAddress.locality ?: ""
            val street = receivedAddress.thoroughfare ?: ""
            var houseNumber = receivedAddress.subThoroughfare ?: ""

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
                var houseNumber = address.houseNumber

                if (houseNumber.isEmpty()) {
                    val regex = Regex("\\d+")

                    val strAddress = etAddress.text.toString()

                    val number = regex.find(strAddress)
                    address.houseNumber = number?.value ?: ""
                }

                if (city.isNotEmpty() && street.isNotEmpty()) {
                    realtimeDatabaseManager.saveUserAddress(address)
                    parentFragmentManager.popBackStackImmediate("AddressFragment", 0);
                }
            }
        }

        binding.ibUserCurrentLocation.setOnClickListener {


            googleMap.clear()

            googleMap.addMarker(MarkerOptions().position(currentUserLatLng))

            googleMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    currentUserLatLng,
                    VALUE_FOR_ZOOM
                )
            )


            val geocoder = Geocoder(requireContext(), Locale.getDefault())
            val addresses = geocoder.getFromLocation(currentUserLatLng.latitude, currentUserLatLng.longitude, 1)

            if (!addresses.isNullOrEmpty()) {
                val receivedAddress = addresses[0]

                val city = receivedAddress.locality ?: ""
                val street = receivedAddress.thoroughfare ?: ""
                var houseNumber = receivedAddress.subThoroughfare ?: ""

                val address = Address(city, street, houseNumber)

                if (address != null) {
                    val city = address.city
                    val street = address.street
                    val houseNumber = address.houseNumber

                    if (houseNumber.isEmpty()) {
                        binding.tvAddressError.visibility = View.VISIBLE
                        isFullAddress.value = false
                    } else {
                        binding.tvAddressError.visibility = View.GONE
                        isFullAddress.value = true
                    }

                    if (city.isNotEmpty() && street.isNotEmpty()) {
                        etAddress.setText("$city, $street, $houseNumber")
                    } else {
                        etAddress.text.clear()
                        binding.tvAddressError.visibility = View.VISIBLE
                        isFullAddress.value = false
                    }
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

    fun isWithinBelarus(latLng: LatLng): Boolean {
        return belarusBounds.contains(latLng)
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