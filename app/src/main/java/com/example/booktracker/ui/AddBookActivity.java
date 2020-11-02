package com.example.booktracker.ui;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.booktracker.R;
import com.example.booktracker.boundary.AddBookQuery;
import com.example.booktracker.boundary.GetBookQuery;
import com.example.booktracker.control.Email;
import com.example.booktracker.entities.Book;
import com.example.booktracker.entities.BookCollection;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * https://github.com/mitchtabian/AndroidImageCropper-Example
 */
public class AddBookActivity extends AppCompatActivity {

    private String TAG = "AppDebug";
    private int GALLERY_REQUEST_CODE = 1234;
    private Uri imageUri;
    private BookCollection bookList;
    private String email;
    private AddBookQuery addQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addbook);

        final EditText titleView = findViewById(R.id.addbook_title);
        final EditText authorView = findViewById(R.id.addbook_author);
        final EditText isbnView = findViewById(R.id.addbook_isbn);
        final EditText descView = findViewById(R.id.addbook_description);

        final ImageView imageView = findViewById(R.id.addbook_image);

        //============Ivan===============
        Button scanBtn = findViewById(R.id.scan_btn);
        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(view.getContext(), ScanActivity.class));
            }
        });
        email = ((Email) this.getApplication()).getEmail();

        //===============================

        Button addBtn = findViewById(R.id.addbook_addbtn);
        addBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                List<String> authors = new ArrayList<String>();
                String title = titleView.getText().toString();
                String author = authorView.getText().toString();
                String isbn = isbnView.getText().toString();
                String desc = descView.getText().toString();
                if (isbn.length() != 13){
                    isbnView.setError("isbn must have 13 digits");
                }
                authors.add(author);
                Book newBook = new Book(email,authors,title,isbn,desc);
                addQuery = new AddBookQuery(email);
                Toast.makeText(AddBookActivity.this, addQuery.addBook(newBook), Toast.LENGTH_LONG).show();
                //====Ivan: made it so that the activity automatically exits==
                try{
                    Thread.sleep(2000);
                }catch (InterruptedException e){
                    Thread.currentThread().interrupt();
                }
                finish();
                //============================================================
            }
        });

        Button cancelBtn = findViewById(R.id.addbook_cancelbtn);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(v.getContext(), HomeActivity.class));
            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickFromGallery(v);
            }
        });

    }

    /**
     *
     *
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == this.GALLERY_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            imageUri = result.getUri();
            launchImageCrop(imageUri);
            setImage(imageUri);
        }

        else {
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
        }
    }

    private void setImage(Uri uri) {
        ImageView image = findViewById(R.id.addbook_image);
        Glide.with(this)
                .load(uri)
                .into(image);
    }

    private void launchImageCrop(Uri uri) {
        CropImage.activity(uri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1200, 1200)
                .setCropShape(CropImageView.CropShape.RECTANGLE)
                .start(this);
    }

    private void pickFromGallery(View v) {
        Intent intent = new Intent(v.getContext(), MediaStore.Images.Media.EXTERNAL_CONTENT_URI.getClass());
        intent.setType("image/*");
        String[] mimeTypes = new String[]{"image/jpeg", "image/png", "image/jpg"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(intent, GALLERY_REQUEST_CODE);

    }


}
