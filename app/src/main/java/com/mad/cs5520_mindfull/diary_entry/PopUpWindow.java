package com.mad.cs5520_mindfull.diary_entry;

import androidx.appcompat.app.AppCompatDialogFragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mad.cs5520_mindfull.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class PopUpWindow extends AppCompatDialogFragment {

    public EditText titleEditTextView;
    public EditText contentEditTextView;
    public TextView dateEditText;
    public String username;
    public popUpListener listener;
    private DatabaseReference realtimeDatabase;

    /**
     * onCreateDialog(). Create popup dialog
     * @param savedInstanceState
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_pop_up_window, null);
        realtimeDatabase = FirebaseDatabase.getInstance().getReference();
        username = getArguments().getString("username");
        builder.setView(view)
                .setTitle("Input")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
//                     Rewrote positive button method so custom validation could be added to editText
                    }
                });

        titleEditTextView = view.findViewById(R.id.titleEditTextView);
        contentEditTextView = view.findViewById(R.id.contentEditTextView);
        dateEditText = view.findViewById(R.id.dateEditText);
        return builder.create();
    }

    /**
     * onAttach() Create popup dialog
     *
     * @param context
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (popUpListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() +
                    "error when implementing popUpWindow");
        }
    }

    //Method that will be called when add is clicked on dialog
    public interface popUpListener {
        void addDiaryItem(String title, String content, String date);
    }


    /**
     * onResume() resumes activity
     */
    @Override
    public void onResume() {
        super.onResume();

        final AlertDialog d = (AlertDialog) getDialog();
        if (d != null) {
            Button positiveButton = (Button) d.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String title = titleEditTextView.getText().toString();
                    String content = contentEditTextView.getText().toString();
                    String date = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
                    HashMap<String, Object> messageObj = new HashMap<>();
                    messageObj.put("title", title);
                    messageObj.put("content", content);
                    messageObj.put("date", date);
                    // Send sticker to database and as notification to recipient
                    realtimeDatabase.child("users").child(username).child("diary-entries").push().setValue(messageObj);

                    d.dismiss();
                    Toast.makeText(getActivity(), "The link has been successfully added!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}