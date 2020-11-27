package com.example.booktracker;

import android.app.Activity;
import android.widget.EditText;
import android.widget.SearchView;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import com.example.booktracker.boundary.AddBookQuery;
import com.example.booktracker.boundary.DeleteBookQuery;
import com.example.booktracker.entities.Book;
import com.example.booktracker.entities.Request;
import com.example.booktracker.ui.HomeActivity;
import com.example.booktracker.ui.SetGeoActivity;
import com.example.booktracker.ui.SignInActivity;
import com.example.booktracker.ui.ViewBookActivity;
import com.example.booktracker.ui.ViewGeoActivity;
import com.google.android.gms.maps.model.LatLng;
import com.robotium.solo.Solo;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.Assert.assertTrue;

/**
 * Book Exchange Test
 * Tests Requesting, Accepting, setGeo, viewGeo, borrowing, returning
 */
public class BookExchangeTest {
    private Solo solo;
    private String email1 = "testrequest1@gmail.com";
    private String email2 = "testrequest2@gmail.com";
    private String pass = "password";


    private Book book;
    @Rule
    public ActivityTestRule<SignInActivity> rule =
            new ActivityTestRule<>(SignInActivity.class, true, true);

    /**
     * Initialize solo to be used by tests. And add book to be tested in db.
     *
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        addBookToDb(email1);
        solo = new Solo(InstrumentationRegistry.getInstrumentation(),
                rule.getActivity());
        instantiateReqs(email1);
        instantiateReqs(email2);
    }

    private void instantiateReqs(String email) {
        login(email);
        navDrawer("Incoming Requests");
        navDrawer("Accepted Requests");
        navDrawer("Requested Books");
        logout();
    }


    /**
     * Sign in and set the current activity to HomeActivity.
     */
    private void login(String email) {
        solo.assertCurrentActivity("Wrong activity should be SignInActivity",
                SignInActivity.class);
        solo.enterText((EditText) solo.getView(R.id.email_field), email);
        solo.enterText((EditText) solo.getView(R.id.password_field), pass);
        solo.clickOnButton("Sign In");
        checkActivity(HomeActivity.class, "HomeActivity");
    }

    /**
     * Log out of current account
     */
    private void logout(){
        navDrawer("Profile");
        solo.clickOnText("Logout");
        checkActivity(SignInActivity.class, "SignInActivity");
    }


    /**
     * Delete the book that was used from firestore.
     */
    private void deleteBook(String email) {
        DeleteBookQuery del = new DeleteBookQuery(email);
        Book book1 = new Book();
        book1.setIsbn("1234123412349");
        book1.setStatus("available");
        del.deleteBook(book1);
    }

    /**
     * Add test book to db
     */
    private void addBookToDb(String email) {
        AddBookQuery addBook = new AddBookQuery(email);
        ArrayList<String> author = new ArrayList<>();
        author.add("author");
        HashMap<String, String> owner = new HashMap<>();
        owner.put(email, "");
        book = new Book(owner, author, "request69 Test title",
                "1234123412349", "descr69");
        addBook.loadUsername(book);
        addBook.addBook(book);
    }

    /**
     * test2@gmail.com requests book from test1@gmail.com
     */
    private void requestBook() {
        navDrawer("Find Books");
        SearchView searchview = (SearchView) solo.getView(R.id.book_search);
        searchview.setQuery("descr69",true);
        assertTrue("Book not appearing in Find Books", solo.searchText("request69"));
        //click on book
        solo.clickOnText("request69");
        //request book
        solo.clickOnView(solo.getView(R.id.request_book_button));
    }

    /**
     * Once user has requested a book it should show up in RequestedBooks
     */
    private void checkRequestedBooks(){
        navDrawer("Requested Books");
        assertTrue("Book not appearing in Requested", solo.searchText("request69"));
    }

    /**
     * test1@gmail.com accepts book request from test2@gmail.com
     * and specifies a pickupLocation
     */
    private void acceptRequest() {
        //accept request
        navDrawer("Incoming Requests");
        assertTrue("Book not appearing in Incoming requests", solo.searchText("request69"));
        solo.clickOnText("request69");
        solo.clickOnText("Accept");
        //solo.clickOnButton("Accept");
        solo.clickOnText("Yes");
        checkActivity(SetGeoActivity.class, "SetGeoActivity");
        setGeo();
    }

    /**
     * DOESNT WORK -- NEED TO SET LOCATION PIN MANUALLY
     */
    private void setGeo(){
        //set location
        solo.clickLongOnScreen(240,400,1500);
        solo.clickOnButton("Confirm");
        checkActivity(HomeActivity.class, "HomeActivity");
    }

