package com.example.booktracker.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.booktracker.R;
import com.example.booktracker.boundary.BookCollection;
import com.example.booktracker.boundary.DeleteBookQuery;
import com.example.booktracker.boundary.GetBookQuery;
import com.example.booktracker.entities.Book;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

import static android.provider.AlarmClock.EXTRA_MESSAGE;
import static androidx.fragment.app.DialogFragment.STYLE_NO_TITLE;

public class MyBooksFragment extends Fragment {
    private ListView bookList;
    private Book selected_book = null;
    private GetBookQuery getQuery;
    private String userEmail, userSelected;
    private View view;
    private DeleteBookQuery del;
    private BookCollection collection;
    private MyBooksFragment instance;
    private DocumentSnapshot userDoc;
    private String lastStatus = "myBooks";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // =======================set attributes=======================
        view = inflater.inflate(R.layout.fragment_my_books, container, false);
        HomeActivity activity = (HomeActivity) getActivity();
        bookList = view.findViewById(R.id.my_book_list);
        userEmail = activity.getUserEmail();
        collection = new BookCollection(new ArrayList<>(), bookList,
                userEmail, view.getContext());
        del = new DeleteBookQuery(userEmail);
        instance = this;
        getQuery = (new GetBookQuery(userEmail, collection, view.getContext()));
        setHasOptionsMenu(true);
        activity.notifRefresh();
        // ======================================================
        setSelectListener();
        setDeleteListener();
        setViewListener();
        setFilterListener();

        ImageButton addBookBtn = view.findViewById(R.id.add_book_button);
        addBookBtn.setOnClickListener(view -> {
            Intent intent = new Intent(view.getContext(),
                    AddBookActivity.class);
            intent.putExtra(EXTRA_MESSAGE, userEmail);
            startActivity(intent);
        });

        ImageButton editBookBtn = view.findViewById(R.id.edit_book_button);
        editBookBtn.setOnClickListener(view -> {
            if (selected_book != null) {
                Intent intent = new Intent(view.getContext(),
                        EditBookActivity.class);
                intent.putExtra("USER_EMAIL", userEmail);
                intent.putExtra("BOOK", selected_book);
                startActivity(intent);
            } else {
                Toast.makeText(view.getContext(), "No book selected",
                        Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
    public String getEmail(){
        return userEmail;
    }
    private void setFilterListener(){
        ImageButton filterBtn = view.findViewById(R.id.filter_button);
        filterBtn.setOnClickListener(v -> new FilterFragment(instance).show(getParentFragmentManager(),
                "Filter"));
    }
    private void setViewListener() {
        ImageButton viewBookBtn = view.findViewById(R.id.view_book_button);
        viewBookBtn.setOnClickListener(view -> {
            if (selected_book != null) {
                Intent intent = new Intent(view.getContext(),
                        ViewBookActivity.class);
                intent.putExtra(EXTRA_MESSAGE, selected_book.getIsbn());
                startActivity(intent);
            }
            else {
                Toast.makeText(view.getContext(), "No book selected", Toast.LENGTH_SHORT).show();
            }
        });
    }


    public void setLastStatus(String lastStatus) {
        this.lastStatus = lastStatus;
    }


    /**
     * Set the callback function to be executed when a book need to be deleted
     */
    private void setDeleteListener() {
        ImageButton deleteBookBtn = view.findViewById(R.id.delete_book_button);
        deleteBookBtn.setOnClickListener(view -> {
            if (selected_book != null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setMessage("Are you sure you want to delete this book from your library?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();
            } else {
                Toast.makeText(view.getContext(), "Book cant be deleted",
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Set the callback function to keep track of the selected books
     */
    private void setSelectListener() {
        bookList.setOnItemClickListener((adapter, v, position, id) -> {
            selected_book = collection.getBook(position);
            if (selected_book.getOwner() != null) {
                userSelected = selected_book.getOwnerEmail();
            }
            if (userSelected != null) {
                getUserDoc(userSelected);
            }
        });
    }

    private Boolean getUserDoc(String owner) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (owner == null) {
            return false;
        } else {
            DocumentReference docRef = db.collection("users").document(owner);
            docRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc != null) {
                        userDoc = doc;
                    }
                }
            });
        }
        return true;
    }

    /**
     * Refresh the listView when the user return the the HomeActivity in case
     * an update to
     * the BookCollection was made
     */
    @Override
    public void onResume() {
        // this is needed to refresh the list of books displayed when the user
        // goes back to the
        // home activity
        super.onResume();
        switch (lastStatus){
            case "myBooks":
                getQuery().getMyBooks();
                break;
            case "accepted":
                getQuery().getMyBooksStatus(getEmail(),"accepted");
                break;
            case "borrowed":
                getQuery().getMyBooksStatus(getEmail(),"borrowed");
                break;
            case "available":
                getQuery().getMyBooksStatus(getEmail(),"available");
                break;
        }
    }

    public void setStatus(String newStatus) {
    }

    public GetBookQuery getQuery() {
        return getQuery;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu,
                                    @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_view_user) {
            if (getUserDoc(userSelected)) {
                showUserDialog(userDoc);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void showUserDialog(DocumentSnapshot userDoc) {
        String username = userDoc.getString("username");
        String email = userDoc.getString("email");
        String phone = userDoc.getString("phone");
        ViewUserDialog userDialog = ViewUserDialog.newInstance(username, email, phone);
        userDialog.setStyle(STYLE_NO_TITLE, 0);
        userDialog.show(getParentFragmentManager(), "VIEW USER");
    }

    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    if (selected_book.getOwner() != null && selected_book.getOwnerEmail().trim().equals(userEmail.trim())) {
                        userSelected = selected_book.getOwnerEmail();
                        del.deleteBook(selected_book);
                        collection.deleteBook(selected_book);
                    }else{
                        Toast.makeText(view.getContext(),
                                "Book cannot be deleted since you do not own it",
                                Toast.LENGTH_LONG).show();
                    }
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    //Do not delete book. return to MyBooks
                    break;
            }
        }
    };


}