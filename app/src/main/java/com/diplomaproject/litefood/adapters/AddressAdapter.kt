package com.diplomaproject.litefood.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.diplomaproject.litefood.R
import com.diplomaproject.litefood.data.Address
import com.diplomaproject.litefood.databinding.ItemAddressBinding

class AddressAdapter(
    private var addresses: MutableList<Address>,
    private val onItemClickListener: OnItemClickListener
) : RecyclerView.Adapter<AddressAdapter.AddressViewHolder>() {

    inner class AddressViewHolder(itemView: View) : ViewHolder(itemView) {
        val binding = ItemAddressBinding.bind(itemView)
        val overFlowMenu = binding.ibOverFlowMenu


        @SuppressLint("SetTextI18n")
        fun bind(address: Address) {
            binding.apply {
                textViewAddress.setText("${address.city}, ${address.street}, ${address.houseNumber}")
            }
        }

    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, position: Int): AddressViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_address, viewGroup, false)
        return AddressViewHolder(view)
    }

    override fun onBindViewHolder(holder: AddressViewHolder, position: Int) {
        val address = addresses[position]
        holder.bind(address)

        holder.overFlowMenu.setOnClickListener { view ->
            showPopupMenu(view, position)
        }
    }

    override fun getItemCount(): Int {
        return addresses.size
    }

    private fun showPopupMenu(view: View, position: Int) {
        val popupMenu = PopupMenu(view.context, view)

        popupMenu.menuInflater.inflate(R.menu.popup_menu_addresses, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {
            override fun onMenuItemClick(menuItem: MenuItem?): Boolean {
                val itemId = menuItem?.itemId
                when (itemId) {
                    R.id.delete_address -> {
                        onItemClickListener.onItemClick(position)
                        return true
                    }
                }
                return false
            }

        })

        popupMenu.show()

    }

    fun deleteAddress(position: Int) {
        addresses.removeAt(position)
        notifyItemChanged(position)
    }

    fun getAddress(position: Int): Address {
        return addresses[position]
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }
}