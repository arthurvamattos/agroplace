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
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.withDecorView;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

@LargeTest
@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ProfileTest {

    @Rule
    public ActivityTestRule<LoginActivity> mActivityTestRule = new ActivityTestRule<>(LoginActivity.class);

    @Test
    public void inputErrorsProfileTest() {
        ViewInteraction textInputEditText = onView(
                allOf(withId(R.id.login_username),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("com.google.android.material.textfield.TextInputLayout")),
                                        0),
                                0),
                        isDisplayed()));
        textInputEditText.perform(replaceText("auth@test.com"), closeSoftKeyboard());

        ViewInteraction textInputEditText2 = onView(
                allOf(withId(R.id.login_password),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("com.google.android.material.textfield.TextInputLayout")),
                                        0),
                                0),
                        isDisplayed()));
        textInputEditText2.perform(replaceText("senha1234"), closeSoftKeyboard());

        ViewInteraction appCompatButton = onView(withId(R.id.login_btn));
        appCompatButton.perform(click());

        onView(isRoot()).perform(waitFor(5000));

        ViewInteraction overflowMenuButton = onView(
                allOf(withContentDescription("More options"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.toolbar),
                                        1),
                                2),
                        isDisplayed()));
        overflowMenuButton.perform(click());

        ViewInteraction appCompatTextView = onView(
                allOf(withId(R.id.title), withText("Meu Perfil"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.content),
                                        0),
                                0),
                        isDisplayed()));
        appCompatTextView.perform(click());

        onView(isRoot()).perform(waitFor(500));

        ViewInteraction textInputEditTextUser = onView(
                allOf(withId(R.id.formulario_usuario_nome),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("com.google.android.material.textfield.TextInputLayout")),
                                        0),
                                0)));

        ViewInteraction textInputEditTextPass = onView(
                allOf(withId(R.id.formulario_usuario_password),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("com.google.android.material.textfield.TextInputLayout")),
                                        0),
                                0)));

        ViewInteraction textInputEditTextEmail = onView(
                allOf(withId(R.id.formulario_usuario_email),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("com.google.android.material.textfield.TextInputLayout")),
                                        0),
                                0)));

        ViewInteraction textInputEditTextConfirmPass = onView(
                allOf(withId(R.id.formulario_usuario_confirm_password),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("com.google.android.material.textfield.TextInputLayout")),
                                        0),
                                0)));
        ViewInteraction actionMenuItemView = onView(
                allOf(withId(R.id.menu_formulario_salvar), withContentDescription("Salvar"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.toolbar),
                                        2),
                                0)));


        // testar se todos os campos foram preenchidos
        textInputEditTextUser.perform(scrollTo(), replaceText(""), closeSoftKeyboard());
        textInputEditTextPass.perform(scrollTo(), replaceText("asd"), closeSoftKeyboard());
        textInputEditTextEmail.perform(scrollTo(), replaceText("auth@test.com"), closeSoftKeyboard());
        textInputEditTextConfirmPass.perform(scrollTo(), replaceText("d"), closeSoftKeyboard());
        actionMenuItemView.perform(scrollTo(), click());

        onView(withId(com.google.android.material.R.id.snackbar_text))
                .check(matches(withText("Por favor, preencha os campos")));


        onView(isRoot()).perform(waitFor(2000));


        // testar se as senhas são iguais
        textInputEditTextUser.perform(scrollTo(), replaceText("Errors Profile Test"), closeSoftKeyboard());
        textInputEditTextPass.perform(scrollTo(), replaceText("Errors Profile Test"), closeSoftKeyboard());
        textInputEditTextEmail.perform(scrollTo(), replaceText("auth@test.com"), closeSoftKeyboard());
        textInputEditTextConfirmPass.perform(scrollTo(), replaceText("Errors Profile Test Senha"), closeSoftKeyboard());
        actionMenuItemView.perform(scrollTo(), click());

        onView(withId(com.google.android.material.R.id.snackbar_text))
                .check(matches(withText("As senhas informadas devem ser iguais, se preferir deixe os campos em branco para manter a senha atual!")));

        onView(isRoot()).perform(waitFor(2000));
    }

    @Test
    public void profileTest() {
        ViewInteraction overflowMenuButton = onView(
                allOf(withContentDescription("More options"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.toolbar),
                                        1),
                                2),
                        isDisplayed()));
        overflowMenuButton.perform(click());

        ViewInteraction appCompatTextView = onView(
                allOf(withId(R.id.title), withText("Meu Perfil"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.content),
                                        0),
                                0),
                        isDisplayed()));
        appCompatTextView.perform(click());

        onView(isRoot()).perform(waitFor(500));

        ViewInteraction textInputEditTextUser = onView(
                allOf(withId(R.id.formulario_usuario_nome),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("com.google.android.material.textfield.TextInputLayout")),
                                        0),
                                0)));
        textInputEditTextUser.perform(scrollTo(), replaceText("Profile New Test"));
        textInputEditTextUser.perform(closeSoftKeyboard());

        ViewInteraction actionMenuItemView = onView(
                allOf(withId(R.id.menu_formulario_salvar), withContentDescription("Salvar"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.toolbar),
                                        2),
                                0)));
        actionMenuItemView.perform(scrollTo(), click());

        onView(isRoot()).perform(waitFor(500));

        onView(withId(com.google.android.material.R.id.snackbar_text))
                .check(matches(withText("Usuário alterado com sucesso!")));
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
