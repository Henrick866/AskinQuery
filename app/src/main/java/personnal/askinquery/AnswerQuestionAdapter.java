package personnal.askinquery;


import android.content.Context;
import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Henrick on 2018-09-21.
 */

public class AnswerQuestionAdapter extends ArrayAdapter<Question> implements AnswerOptionAdapter.OptionToQuestionListener{

    private ArrayList<Integer> questionReponse;
    private boolean Si_Resultats;
    private int[] LoadedCompteur;
    private Context c;
    AnswerQuestionAdapter(Context context, ArrayList<Question> questions, boolean b,@Nullable HashMap<String, Integer> save){
        super(context, 0, questions);
        c = context;
        Si_Resultats = b;
        questionReponse = new ArrayList<>();
        LoadedCompteur = new int[questions.size()];
        if(save != null) {
            for (int i = 0; i < questions.size(); i++) {
                questionReponse.add(save.get(String.valueOf(i)));
                LoadedCompteur[i] = 0;
            }
        }else{
            for (int i = 0; i < questions.size(); i++) {
                questionReponse.add(-1);
                LoadedCompteur[i] = 0;
            }
        }

    }

    public View getView(final int position, View convertView, ViewGroup parent){
        final Question question = getItem(position);
        View view = convertView;
        final AnswerQuestionAdapter.ViewHolder holder;

        final AnswerOptionAdapter answerOptionAdapter = new AnswerOptionAdapter(c, question.Options, question.Type_Question, this, position, Si_Resultats);

        if(view == null){
            view = LayoutInflater.from(getContext()).inflate(R.layout.answer_question_element, parent, false);
            holder = new AnswerQuestionAdapter.ViewHolder(view, position);

            view.setTag(holder);
        }else{
            holder = (AnswerQuestionAdapter.ViewHolder)view.getTag();
        }
        holder.TexteQuestion.setText(c.getString(R.string.Poll_Answ_Question_Title, position+1, question.Texte_Question));

        if(question.Type_Question != Question.TYPE_TEXTE){
        for(int i = 0; i < question.Options.size(); i++){
            final int index = i;
            if(question.Options.get(index).ImagePreload == null) {//ne charge que quand c'est vide;
                StorageReference ImageRef = FirebaseStorage.getInstance().getReference();
                if (question.Type_Question == Question.TYPE_IMAGE) {
                    ImageRef = ImageRef.child(FireBaseInteraction.Storage_Paths.OPTIONS_IMAGES_THUMBNAILS).child(question.Options.get(i).ID + ".jpg");

                } else {
                    ImageRef = ImageRef.child(FireBaseInteraction.Storage_Paths.OPTIONS_VIDEOS_THUMBNAILS).child(question.Options.get(i).ID + ".jpg");
                }
                ImageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        question.Options.get(index).ImagePreload = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        tasksDone(question.Options.size(), position, answerOptionAdapter, holder);
                    }
                });
            }
        }
        }else{
            holder.Loading.setVisibility(View.GONE);
            holder.ListeOption.setAdapter(answerOptionAdapter);
            Utils.setListViewHeightBasedOnItems(holder.ListeOption);
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
    private synchronized void tasksDone(int TailleListeOptions, int position, AnswerOptionAdapter answerOptionAdapter, ViewHolder viewHolder){
        LoadedCompteur[position]++;
        if(LoadedCompteur[position] == TailleListeOptions){
            answerOptionAdapter.notifyDataSetChanged();
            viewHolder.ListeOption.setAdapter(answerOptionAdapter);
            Utils.setListViewHeightBasedOnItems(viewHolder.ListeOption);
            viewHolder.Loading.setVisibility(View.GONE);
        }
    }
    static class ViewHolder{
        TextView TexteQuestion, TexteInstruct;
        ListView ListeOption;
        ProgressBar Loading;
        ViewHolder(View view, int position){
            TexteInstruct = view.findViewById(R.id.answer_question_instruct);
            TexteQuestion = view.findViewById(R.id.answer_question_texte);
            ListeOption = view.findViewById(R.id.answer_question_options);
            Loading = view.findViewById(R.id.answer_question_progress);
        }
    }
    public void SendResults(int positionQuestion, int positionOption){
                questionReponse.set(positionQuestion, positionOption);
    }
    public int getPositionChecked(int positionQuestion){
        return questionReponse.get(positionQuestion);
    }
    ArrayList<Integer> getQuestionReponse(){
        return questionReponse;
    }

}