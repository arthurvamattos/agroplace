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
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.is;

@LargeTest
@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ConversationTest {

    @Rule
    public ActivityTestRule<LoginActivity> mActivityTestRule = new ActivityTestRule<>(LoginActivity.class);

    @Test
    public void A_contatoPorProdutoTest() {
        ViewInteraction usernameField = onView(
                allOf(withId(R.id.login_username),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("com.google.android.material.textfield.TextInputLayout")),
                                        0),
                                0)));

        ViewInteraction passwordField = onView(
                allOf(withId(R.id.login_password),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("com.google.android.material.textfield.TextInputLayout")),
                                        0),
                                0)));


        ViewInteraction loginBtn = onView(
                allOf(withId(R.id.login_btn), withText("ENTRAR"),
                        childAtPosition(
                                allOf(withId(R.id.login_btn_holder),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                3)),
                                2)));


        usernameField.perform(scrollTo(), replaceText("felipe@mail.com"), closeSoftKeyboard());
        passwordField.perform(scrollTo(), replaceText("fakePass47"), closeSoftKeyboard());
        loginBtn.perform(scrollTo(), click());

        onView(isRoot()).perform(waitFor(6000));

        ViewInteraction listaDeProdutos = onView(
                allOf(childAtPosition(
                        allOf(withId(R.id.products_recycler_view),
                                childAtPosition(
                                        withClassName(is("android.widget.FrameLayout")),
                                        1)),
                        0),
                        isDisplayed()));
        listaDeProdutos.perform(click());

        onView(isRoot()).perform(waitFor(1000));

        ViewInteraction entrarEmContato = onView(
                allOf(withId(R.id.produto_btn), withText("ENTRAR EM CONTATO"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("androidx.core.widget.NestedScrollView")),
                                        0),
                                4),
                        isDisplayed()));
        entrarEmContato.perform(click());

        onView(isRoot()).perform(waitFor(1500));

        ViewInteraction menuContato = onView((withId(R.id.menu_conversa_contato)));
        menuContato.check(matches(isDisplayed()));
    }

    @Test
    public void B_adicionarContatoTest() {

        ViewInteraction listaDeProdutos = onView(
                allOf(childAtPosition(
                        allOf(withId(R.id.products_recycler_view),
                                childAtPosition(
                                        withClassName(is("android.widget.FrameLayout")),
                                        1)),
                        0),
                        isDisplayed()));
        listaDeProdutos.perform(click());

        onView(isRoot()).perform(waitFor(1000));

        ViewInteraction entrarEmContato = onView(
                allOf(withId(R.id.produto_btn), withText("ENTRAR EM CONTATO"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("androidx.core.widget.NestedScrollView")),
                                        0),
                                4),
                        isDisplayed()));
        entrarEmContato.perform(click());

        onView(isRoot()).perform(waitFor(1500));

        ViewInteraction actionMenuItemView = onView(
                allOf(withId(R.id.menu_conversa_contato), withContentDescription("Adicionar Contato"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.tb_conversa),
                                        3),
                                1),
                        isDisplayed()));
        actionMenuItemView.perform(click());

        onView(isRoot()).perform(waitFor(1500));
        onView(withId(com.google.android.material.R.id.snackbar_text))
                .check(matches(withText("João Victor está na sua lista de contatos")));
    }

    @Test
    public void C_iniciarConversaPeloMenuConversasTest() {
        ViewInteraction actionMenuItemView2 = onView(
                allOf(withId(R.id.menu_main_conversas), withContentDescription("Abrir conversas"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.toolbar),
                                        1),
                                1),
                        isDisplayed()));
        actionMenuItemView2.perform(click());
        onView(isRoot()).perform(waitFor(1500));

        ViewInteraction listView = onView(withId(R.id.conversas_listview));
        listView.check(matches(isDisplayed()));
    }

    @Test
    public void D_iniciarConversaPeloMenuContatosTest() {
        ViewInteraction actionMenuItemView2 = onView(
                allOf(withId(R.id.menu_main_conversas), withContentDescription("Abrir conversas"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.toolbar),
                                        1),
                                1),
                        isDisplayed()));
        actionMenuItemView2.perform(click());
        onView(isRoot()).perform(waitFor(1500));

        ViewInteraction textView2 = onView(
                allOf(withText("CONTATOS"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.stl_tabs),
                                        0),
                                1),
                        isDisplayed()));
        textView2.perform(click());

        ViewInteraction listView2 = onView((withId(R.id.contatos_listview)));
        listView2.check(matches(isDisplayed()));
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
