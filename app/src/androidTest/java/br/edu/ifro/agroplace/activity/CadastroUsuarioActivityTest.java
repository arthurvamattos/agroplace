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
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

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
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CadastroUsuarioActivityTest {

    @Rule
    public ActivityTestRule<CadastroUsuarioActivity> mActivityTestRule = new ActivityTestRule<>(CadastroUsuarioActivity.class);

    ViewInteraction nameInputEditText = onView(
            allOf(withId(R.id.cadastro_usuario_nome),
                    childAtPosition(
                            childAtPosition(
                                    withClassName(is("com.google.android.material.textfield.TextInputLayout")),
                                    0),
                            0),
                    isDisplayed()));
    ViewInteraction emailInputEditText = onView(
            allOf(withId(R.id.cadastro_usuario_email),
                    childAtPosition(
                            childAtPosition(
                                    withClassName(is("com.google.android.material.textfield.TextInputLayout")),
                                    0),
                            0),
                    isDisplayed()));


    ViewInteraction passInputEditText = onView(
            allOf(withId(R.id.cadastro_usuario_password),
                    childAtPosition(
                            childAtPosition(
                                    withClassName(is("com.google.android.material.textfield.TextInputLayout")),
                                    0),
                            0),
                    isDisplayed()));

    ViewInteraction pass2InputEditText = onView(
            allOf(withId(R.id.cadastro_usuario_oonfirm_password),
                    childAtPosition(
                            childAtPosition(
                                    withClassName(is("com.google.android.material.textfield.TextInputLayout")),
                                    0),
                            0),
                    isDisplayed()));

    ViewInteraction appCompatButton = onView(withId(R.id.cadastro_usuario_btn));


    ViewInteraction viewPager = onView(
            allOf(withId(R.id.vp_main),
                    childAtPosition(
                            allOf(withId(R.id.main_id),
                                    childAtPosition(
                                            withId(android.R.id.content),
                                            0)),
                            2),
                    isDisplayed()));

    @Test
    public void A_emailInUseTest() {
        nameInputEditText.perform(replaceText("Auth Test"), closeSoftKeyboard());
        emailInputEditText.perform(replaceText("auth@test.com"), closeSoftKeyboard());
        passInputEditText.perform(replaceText("senha1234"), closeSoftKeyboard());
        pass2InputEditText.perform(replaceText("senha1234"), closeSoftKeyboard());
        appCompatButton.perform(scrollTo(), click());
        onView(isRoot()).perform(waitFor(1500));
        onView(withId(com.google.android.material.R.id.snackbar_text))
                .check(matches(withText("O e-mail informado já está em uso!")));
    }

    @Test
    public void B_invalidEmail() {
        nameInputEditText.perform(replaceText("Auth Test"), closeSoftKeyboard());
        emailInputEditText.perform(replaceText("authtest.com"), closeSoftKeyboard());
        passInputEditText.perform(replaceText("senha1234"), closeSoftKeyboard());
        pass2InputEditText.perform(replaceText("senha1234"), closeSoftKeyboard());
        appCompatButton.perform(scrollTo(), click());
        onView(isRoot()).perform(waitFor(1500));
        onView(withId(com.google.android.material.R.id.snackbar_text))
                .check(matches(withText("Por favor digite um e-mail válido!")));
    }

    @Test
    public void C_weekPasswords() {
        nameInputEditText.perform(replaceText("Auth Test"), closeSoftKeyboard());
        emailInputEditText.perform(replaceText("auth@test.com"), closeSoftKeyboard());
        passInputEditText.perform(replaceText("s"), closeSoftKeyboard());
        pass2InputEditText.perform(replaceText("s"), closeSoftKeyboard());
        appCompatButton.perform(scrollTo(), click());
        onView(isRoot()).perform(waitFor(1500));
        onView(withId(com.google.android.material.R.id.snackbar_text))
                .check(matches(withText("Por favor digite uma senha mais forte!")));
    }

    @Test
    public void D_passwordsMustBeTheSame() {
        nameInputEditText.perform(replaceText("Auth Test"), closeSoftKeyboard());
        emailInputEditText.perform(replaceText("auth@test.com"), closeSoftKeyboard());
        passInputEditText.perform(replaceText("senha1234"), closeSoftKeyboard());
        pass2InputEditText.perform(replaceText("senha1235"), closeSoftKeyboard());
        appCompatButton.perform(scrollTo(), click());
        onView(withId(com.google.android.material.R.id.snackbar_text))
                .check(matches(withText("As senhas informadas são diferentes!")));
    }

    @Test
    public void E_allRequiredFieldsMustBeFilleds() {
        nameInputEditText.perform(replaceText("Auth Test"), closeSoftKeyboard());
        emailInputEditText.perform(replaceText("auth@test.com"), closeSoftKeyboard());
        passInputEditText.perform(replaceText("senha1234"), closeSoftKeyboard());
        pass2InputEditText.perform(replaceText(""), closeSoftKeyboard());
        appCompatButton.perform(scrollTo(), click());
        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText("Por favor informe todos os campos!")));


        nameInputEditText.perform(replaceText("Auth Test"), closeSoftKeyboard());
        emailInputEditText.perform(replaceText("auth@test.com"), closeSoftKeyboard());
        passInputEditText.perform(replaceText(""), closeSoftKeyboard());
        pass2InputEditText.perform(replaceText("senha1234"), closeSoftKeyboard());
        appCompatButton.perform(scrollTo(), click());
        onView(withId(com.google.android.material.R.id.snackbar_text))
                .check(matches(withText("Por favor informe todos os campos!")));

        nameInputEditText.perform(replaceText("Auth Test"), closeSoftKeyboard());
        emailInputEditText.perform(replaceText(""), closeSoftKeyboard());
        passInputEditText.perform(replaceText("senha1234"), closeSoftKeyboard());
        pass2InputEditText.perform(replaceText("senha1234"), closeSoftKeyboard());
        appCompatButton.perform(scrollTo(), click());
        onView(withId(com.google.android.material.R.id.snackbar_text))
                .check(matches(withText("Por favor informe todos os campos!")));

        nameInputEditText.perform(replaceText(""), closeSoftKeyboard());
        emailInputEditText.perform(replaceText("auth@test.com"), closeSoftKeyboard());
        passInputEditText.perform(replaceText("senha1234"), closeSoftKeyboard());
        pass2InputEditText.perform(replaceText("senha1234"), closeSoftKeyboard());
        appCompatButton.perform(scrollTo(), click());
        onView(withId(com.google.android.material.R.id.snackbar_text))
                .check(matches(withText("Por favor informe todos os campos!")));
    }

    @Test
    public void F_cadastroUsuarioActivityTest() {
        nameInputEditText.perform(replaceText("Auth Test"), closeSoftKeyboard());
        emailInputEditText.perform(replaceText("newauth@test.com"), closeSoftKeyboard());
        passInputEditText.perform(replaceText("senha1234"), closeSoftKeyboard());
        pass2InputEditText.perform(replaceText("senha1234"), closeSoftKeyboard());
        appCompatButton.perform(scrollTo(), click());
        onView(isRoot()).perform(waitFor(5000));
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
