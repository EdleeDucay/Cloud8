package com.example.booktracker;

import android.widget.EditText;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import com.example.booktracker.boundary.AddBookQuery;
import com.example.booktracker.boundary.DeleteBookQuery;
import com.example.booktracker.entities.Book;
import com.example.booktracker.ui.HomeActivity;
import com.example.booktracker.ui.SignInActivity;
import com.robotium.solo.Solo;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class BookUserInfoTest {
    private Solo solo;
    private String email = "test@gmail.com";
    private String pass = "password";
    private String username = "test";
    private String phone = "12345678";
    private Book book;
    @Rule
    public ActivityTestRule<SignInActivity> rule =
            new ActivityTestRule<>(SignInActivity.class, true, true);

    /**
     * Initialize solo to be used by tests.
     *
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        addToDb();
        solo = new Solo(InstrumentationRegistry.getInstrumentation(), rule.getActivity());
        login();
    }

    /**
     * called after each test has been done
     */
    @After
    public void tearDown(){
        deleteBook();
    }

    /**
     * Delete the book that was used from firestore.
     */
    private void deleteBook() {
        DeleteBookQuery del = new DeleteBookQuery(email);
        Book book1 = new Book();
        book1.setIsbn("6980671678814");
        book1.setStatus("available");
        del.deleteBook(book1);
    }

    /**
     * Sign in and set the current activity to HomeActivity.
     */
    private void login() {
        solo.assertCurrentActivity("Wrong activity, should be SignInActivity", SignInActivity.class);
        solo.enterText((EditText) solo.getView(R.id.email_field), email);
        solo.enterText((EditText) solo.getView(R.id.password_field), pass);
        solo.clickOnButton("Sign In");
        solo.waitForActivity(HomeActivity.class);
        solo.assertCurrentActivity("Wrong activity, should be HomeActivity", HomeActivity.class);
    }

    /**
     * Add test book to db
     */
    private void addToDb() {
        AddBookQuery addBook = new AddBookQuery(email);
        List<String> author = new ArrayList<>();
        author.add("Karl Pogs");
        HashMap<String, String> owner = new HashMap<>();
        owner.put(email, "");
        book = new Book(owner, author, "/pol/ Manifesto",
                "6980671678814", "Test book");
        addBook.loadUsername(book);
        addBook.addBook(book);
    }

    @Test
    public void testUserInfo() {
        solo.clickOnText("/pol/ Manifesto");
        solo.clickOnMenuItem("View User");
        assertTrue("Cannot find username", solo.searchText(username));
        assertTrue("Cannot find email", solo.searchText(email));
        assertTrue("Cannot find phone number", solo.searchText(phone));
    }
}
