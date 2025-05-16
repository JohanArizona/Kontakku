package com.example.tugaspamaplikasikontak.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tugaspamaplikasikontak.R;
import com.example.tugaspamaplikasikontak.adapter.ContactAdapter;
import com.example.tugaspamaplikasikontak.model.Contact;
import com.example.tugaspamaplikasikontak.ui.auth.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ContactAdapter adapter;
    private List<Contact> contactList;
    private DatabaseReference db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance().getReference("contacts").child(mAuth.getCurrentUser().getUid());
        recyclerView = findViewById(R.id.recyclerView);
        Button addButton = findViewById(R.id.addButton);
        Button logoutButton = findViewById(R.id.logoutButton);

        contactList = new ArrayList<>();
        adapter = new ContactAdapter(this, contactList, mAuth.getCurrentUser().getUid());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        addButton.setOnClickListener(v -> showAddContactDialog());
        logoutButton.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        loadContacts();
    }

    private void showAddContactDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Tambah Kontak");

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_contact, null);
        EditText nameEditText = view.findViewById(R.id.nameEditText);
        EditText phoneEditText = view.findViewById(R.id.phoneEditText);
        EditText instagramEditText = view.findViewById(R.id.instagramEditText);

        builder.setView(view);
        builder.setPositiveButton("Simpan", (dialog, which) -> {
            String name = nameEditText.getText().toString();
            String phone = phoneEditText.getText().toString();
            String instagram = instagramEditText.getText().toString();

            if (name.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Nama dan nomor telepon wajib diisi", Toast.LENGTH_SHORT).show();
                return;
            }

            String contactId = db.push().getKey();
            Contact contact = new Contact(contactId, name, phone, instagram);

            Map<String, Object> contactMap = new HashMap<>();
            contactMap.put("id", contact.getId());
            contactMap.put("name", contact.getName());
            contactMap.put("phone", contact.getPhone());
            contactMap.put("instagram", contact.getInstagram());

            db.child(contactId)
                    .setValue(contactMap)
                    .addOnSuccessListener(aVoid -> Toast.makeText(this, "Kontak ditambahkan", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(this, "Gagal menambah kontak", Toast.LENGTH_SHORT).show());
        });
        builder.setNegativeButton("Batal", null);
        builder.show();
    }

    private void loadContacts() {
        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                contactList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Contact contact = dataSnapshot.getValue(Contact.class);
                    if (contact != null) {
                        contactList.add(contact);
                    }
                }
                // Sort contacts by name (alphabetically, ascending)
                Collections.sort(contactList, new Comparator<Contact>() {
                    @Override
                    public int compare(Contact c1, Contact c2) {
                        return c1.getName().compareToIgnoreCase(c2.getName());
                    }
                });
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomeActivity.this, "Gagal memuat kontak", Toast.LENGTH_SHORT).show();
            }
        });
    }
}