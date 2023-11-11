package com.example.iat359project;

import static com.example.iat359project.R.layout.activity_entrycard;

import static java.lang.String.valueOf;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class RecAdapter extends RecyclerView.Adapter<RecAdapter.MyViewHolder>{

    static ArrayList<Journal> list;
    Context context;
    private final RecViewInterface recViewInterface;

    public RecAdapter(ArrayList<Journal> _list, RecViewInterface _recViewInterface){
        list = _list; //List of all of the Journal entries
        recViewInterface = _recViewInterface;
    }

    @Override
    public RecAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int pos) {
        View v = LayoutInflater.from(parent.getContext()).inflate(activity_entrycard, parent, false);
        MyViewHolder vHolder = new MyViewHolder(v);
        return vHolder;
    }


    @Override
    public void onBindViewHolder(RecAdapter.MyViewHolder holder, int position) {
        //Sets the values of each of the necessary views
        Journal j = list.get(position);
        //Text entry
        holder.entryText.setText(j.getEntry());
        //Date
        String deleteSecs = j.getDate().substring(0,j.getDate().length()-3);
        holder.dateText.setText(deleteSecs);
        //Mood
        setMoodColour(holder, j.getMood());
        switch(j.getMood()){
            case Constants.HAPPY:   holder.moodText.setText(valueOf("Happy")); break;
            case Constants.SAD:     holder.moodText.setText(valueOf("Sad")); break;
            case Constants.ANGRY:   holder.moodText.setText(valueOf("Angry")); break;
            case Constants.LOVE:    holder.moodText.setText(valueOf("Love")); break;
            case Constants.TIRED:   holder.moodText.setText(valueOf("Tired")); break;
            case Constants.WORRIED: holder.moodText.setText(valueOf("Worried")); break;
            case Constants.EXCITED: holder.moodText.setText(valueOf("Excited")); break;
            case Constants.NEUTRAL: holder.moodText.setText(valueOf("Neutral")); break;
        }
        //Temp
        holder.tempText.setText(valueOf(j.getTemp() +" atm"));
        //Image
        Bitmap pic = rotateImageSQL(j);
        holder.entryImage.setImageBitmap(pic);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public TextView entryText;
        public TextView dateText;
        public TextView moodText;
        public TextView tempText;
        public ImageView entryImage;
        public TextView moodColour;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            //Initialization
            entryText = (TextView) itemView.findViewById(R.id.textEntry);
            dateText = (TextView) itemView.findViewById(R.id.textDate);
            moodText = (TextView) itemView.findViewById(R.id.textMood);
            tempText = (TextView) itemView.findViewById(R.id.textTemp);
            entryImage = (ImageView) itemView.findViewById(R.id.imagePic);
            moodColour = (TextView) itemView.findViewById(R.id.moodColour);;

            //Add onClick and onLongClick for use in the Gallery activity

            itemView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    if (recViewInterface != null){
                        int pos = getAdapterPosition();
                        recViewInterface. onItemClick(pos);
                    }
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener(){
                @Override
                public boolean onLongClick(View v){
                    if (recViewInterface != null){
                        int pos = getAdapterPosition();
                        recViewInterface.onItemLongClick(pos);
                    }
                    return true;
                }
            });
            context = itemView.getContext();
        }

        @Override
        public void onClick(View v) {
            Toast.makeText(context, "You clicked... " + dateText.getText().toString(), Toast.LENGTH_SHORT).show();
        }
    }
    @SuppressLint("ResourceAsColor")
    public void setMoodColour(RecAdapter.MyViewHolder holder, int mood){
        //Sets recyclerView's CardView's textView of the mood into the correct color.
        switch (mood) {
            case Constants.HAPPY:
                holder.moodColour.setBackgroundColor(ContextCompat.getColor(context.getApplicationContext(),R.color.happyGreen));
                break;
            case Constants.SAD:
                holder.moodColour.setBackgroundColor(ContextCompat.getColor(context.getApplicationContext(), R.color.sadBlue));
                break;
            case Constants.ANGRY:
                holder.moodColour.setBackgroundColor(ContextCompat.getColor(context.getApplicationContext(), R.color.angryRed));
                break;
            case Constants.LOVE:
                holder.moodColour.setBackgroundColor(ContextCompat.getColor(context.getApplicationContext(), R.color.lovePink));
                break;
            case Constants.TIRED:
                holder.moodColour.setBackgroundColor(ContextCompat.getColor(context.getApplicationContext(), R.color.tiredBrown));
                break;
            case Constants.WORRIED:
                holder.moodColour.setBackgroundColor(ContextCompat.getColor(context.getApplicationContext(), R.color.worriedPurple));
                break;
            case Constants.EXCITED:
                holder.moodColour.setBackgroundColor(ContextCompat.getColor(context.getApplicationContext(), R.color.excitedYellow));
                break;
            case Constants.NEUTRAL:
                holder.moodColour.setBackgroundColor(ContextCompat.getColor(context.getApplicationContext(), R.color.neutralGrey));
                break;
        }
    }

    private Bitmap rotateImageSQL(Journal j){ //Rotates image if necessary
        Bitmap pic = null;
        if(j.getImage() != null && j.getImage() != "NO IMAGE") {
            File f = new File(j.getImage());
            pic = BitmapFactory.decodeFile(f.getAbsolutePath());
            try {
                pic = ImageActivity.rotatePhoto(f.getAbsolutePath(), pic);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return pic;
    }


}
