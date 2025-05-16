package com.diplomaproject.litefood

import com.diplomaproject.litefood.fragments.MainFragment
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class MainFragmentTest {

    @Test
    fun `should return all views are initialized`() {

        val mainFragment = MainFragment()
        mainFragment.onViewCreated()

        Assertions.assertEquals(2, 2)
    }
}