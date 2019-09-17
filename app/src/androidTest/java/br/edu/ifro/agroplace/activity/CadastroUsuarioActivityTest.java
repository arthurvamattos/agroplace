package br.edu.ifro.agroplace.activity;


import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

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
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import br.edu.ifro.agroplace.R;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class CadastroUsuarioActivityTest {

    @Rule
    public ActivityTestRule<CadastroUsuarioActivity> mActivityTestRule = new ActivityTestRule<>(CadastroUsuarioActivity.class);

    @Test
    public void loginActivityTest() {
        ViewInteraction nameInputEditText = onView(
                allOf(withId(R.id.cadastro_usuario_nome),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("com.google.android.material.textfield.TextInputLayout")),
                                        0),
                                0),
                        isDisplayed()));
        nameInputEditText.perform(replaceText("Auth Test"), closeSoftKeyboard());

        ViewInteraction emailInputEditText = onView(
                allOf(withId(R.id.cadastro_usuario_email),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("com.google.android.material.textfield.TextInputLayout")),
                                        0),
                                0),
                        isDisplayed()));
        emailInputEditText.perform(replaceText("auth@test.com"), closeSoftKeyboard());

        ViewInteraction passInputEditText = onView(
                allOf(withId(R.id.cadastro_usuario_password),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("com.google.android.material.textfield.TextInputLayout")),
                                        0),
                                0),
                        isDisplayed()));
        passInputEditText.perform(replaceText("senha1234"), closeSoftKeyboard());

        ViewInteraction pass2InputEditText = onView(
                allOf(withId(R.id.cadastro_usuario_oonfirm_password),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("com.google.android.material.textfield.TextInputLayout")),
                                        0),
                                0),
                        isDisplayed()));
        pass2InputEditText.perform(replaceText("senha1234"), closeSoftKeyboard());

        ViewInteraction appCompatButton = onView(
                allOf(withId(R.id.cadastro_usuario_btn), withText("CADASTRAR"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.cadastro_usuario_form_holder),
                                        1),
                                5)));
        appCompatButton.perform(scrollTo(), click());

        onView(isRoot()).perform(waitFor(5000));

        ViewInteraction viewPager = onView(
                allOf(withId(R.id.vp_main),
                        childAtPosition(
                                allOf(withId(R.id.main_id),
                                        childAtPosition(
                                                withId(android.R.id.content),
                                                0)),
                                2),
                        isDisplayed()));
        viewPager.check(matches(isDisplayed()));
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
