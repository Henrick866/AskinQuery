package personnal.askinquery;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class AnswerOptionAdapter extends ArrayAdapter<Option> {
        private Context c;
        private int Type, positionQuestion;
        private boolean Si_Resultats;
        private int PositionChecked;
        private LayoutInflater layoutInflater;
        private OptionToQuestionListener optionToQuestionListener;
AnswerOptionAdapter(Context context, ArrayList<Option> options, int type, OptionToQuestionListener optionToQuestionListener, int positionQuestion, boolean b){
        super(context, 0, options);
        c = context;
        this.positionQuestion = positionQuestion;
        layoutInflater = LayoutInflater.from(context);
        this.Si_Resultats = b;
        this.optionToQuestionListener = optionToQuestionListener;
        PositionChecked = this.optionToQuestionListener.getPositionChecked(positionQuestion);
        Type = type;
        }

public View getView(final int position, View convertView, ViewGroup parent){
        final Option option = getItem(position);
        View view = convertView;
final AnswerOptionAdapter.ViewHolder holder;
        if(view == null){
        view = layoutInflater.inflate(R.layout.answer_option_element, parent, false);
        holder = new AnswerOptionAdapter.ViewHolder(view, position, PositionChecked);

        view.setTag(holder);
        }else{
        holder = (AnswerOptionAdapter.ViewHolder)view.getTag();
        }
        holder.TexteOption.setText(option.Texte);
        if(Type != Question.TYPE_TEXTE) {

            if (option.ImagePreload != null) {
                holder.OptionImg.setImageBitmap(option.ImagePreload);

                if(Type == Question.TYPE_VIDEO){
                    holder.OptionIcon.setImageResource(R.drawable.ic_play_circle_color);
                    holder.OptionIcon.setVisibility(View.VISIBLE);
                    holder.OptionImg.setVisibility(View.VISIBLE);
                    holder.OptionImg.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(c, VideoPopupActivity.class);
                            intent.putExtra("Path", option.Chemin_Media);
                            intent.putExtra("NotOnServer", option.notOnServer);
                            c.startActivity(intent);
                        }
                    });
                }else{
                    holder.OptionIcon.setImageResource(R.drawable.ic_loupe_black_24dp);
                    holder.OptionIcon.setVisibility(View.VISIBLE);
                    holder.OptionImg.setVisibility(View.VISIBLE);
                    holder.OptionImg.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            final Dialog dialog = new Dialog(c);
                            dialog.setContentView(R.layout.image_dialog);
                            dialog.show();
                            final ImageView Image = dialog.findViewById(R.id.image_dialog_imageview);
                            final ProgressBar progressBar = dialog.findViewById(R.id.image_dialog_progressBar);

                            StorageReference FullImgRef = FirebaseStorage.getInstance().getReference().child(option.Chemin_Media);
                            FullImgRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                @Override
                                public void onSuccess(byte[] bytes) {
                                    Bitmap b = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                    Image.setImageBitmap(b);
                                    progressBar.setVisibility(View.GONE);
                                    Image.setVisibility(View.VISIBLE);
                                }
                            });
                            final Button CloseBtn = dialog.findViewById(R.id.ImageCloseBtn);
                            CloseBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    dialog.dismiss();
                                }
                            });
                        }
                    });
                }
            } else {
                holder.OptionImg.setVisibility(View.INVISIBLE);
            }
        }else{
            holder.OptionImg.setVisibility(View.GONE);
        }
        if(Si_Resultats){
            holder.CheckZone.setVisibility(View.GONE);
            holder.ResultZone.setVisibility(View.VISIBLE);
            int total = option.getTotalVotes();
            if(total > 0) {
                int percentage = (option.Score * 100) / option.getTotalVotes();
                holder.LineResult.setProgress(percentage);
                holder.PercentVote.setText(c.getString(R.string.Poll_Answ_Percent, percentage));
            }else{
                holder.LineResult.setProgress(0);
                holder.PercentVote.setText("0 %");
            }
        }else{
            holder.CheckZone.setVisibility(View.VISIBLE);
            holder.ResultZone.setVisibility(View.GONE);
        }
        if(PositionChecked == position){
             holder.VoteBtn.setChecked(true);
        }
        final ViewGroup parentT = parent;

        holder.VoteBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    PositionChecked = position;
                    optionToQuestionListener.SendResults(positionQuestion, position);
                    for(int index = 0; index < parentT.getChildCount(); index++){
                        if(index != position) {
                            View sibling = parentT.getChildAt(index);
                            CheckBox otherCheckbox = sibling.findViewById(R.id.answer_option_checkbox);
                            otherCheckbox.setChecked(false);
                        }
                    }
                }
            }
        });
        return view;
        }

static class ViewHolder{
    TextView TexteOption, PercentVote;
    ImageView OptionImg, OptionIcon;
    FrameLayout CheckZone;
    CheckBox VoteBtn;
    LinearLayout ResultZone;
    ProgressBar LineResult;
    ViewHolder(View view, int position, int positionChecked){
        TexteOption = view.findViewById(R.id.answer_option_text);
        PercentVote = view.findViewById(R.id.answer_option_percent);
        OptionImg = view.findViewById(R.id.answer_option_image);
        CheckZone = view.findViewById(R.id.answer_option_checkbox_zone);
        VoteBtn = view.findViewById(R.id.answer_option_checkbox);
        ResultZone = view.findViewById(R.id.answer_option_results);
        LineResult = view.findViewById(R.id.answer_option_bar);
        OptionIcon = view.findViewById(R.id.answer_option_icon);
        if(positionChecked == position){
            VoteBtn.setChecked(true);
        }else{
            VoteBtn.setChecked(false);
        }
    }
}
public interface OptionToQuestionListener{
    void SendResults(int positionQuestion, int positionOption);
    int getPositionChecked(int positionQuestion);
}
public interface AdaptListener{
    FirebaseUser getUser();
}
}