package com.sundown.maplists.views;

import android.content.Context;
import android.text.InputFilter;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sundown.maplists.R;
import com.sundown.maplists.models.Field;
import com.sundown.maplists.models.EntryField;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.sundown.maplists.extras.Constants.FIELDS.FIELD_CHECKED;
import static com.sundown.maplists.extras.Constants.FIELDS.FIELD_DATE;
import static com.sundown.maplists.extras.Constants.FIELDS.FIELD_DECIMAL;
import static com.sundown.maplists.extras.Constants.FIELDS.FIELD_NUMBER;
import static com.sundown.maplists.extras.Constants.FIELDS.FIELD_PIC;
import static com.sundown.maplists.extras.Constants.FIELDS.FIELD_TIME;
import static com.sundown.maplists.extras.Constants.FIELDS.FIELD_RATING;

/**
 * Created by Sundown on 7/6/2015.
 */
public class FieldView extends RelativeLayout implements View.OnClickListener, EntryField.Observer {


    public interface FieldViewListener {
        void editFieldTitle(int tag);
        void deleteField(int tag);
        void entryTyped(int tag, String entry);
    }

    private final static LinearLayout.LayoutParams layoutFillWidth = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    private final static LinearLayout.LayoutParams layoutWrapWidth = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    private final static SimpleDateFormat dateFormat = new SimpleDateFormat("MM'/'dd");
    private final static SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm:ss a");
    public static final String PHOTO = "P";
    public static final String EDIT = "E";
    public static final String SPINNER = "S";
    public static final String CHECKBOX = "C";
    public static final String RATING = "R";
    private static final int MAX_ENTRY_CHARS = 1000;
    private static FieldViewListener listener;


    private Context context;
    private TextView fieldTitle;
    public String getTitle(){ return String.valueOf(fieldTitle.getText());}


    private LinearLayout fieldEntryContainer;
    private ImageButton editFieldTitle, deleteFieldView;
    private int type;
    public int getType(){ return type;}


    public FieldView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        editFieldTitle = (ImageButton) findViewById(R.id.editFieldTitle);
        deleteFieldView = (ImageButton) findViewById(R.id.deleteFieldView);
        fieldTitle = (TextView) findViewById(R.id.fieldTitle);
        fieldEntryContainer = (LinearLayout) findViewById(R.id.fieldEntryContainer);

        deleteFieldView.setOnClickListener(this);
        editFieldTitle.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.editFieldTitle:
                listener.editFieldTitle((int) getTag());
                break;

            case R.id.deleteFieldView:
                listener.deleteField((int) getTag());
                break;
        }

    }

    public void init(Context context, FieldViewListener listener, Field field){
        this.context = context;
        this.listener = listener;
        this.type = field.type;
        updateTitle(field.title);
        addComponentView(field);

        if (field.permanent)
            setPermanentInterface();

    }

    public void addComponentView(Field field) {

        View view;

        switch (field.type) {
            case FIELD_PIC: {
                view = new RelativeLayout(context);
                view.setId(field.id);
                view.setTag(PHOTO); //NOTE: this works! retains tag even though we add fragment here
                disableTitleButtons(); //photofragment has its own buttons
                break;
            }

            case FIELD_RATING: {
                EntryField entry = (EntryField) field;
                final RatingBar bar = new RatingBar(context);
                bar.setNumStars(5);
                bar.setTag(RATING);
                if (entry.entry != null) {
                    try {
                        bar.setRating(Float.parseFloat(entry.entry));
                    } catch (Exception e) {}
                }
                bar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                    @Override
                    public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                        listener.entryTyped((int) getTag(), String.valueOf(bar.getRating()));
                    }
                });
                view = bar;
                break;
            }

            /*case FIELD_DROPDOWN:
                //todo
                break;*/

            case FIELD_CHECKED: {
                EntryField entry = (EntryField) field;
                CheckBox checkBox = new CheckBox(context);
                checkBox.setTag(CHECKBOX);
                if (entry.entry != null) {
                    try {
                        checkBox.setChecked(entry.entry.equals("1")?true:false);
                    } catch (Exception e) {}
                }
                view = checkBox;
                break;
            }

            default:{
                EntryField entry = (EntryField) field;
                EditText v = new EditText(context);
                v.setTag(EDIT);
                v.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_ENTRY_CHARS)});
                if (entry.entry != null){
                    v.setHint(entry.entry);
                }
                v.setOnFocusChangeListener(new OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (!hasFocus) {
                            EditText view = (EditText) v;
                            String text = view.getText().toString().trim();
                            if (text != null && text.length() > 0)
                                listener.entryTyped((int) getTag(), text);
                        }
                    }
                }); /*
                v.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        listener.entryTyped((int) getTag(), s.toString());
                        Log.m("afterTextChanged fired");
                    }
                });*/

                switch (entry.type) {
                    case FIELD_NUMBER:
                        v.setInputType(InputType.TYPE_CLASS_NUMBER);
                        break;
                    case FIELD_DECIMAL:
                        v.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                        break;
                    case FIELD_DATE:
                        v.setHint(dateFormat.format(new Date()));
                        break;
                    case FIELD_TIME:
                        v.setHint(timeFormat.format(new Date()));
                        break;
                }

                view = v;
                break;
            }

        }

        if (field.type == FIELD_RATING){
            view.setLayoutParams(layoutWrapWidth);
        } else {
            view.setLayoutParams(layoutFillWidth);
        }

        fieldEntryContainer.addView(view);

    }

    public View getChild(){
        return fieldEntryContainer.getChildAt(0);
    }

    private void disableTitleButtons(){
        disableDeleteButton();
        editFieldTitle.setOnClickListener(null);

        editFieldTitle.setVisibility(GONE);
        fieldTitle.setVisibility(GONE);
    }

    private void disableDeleteButton(){
        deleteFieldView.setOnClickListener(null);
        deleteFieldView.setVisibility(GONE);
    }

    private void setPermanentInterface(){
        disableDeleteButton();
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) editFieldTitle.getLayoutParams();
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        editFieldTitle.setLayoutParams(params);
    }

    @Override
    public void updateTitle(String title) {
        if (title != null)
            fieldTitle.setText(title);
    }

    public String getEntry(){
        View child = getChild();
        String tag = String.valueOf(child.getTag());

        switch(tag){
            case PHOTO:
                return null;

            case RATING:
                RatingBar b = (RatingBar)child;
                return String.valueOf(b.getRating());

            case EDIT:
                EditText e = (EditText)child;
                String text = e.getText().toString().trim();
                if (text == null || text.length() == 0) {
                    if (e.getHint() != null) {
                        text = e.getHint().toString().trim();
                    }
                }
                return text;


            /*case SPINNER: //todo
                    Spinner s = (Spinner) container.getChildAt(i);
                    model.fieldEntries.get(counter++).entry = s.getSelectedItem().toString().trim();
                break;*/

            case CHECKBOX:
                CheckBox box = (CheckBox)child;
                int x = box.isChecked() ? 1 : 0;
                return String.valueOf(x);

            default:
                return "";
        }
    }

}
