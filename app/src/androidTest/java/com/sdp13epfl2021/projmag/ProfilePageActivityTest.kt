package com.sdp13epfl2021.projmag


import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.sdp13epfl2021.projmag.activities.ProfilePageActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(androidx.test.ext.junit.runners.AndroidJUnit4::class)
class ProfilePageActivityTest {

    @get:Rule
    var activityRule: ActivityScenarioRule<ProfilePageActivity> =
        ActivityScenarioRule(ProfilePageActivity::class.java)

    @Test
    fun testChangingValues() {
        onView(withId(R.id.profile_lastname)).perform(scrollTo(),clearText(), typeText("lastName"))
        onView(withId(R.id.profile_firstname)).perform(scrollTo(),clearText(), typeText("firstName"))
        onView(withId(R.id.profile_age)).perform(scrollTo(),clearText(), typeText("21"))
        onView(withId(R.id.profile_genre)).perform(scrollTo(),clearText(), typeText("Male"))
        onView(withId(R.id.profile_phone_number)).perform(scrollTo(),clearText(), typeText("0101010100"))
    }


    @Test
    fun changeProfileValuesAndSumbmit() {
        onView(withId(R.id.buttonSubChangeProfil)).perform(scrollTo())
        Thread.sleep(10000)
        onView(withId(R.id.buttonSubChangeProfil)).perform(click())


    }
}
