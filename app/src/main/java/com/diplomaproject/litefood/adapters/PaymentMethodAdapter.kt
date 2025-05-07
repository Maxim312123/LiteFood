package com.diplomaproject.litefood.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.diplomaproject.litefood.R
import com.diplomaproject.litefood.databinding.HeaderPaymentMethodBinding
import com.diplomaproject.litefood.databinding.ItemPaymentMethodBinding


class PaymentMethodAdapter(
    var paymentMethodList: MutableList<HashMap<String, Any>?>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private companion object {
        const val TYPE_HEADER: Int = 0
        const val TYPE_ITEM: Int = 1
    }

    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = HeaderPaymentMethodBinding.bind(itemView)
        val tvCardLast4Digits = binding.tvNumberCreditCard
        val ivPaymentSystem = binding.ivPaymentSystem
    }

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ItemPaymentMethodBinding.bind(itemView)
        val tvCardLast4Digits = binding.tvNumberCreditCard
        val ivPaymentSystem = binding.ivPaymentSystem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == TYPE_ITEM) {
            val view =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_payment_method, parent, false)
            return ItemViewHolder(view)
        } else if (viewType == TYPE_HEADER) {
            val view =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.header_payment_method, parent, false)
            return HeaderViewHolder(view)
        } else {
            throw RuntimeException("There is no type that matches the type $viewType. Make sure you're using types correctly.")
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> {
                val mainPaymentMethod =
                    paymentMethodList.find { it?.get("isMainPaymentMethod") == true }

                if (mainPaymentMethod != null) {
                    val cardLast4Digits = mainPaymentMethod["last4Digits"].toString()
                    val cardBrand = mainPaymentMethod["cardBrand"]

                    holder.tvCardLast4Digits.text = "**** $cardLast4Digits"
                    if (cardBrand == "Visa") {
                        holder.ivPaymentSystem.setImageResource(R.drawable.visa24)
                    } else {
                        holder.ivPaymentSystem.setImageResource(R.drawable.mastercard24)
                    }
                }
            }

            is ItemViewHolder -> {
                val cardInfo =
                    paymentMethodList[position - 1] // Сдвинем на единицу для доступа к элементам списка
                if (cardInfo != null) {
                    val cardLast4Digits = cardInfo["last4Digits"].toString()
                    val cardBrand = cardInfo["cardBrand"]

                    holder.tvCardLast4Digits.text = "**** $cardLast4Digits"
                    if (cardBrand == "Visa") {
                        holder.ivPaymentSystem.setImageResource(R.drawable.visa24)
                    } else {
                        holder.ivPaymentSystem.setImageResource(R.drawable.mastercard24)
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return paymentMethodList.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) TYPE_HEADER else TYPE_ITEM
    }
}