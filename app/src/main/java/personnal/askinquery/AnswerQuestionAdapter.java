package personnal.askinquery;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Path;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Henrick on 2018-09-21.
 */

public class AnswerQuestionAdapter extends ArrayAdapter<Question> implements AnswerOptionAdapter.OptionToQuestionListener{

    private ArrayList<Integer> questionReponse;
    private boolean Si_Resultats;
    Context c;
    public AnswerQuestionAdapter(Context context, ArrayList<Question> questions, boolean b,@Nullable HashMap<String, Integer> save){
        super(context, 0, questions);
        c = context;
        Si_Resultats = b;
        questionReponse = new ArrayList<>();
        if(save != null) {
            for (int i = 0; i < questions.size(); i++) {
                questionReponse.add(save.get(String.valueOf(i)));
            }
        }else{
            for (int i = 0; i < questions.size(); i++) {
                questionReponse.add(-1);
            }
        }

    }

    public View getView(int position, View convertView, ViewGroup parent){
        final Question question = getItem(position);
        View view = convertView;
        final AnswerQuestionAdapter.ViewHolder holder;
        final ArrayList<Option> liste_options = question.Options;
        final AnswerOptionAdapter answerOptionAdapter = new AnswerOptionAdapter(c, liste_options, question.Type_Question, this, position, Si_Resultats);

        if(view == null){
            view = LayoutInflater.from(getContext()).inflate(R.layout.answer_question_element, parent, false);
            holder = new AnswerQuestionAdapter.ViewHolder(view, position);

            view.setTag(holder);
            //holder.TexteReponse.setTag(OptionData.get(POSITION).ID);
        }else{
            holder = (AnswerQuestionAdapter.ViewHolder)view.getTag();
        }
        holder.ListeOption.setAdapter(answerOptionAdapter);
        Utils.setListViewHeightBasedOnItems(holder.ListeOption);
        holder.TexteQuestion.setText(c.getString(R.string.Poll_Answ_Question_Title, position+1, question.Texte_Question));

        if(question.Type_Question != Question.TYPE_TEXTE){
        for(int i = 0; i < liste_options.size(); i++){
            final int index = i;
            StorageReference ImageRef = FirebaseStorage.getInstance().getReference();
            if(question.Type_Question == Question.TYPE_IMAGE){
                ImageRef = ImageRef.child(FireBaseInteraction.Storage_Paths.OPTIONS_IMAGES_THUMBNAILS).child(liste_options.get(i).ID+".jpg");

            }else{
                ImageRef = ImageRef.child(FireBaseInteraction.Storage_Paths.OPTIONS_VIDEOS_THUMBNAILS).child(liste_options.get(i).ID+".jpg");
            }
            ImageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0, bytes.length);
                    liste_options.get(index).ImagePreload = bitmap;
                    answerOptionAdapter.notifyDataSetChanged();
                    Utils.setListViewHeightBasedOnItems(holder.ListeOption);
                }
            });
        }
        }
        if(question.Type_Question == Question.TYPE_VIDEO){
            holder.TexteInstruct.setText(R.string.Post_Elem_Instruct_Vid);
            holder.TexteInstruct.setVisibility(View.VISIBLE);
        }
        if(question.Type_Question == Question.TYPE_IMAGE){
            holder.TexteInstruct.setText(R.string.Post_Elem_Instruct_Img);
            holder.TexteInstruct.setVisibility(View.VISIBLE);
        }

        return view;
    }

    static class ViewHolder{
        TextView TexteQuestion, TexteInstruct;
        ListView ListeOption;
        public ViewHolder(View view, int position){
            TexteInstruct = view.findViewById(R.id.answer_question_instruct);
            TexteQuestion = view.findViewById(R.id.answer_question_texte);
            ListeOption = view.findViewById(R.id.answer_question_options);
        }
    }
    public void SendResults(int positionQuestion, int positionOption){
                questionReponse.set(positionQuestion, positionOption);
    }
    public int getPositionChecked(int positionQuestion){
        return questionReponse.get(positionQuestion);
    }
    public ArrayList<Integer> getQuestionReponse(){
        return questionReponse;
    }

    public interface AdaptListener{
    }

}