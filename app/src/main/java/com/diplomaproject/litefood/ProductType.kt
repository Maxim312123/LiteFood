package com.diplomaproject.litefood

enum class ProductType(val productType: String, val number: Int) {
    BURGERS("burgers", 0), COFFEE("coffee", 1), DESSERTS("desserts", 2),
    KEBAB("kebab", 3), DRINKS("drinks", 4), FRENCH_FRIES("french fries", 5),
    HOT_DOGS("hot dogs", 6), PIZZA("pizza", 7), SANDWICHES("sandwiches", 8),
    SOUPS("soups", 9), SUSHI("sushi", 10), TEA("tea", 11);

    companion object{
        fun getCategoryNameByNumber(number: Int): String? {
            val categories = entries.toTypedArray()
            return categories.find { it.number == number }?.productType
        }
    }
}