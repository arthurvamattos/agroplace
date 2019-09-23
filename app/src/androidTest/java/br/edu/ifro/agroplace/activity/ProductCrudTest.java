package br.edu.ifro.agroplace.activity;


import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.annotation.CheckResult;
import androidx.test.espresso.AmbiguousViewMatcherException;
import androidx.test.espresso.NoMatchingRootException;
import androidx.test.espresso.NoMatchingViewException;
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
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsInstanceOf.any;

@LargeTest
@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ProductCrudTest {

    @Rule
    public ActivityTestRule<LoginActivity> mActivityTestRule = new ActivityTestRule<>(LoginActivity.class);

    @Test
    public void A_createProductTest() {
        // login
        ViewInteraction loginUsernameField = onView(withId(R.id.login_username));
        ViewInteraction loginPasswordField = onView(withId(R.id.login_password));
        ViewInteraction loginBtn = onView(withId(R.id.login_btn));

        ViewInteraction myProductsBtn = onView(
                allOf(withText("MEUS PRODUTOS"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.main_stl_tabs),
                                        0),
                                1),
                        isDisplayed()));

        loginUsernameField.perform(scrollTo(), replaceText("auth@test.com"), closeSoftKeyboard());
        loginPasswordField.perform(scrollTo(), replaceText("senha1234"), closeSoftKeyboard());
        loginBtn.perform(scrollTo(), click());

        onView(isRoot()).perform(waitFor(12000));

        ViewInteraction novaVendaBtn = onView(withId(R.id.btn_nova_venda));

        myProductsBtn.perform(scrollTo(), click());
        novaVendaBtn.perform(click());

        onView(isRoot()).perform(waitFor(1000));

        ViewInteraction productPhotoBtn = onView(withId(R.id.formulario_btn_foto));
        ViewInteraction optionGallery = onView(
                allOf(withId(android.R.id.button1), withText("Abrir a galeria"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.buttonPanel),
                                        0),
                                3)));
        ViewInteraction productNameField = onView(withId(R.id.formulario_nome_produto));
        ViewInteraction productValueField = onView(withId(R.id.formulario_valor));
        ViewInteraction productDescriptionField = onView(withId(R.id.formulario_descricao));
        ViewInteraction btnSalvar = onView(withId(R.id.menu_formulario_salvar));

        productPhotoBtn.perform(click());
        optionGallery.perform(scrollTo(), click());
        productNameField.perform(replaceText("Banana"), closeSoftKeyboard());
        productValueField.perform(replaceText("R$ 03.00"), closeSoftKeyboard());
        productDescriptionField.perform(replaceText("Tipo: nanica"), closeSoftKeyboard());
        btnSalvar.perform(click());

        onView(isRoot()).perform(waitFor(10000));

        ViewInteraction textView = onView(
                allOf(withId(R.id.product_name), withText("Banana"),
                        childAtPosition(
                                allOf(withId(R.id.product_descript),
                                        childAtPosition(
                                                withId(R.id.product_container),
                                                0)),
                                0),
                        isDisplayed()));
        textView.check(matches(withText("Banana")));

    }

    @Test
    public void B_editProductTest() {
        onView(isRoot()).perform(waitFor(3000));

        ViewInteraction myProductsBtn = onView(
                allOf(withText("MEUS PRODUTOS"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.main_stl_tabs),
                                        0),
                                1),
                        isDisplayed()));

        ViewInteraction productNameField = onView(withId(R.id.formulario_nome_produto));
        ViewInteraction productValueField = onView(withId(R.id.formulario_valor));
        ViewInteraction productDescriptionField = onView(withId(R.id.formulario_descricao));
        ViewInteraction btnSalvar = onView(withId(R.id.menu_formulario_salvar));

        myProductsBtn.perform(scrollTo(), click());

        ViewInteraction constraintLayout = onView(
                allOf(childAtPosition(
                        allOf(withId(R.id.myproducts_recycler_view),
                                childAtPosition(
                                        withClassName(is("android.widget.RelativeLayout")),
                                        1)),
                        0),
                        isDisplayed()));
        constraintLayout.perform(click());

        productNameField.perform(replaceText("Banana nanica"), closeSoftKeyboard());
        productValueField.perform(replaceText("R$ 03.50"), closeSoftKeyboard());
        productDescriptionField.perform(replaceText("Novinha"), closeSoftKeyboard());
        btnSalvar.perform(click());

        onView(isRoot()).perform(waitFor(2000));
        ViewInteraction textView = onView(
                allOf(withId(R.id.product_name), withText("Banana nanica"),
                        childAtPosition(
                                allOf(withId(R.id.product_descript),
                                        childAtPosition(
                                                withId(R.id.product_container),
                                                0)),
                                0),
                        isDisplayed()));
        textView.check(matches(withText("Banana nanica")));

    }

    @Test
    public void C_deleteProductTest() {
        onView(isRoot()).perform(waitFor(3000));
        ViewInteraction myProductsBtn = onView(
                allOf(withText("MEUS PRODUTOS"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.main_stl_tabs),
                                        0),
                                1),
                        isDisplayed()));
        myProductsBtn.perform(scrollTo(), click());

        ViewInteraction constraintLayout = onView(
                allOf(childAtPosition(
                        allOf(withId(R.id.myproducts_recycler_view),
                                childAtPosition(
                                        withClassName(is("android.widget.RelativeLayout")),
                                        1)),
                        0),
                        isDisplayed()));
        constraintLayout.perform(click());

        onView(isRoot()).perform(waitFor(500));

        ViewInteraction actionMenuItemView3 = onView(
                allOf(withId(R.id.menu_formulario_deletar), withContentDescription("Deletar"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.toolbar),
                                        3),
                                0),
                        isDisplayed()));
        actionMenuItemView3.perform(click());

        ViewInteraction appCompatButton3 = onView(
                allOf(withId(android.R.id.button1), withText("Sim"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.buttonPanel),
                                        0),
                                3)));
        appCompatButton3.perform(scrollTo(), click());

        ViewInteraction textView3 = onView(
                allOf(withId(R.id.product_name), withText("Banana nanica"),
                        childAtPosition(
                                allOf(withId(R.id.product_descript),
                                        childAtPosition(
                                                withId(R.id.product_container),
                                                0)),
                                0),
                        isDisplayed()));
        textView3.check(doesNotExist());
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
