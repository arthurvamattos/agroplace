package br.edu.ifro.agroplace.activity;


import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.test.espresso.DataInteraction;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.core.IsInstanceOf;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import br.edu.ifro.agroplace.R;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.is;

@LargeTest
@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ProductPreviewTest {

    @Rule
    public ActivityTestRule<LoginActivity> mActivityTestRule = new ActivityTestRule<>(LoginActivity.class);

    @Test
    public void A_productPreviewTest() {
        // login
        ViewInteraction loginUsernameField = onView(withId(R.id.login_username));
        ViewInteraction loginPasswordField = onView(withId(R.id.login_password));
        ViewInteraction loginBtn = onView(withId(R.id.login_btn));

        loginUsernameField.perform(scrollTo(), replaceText("auth@test.com"), closeSoftKeyboard());
        loginPasswordField.perform(scrollTo(), replaceText("senha1234"), closeSoftKeyboard());
        loginBtn.perform(scrollTo(), click());

        onView(isRoot()).perform(waitFor(8000));

        // checking if the products are displayed
        ViewInteraction productContainer = onView(
                allOf(withId(R.id.product_container),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.products_recycler_view),
                                        0),
                                0),
                        isDisplayed()));
        productContainer.check(matches(isDisplayed()));
    }

    @Test
    public void B_myProductPreviewTest() {
        // checking if 'my products' are displayed
        ViewInteraction myProductsBtn = onView(withText("MEUS PRODUTOS"));
        ViewInteraction myProductContainer = onView(
                allOf(withId(R.id.product_container),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.products_recycler_view),
                                        0),
                                0),
                        isDisplayed())
        );

        myProductsBtn.perform(click());
        myProductContainer.check(matches(isDisplayed()));
    }
    @Test
    public void C_searchTest() {
        // search test
        ViewInteraction forSaleBtn = onView(withText("À VENDA"));
        ViewInteraction searchBtn = onView(withId(R.id.search_button));
        ViewInteraction searchField = onView(withId(R.id.search_src_text));
        ViewInteraction searchedProductContainer = onView(
                allOf(withId(R.id.product_container),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.products_recycler_view),
                                        0),
                                0),
                        isDisplayed())
        );
        ViewInteraction searchCloseBtn = onView(withId(R.id.search_close_btn));

        forSaleBtn.perform(click());
        searchBtn.perform(click());
        searchField.perform(replaceText("goiaba"), closeSoftKeyboard());

        onView(isRoot()).perform(waitFor(1000));

        searchedProductContainer.check(matches(isDisplayed()));
        searchCloseBtn.perform(click());
    }

    @Test
    public void D_categoryTest() {
        // checking the category filter
        ViewInteraction categorySpinner = onView(withId(R.id.categorias_spinner));
        categorySpinner.perform(click());

        DataInteraction appCompatCheckedTextView = onData(anything())
                .inAdapterView(childAtPosition(
                        withClassName(is("android.widget.PopupWindow$PopupBackgroundView")),
                        0))
                .atPosition(1);
        appCompatCheckedTextView.perform(click());


        onView(withId(com.google.android.material.R.id.snackbar_text))
                .check(matches(withText("Mostrando Frutas")));
    }

    @Test
    public void E_productDetailsTest(){
        ViewInteraction constraintLayout2 = onView(
                allOf(childAtPosition(
                        allOf(withId(R.id.products_recycler_view),
                                childAtPosition(
                                        withClassName(is("android.widget.FrameLayout")),
                                        1)),
                        0),
                        isDisplayed()));
        constraintLayout2.perform(click());

        onView(isRoot()).perform(waitFor(1500));

        ViewInteraction textView = onView(withId(R.id.produto_nome));
        textView.check(matches(isDisplayed()));
    }


    public static ViewAction waitFor(long delay) {
        return new ViewAction() {
            @Override public Matcher<View> getConstraints() {
                return ViewMatchers.isRoot();
            }

            @Override public String getDescription() {
                return "wait for " + delay + "milliseconds";
            }

            @Override public void perform(UiController uiController, View view) {
                uiController.loopMainThreadForAtLeast(delay);
            }
        };
    }

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }
}
