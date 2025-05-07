package com.diplomaproject.litefood

enum class FoodSections(val number: Int, val title: String) {
    VEGAN(0, "vegan"), SPICY(1, "spicy"), PORK(2, "pork"),
    FASTFOOD(3, "fastfood"), DRINKS(4, "drinks"), DESSERTS(5, "desserts"),
    CHUCKEN(6, "chicken"), BEEF(7, "beef");

    companion object {
        fun getFoodSectionNameByNumber(number: Int): String {
            val foodSections = FoodSections.values()
            val foodSection = foodSections.find { it.number == number }
            return foodSection!!.title
        }
    }
}