    /**
     * test1@gmail.com accepts book request from test2@gmail.com
     * and specifies a pickupLocation
     */
    private void viewLocation() {
        navDrawer("Accepted Requests");
        assertTrue("Book not appearing in Accepted requests", solo.searchText("request69"));
        solo.clickOnText("request69");
        solo.clickOnButton("View Pickup Location");
        checkActivity(ViewGeoActivity.class, "ViewGeoActivity");
        solo.sleep(1000);
        solo.clickOnButton("Done");
        checkActivity(HomeActivity.class, "HomeActivity");

    }

    /**
     * denote book as borrowed from borrower side in Accepted requests (View Book)
     */
    private void borrowBook() {
        solo.clickOnText("request69");
        solo.clickOnView(solo.getView(R.id.view_button_accepted));
        checkActivity(ViewBookActivity.class, "ViewBookActivity");
        solo.clickOnView(solo.getView(R.id.borrow_book_button));
        exitActivity();
    }

    /**
     * denote book as borrowed from owner side
     */
    private void giveBook() {
        checkActivity(HomeActivity.class, "HomeActivity");
        assertTrue("Book not appearing in My Books", solo.searchText("request69"));
        solo.clickOnText("request69");
        solo.clickOnButton("View");
        checkActivity(ViewBookActivity.class, "ViewBookActivity");
        solo.clickOnButton("Give Book");
        exitActivity();

    }


    /**
     * Book should appear with status borrowed in MyBooks
     */
    private void checkBorrowedStatus(){
        navDrawer("My Books");
        assertTrue("Book not appearing in My Books", solo.searchText("request69"));
        solo.clickOnText("request69");
        solo.clickOnButton("View");
        assertTrue("Book status is not borrowed", solo.searchText("Borrowed"));
        exitActivity();

    }

    /**
     * Book should appear in borrowed books
     */
    private void checkBorrowedBook(){
        navDrawer("Borrowed Books");
        assertTrue("Book not appearing in borrowed books", solo.searchText("request69"));
    }


    /**
     * user2 returns book to user1
     */
    private void returnBook(){
        solo.clickOnText("request69");
        solo.clickOnButton("View");
        checkActivity(ViewBookActivity.class, "ViewBookActivity");
        solo.clickOnButton("Return Book");
        exitActivity();

    }

    /**
     * user1 confirms book is received
     */
    private void receiveBook() {
        navDrawer("My Books");
        assertTrue("Book not appearing in My Books", solo.searchText("request69"));
        solo.clickOnText("request69");
        solo.clickOnButton("View");
        checkActivity(ViewBookActivity.class, "ViewBookActivity");
        exitActivity();
    }

    private void checkActivity(final Class<? extends Activity> Activity, String activity) {
        solo.waitForActivity(Activity);
        solo.assertCurrentActivity("Wrong activity should be " + activity + ", was " + solo.getCurrentActivity(),
                Activity);
    }

    /**
     * return activity to HomeActivity
     */
    private void exitActivity() {
        solo.goBack();
        checkActivity(HomeActivity.class, "HomeActivity");
    }

    /**
     *
     * @param fragment - nav drawer item to click
     */
    private void navDrawer(String fragment){
        solo.clickOnView(solo.getView(R.id.nav_find));
        solo.clickOnImageButton(0);
        solo.clickOnText(fragment);
    }


    /**
     * Delete book form db
     */
    @After
    public final void tearDown() {
        deleteBook(email1);
        DeleteBookQuery del = new DeleteBookQuery();
        del.deleteBookList("incomingRequests",email1);
        del.deleteBookList("lent",email1);
        del.deleteBookList("borrowed",email2);
        del.deleteBookList("requested",email2);
        del.deleteBookList("accepted",email2);
    }

    /**
     *
     */
    @Test
    public void ExchangeTest() {
        //login to user2 account - this account will request user1 book
        login(email2);
        //create request for user1 book from user2
        requestBook();
        //see if new request shows
        checkRequestedBooks();
        //logout user2 account
        logout();
        //login to user1 account
        login(email1);
        //accept request for my book and set location
        acceptRequest();
        //logout user1 account
        logout();
        //login user2 account
        login(email2);
        //view accepted request location
        viewLocation();
        //user2 denote borrowed
        borrowBook();
        //logout user2 account
        logout();
        //login user1 account
        login(email1);
        //user1 denote borrowed
        giveBook();
        //book should have status "borrowed" in My Books
        checkBorrowedStatus();
        //logout user1 account
        logout();
        //login user2 account
        login(email2);
        //book should appear in Borrowed Books
        checkBorrowedBook();
        //return book back to user1
        returnBook();
        //logout user2 account
        logout();
        //login user1 account
        login(email1);
        //receive book from user2
        receiveBook();
        //book should appear available in My Books

        //DONE
    }

}