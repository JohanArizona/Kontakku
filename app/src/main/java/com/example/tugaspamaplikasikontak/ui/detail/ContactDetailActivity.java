package com.example.tugaspamaplikasikontak.ui.detail;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.tugaspamaplikasikontak.R;
import com.example.tugaspamaplikasikontak.model.Contact;
import com.example.tugaspamaplikasikontak.ui.home.HomeActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class ContactDetailActivity extends AppCompatActivity {
    private TextView nameTextView, phoneTextView, instagramTextView, whatsappTextView;
    private DatabaseReference db;
    private String contactId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_detail);

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db = FirebaseDatabase.getInstance().getReference("contacts").child(userId);
        nameTextView = findViewById(R.id.tv_name);
        phoneTextView = findViewById(R.id.tv_number);
        instagramTextView = findViewById(R.id.tv_instagram);
        whatsappTextView = findViewById(R.id.tv_whatsapp);
        TextView backLink = findViewById(R.id.back_link);
        TextView callButton = findViewById(R.id.btn_call);
        TextView whatsappButton = findViewById(R.id.tv_whatsapp);
        TextView instagramButton = findViewById(R.id.btn_instagram);
        ImageView editButton = findViewById(R.id.btn_edit);
        TextView deleteButton = findViewById(R.id.btn_delete);

        contactId = getIntent().getStringExtra("contact_id");
        loadContact();

        backLink.setOnClickListener(v -> finish());

        callButton.setOnClickListener(v -> {
            String phone = phoneTextView.getText().toString();
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone));
            startActivity(intent);
        });

        whatsappButton.setOnClickListener(v -> {
            String phone = phoneTextView.getText().toString();
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/" + phone));
            startActivity(intent);
        });

        instagramButton.setOnClickListener(v -> {
            String instagram = instagramTextView.getText().toString();
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://instagram.com/" + instagram));
            startActivity(intent);
        });

        editButton.setOnClickListener(v -> showEditContactDialog());

        deleteButton.setOnClickListener(v -> {
            db.child(contactId)
                    .removeValue()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Kontak dihapus", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, HomeActivity.class));
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Gagal menghapus kontak", Toast.LENGTH_SHORT).show());
        });
    }

    private void loadContact() {
        db.child(contactId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Contact contact = snapshot.getValue(Contact.class);
                if (contact != null) {
                    nameTextView.setText(contact.getName());
                    phoneTextView.setText(contact.getPhone());
                    instagramTextView.setText(contact.getInstagram());
                    whatsappTextView.setText("WA");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ContactDetailActivity.this, "Gagal memuat kontak", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEditContactDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Kontak");

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_contact, null);
        EditText nameEditText = view.findViewById(R.id.nameEditText);
        EditText phoneEditText = view.findViewById(R.id.phoneEditText);
        EditText instagramEditText = view.findViewById(R.id.instagramEditText);

        // Pre-fill fields with current contact data
        nameEditText.setText(nameTextView.getText());
        phoneEditText.setText(phoneTextView.getText());
        instagramEditText.setText(instagramTextView.getText());

        builder.setView(view);
        builder.setPositiveButton("Simpan", (dialog, which) -> {
            String name = nameEditText.getText().toString();
            String phone = phoneEditText.getText().toString();
            String instagram = instagramEditText.getText().toString();

            if (name.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Nama dan nomor telepon wajib diisi", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> contactMap = new HashMap<>();
            contactMap.put("id", contactId);
            contactMap.put("name", name);
            contactMap.put("phone", phone);
            contactMap.put("instagram", instagram);

            db.child(contactId)
                    .setValue(contactMap)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Kontak diperbarui", Toast.LENGTH_SHORT).show();
                        loadContact(); // Refresh the view with updated data
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Gagal memperbarui kontak", Toast.LENGTH_SHORT).show());
        });
        builder.setNegativeButton("Batal", null);
        builder.show();
    }
}