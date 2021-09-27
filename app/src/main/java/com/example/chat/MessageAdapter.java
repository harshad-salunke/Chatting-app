package com.example.chat;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;

import java.util.List;

public class MessageAdapter extends ArrayAdapter<FriendlyMessage> {
    public MessageAdapter(@NonNull Context context, List<FriendlyMessage> object) {
        super(context,0, object);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if(convertView==null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_massage, parent, false);
        }
        ImageView photoimageView=convertView.findViewById(R.id.photoImageView);
        TextView mesageTextView=convertView.findViewById(R.id.messageTextView);
        TextView authorTextView=convertView.findViewById(R.id.nameTextView);
        FriendlyMessage message=getItem(position);

        boolean isphoto=message.getPhotourl()!=null;
        if(isphoto){
            mesageTextView.setVisibility(View.GONE);
            photoimageView.setVisibility(View.VISIBLE);
            Glide.with(photoimageView.getContext())
                    .load(message.getPhotourl())
                    .into(photoimageView);
        }
        else {
          mesageTextView.setVisibility(View.VISIBLE);
          photoimageView.setVisibility(View.GONE);
          mesageTextView.setText(message.getText());
        }
        authorTextView.setText(message.getName());
        return convertView;
    }
}
