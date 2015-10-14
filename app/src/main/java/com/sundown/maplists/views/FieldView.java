package com.sundown.maplists.views;

import android.content.Context;
import android.text.Html;
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
import com.sundown.maplists.logging.Log;
import com.sundown.maplists.models.fields.EntryField;
import com.sundown.maplists.models.fields.Field;
import com.sundown.maplists.models.fields.FieldType;
import com.sundown.maplists.utils.ViewUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;


/**
 * Created by Sundown on 7/6/2015.
 */
public class FieldView extends RelativeLayout implements View.OnClickListener, EntryField.Observer {


    public interface FieldViewListener {
        void editFieldTitle(int tag);
        void deleteField(int tag);
        void selectColor(int tag);
    }

    private final static LinearLayout.LayoutParams layoutFillWidth = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    private final static LinearLayout.LayoutParams layoutWrapWidth = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    private final static SimpleDateFormat dateFormat = new SimpleDateFormat("MM'/'dd");
    private final static SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm:ss a");
    private static final String PHOTO = "P";
    private static final String EDIT = "E";
    private static final String SPINNER = "S";
    private static final String CHECKBOX = "C";
    private static final String RATING = "R";
    private static final String DOUBLE_VIEW = "D";
    private static final int MAX_ENTRY_CHARS = 1000;
    private FieldViewListener listener;


    private Context context;
    private TextView fieldTitle;
    public String getTitle(){ return String.valueOf(fieldTitle.getText());}


    private LinearLayout fieldEntryContainer;
    private ImageButton colorFieldView, editFieldTitle, deleteFieldView;
    private FieldType type;
    public FieldType getType(){ return type;}


    public FieldView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        editFieldTitle = (ImageButton) findViewById(R.id.editFieldTitle);
        deleteFieldView = (ImageButton) findViewById(R.id.deleteFieldView);
        colorFieldView = (ImageButton) findViewById(R.id.colorFieldView);
        fieldTitle = (TextView) findViewById(R.id.fieldTitle);
        fieldEntryContainer = (LinearLayout) findViewById(R.id.fieldEntryContainer);

        deleteFieldView.setOnClickListener(this);
        editFieldTitle.setOnClickListener(this);
        colorFieldView.setOnClickListener(this);
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

            case R.id.colorFieldView:
                listener.selectColor((int) getTag());
                break;
        }

    }

    public void init(Context context, FieldViewListener listener, Field field){
        this.context = context;
        this.listener = listener;
        this.type = field.getType();
        setTitle(field.getTitle());
        addComponentView(field);

        if (field.isPermanent())
            setPermanentInterface();

        if (field.getId() != 0) //only the first element may have the color picker button
            disableColorButton();

    }

    private void addComponentView(Field field) {
        switch (field.getType()) {
            case PHOTO:
                addPhotoFragmentView(field);
                break;
            case RATING:
                addRatingBar(field);
                break;
            case CHECKBOX:
                addCheckBox(field);
                break;
            case ITEM_LIST:
            case PRICE_LIST:{
                addDoubleEditTextViews(field);
                break;
            }
            default:
                addSingleEditTextViews(field);
                break;
        }
    }

    private void addPhotoFragmentView(Field field){
        View view = new RelativeLayout(context);
        view.setId(field.getId());
        view.setTag(PHOTO); //NOTE: this works! retains tag even though we add fragment here
        disableTitleButtons(); //photofragment has its own buttons
        view.setLayoutParams(layoutFillWidth);
        fieldEntryContainer.addView(view);
    }


    private void addRatingBar(Field field){
        EntryField entry = (EntryField) field;
        final RatingBar bar = new RatingBar(context);
        bar.setNumStars(5);
        bar.setTag(RATING);
        try {
            bar.setRating(Float.parseFloat(entry.getEntry(0)));
        } catch (Exception e) {
            Log.e(e);
        }
        bar.setLayoutParams(layoutWrapWidth);
        fieldEntryContainer.addView(bar);
    }


    private void addCheckBox(Field field){
        EntryField entry = (EntryField) field;
        CheckBox checkBox = new CheckBox(context);
        checkBox.setTag(CHECKBOX);
        try {
            checkBox.setChecked(entry.getEntry(0).equals("1"));
        } catch (Exception e) {Log.e(e);}

        checkBox.setLayoutParams(layoutFillWidth);
        fieldEntryContainer.addView(checkBox);
    }


    private void addSingleEditTextViews(Field field){
        EntryField entry = (EntryField) field;
        int size = entry.getNumEntries();

        for (int i = 0; i < size; ++i){
            EditText v = createEditText(entry.getType(), i);
            String hint = entry.getEntry(i);
            if (hint != null && hint.length() > 0)
                v.setHint(hint);
            v.setLayoutParams(layoutFillWidth);
            fieldEntryContainer.addView(v);
        }
    }

    private void addDoubleEditTextViews(Field field){
        EntryField entry = (EntryField) field;
        int size = entry.getNumEntries();

        for (int i = 0; i < size; ++i){
            if (i+1 < size) {
                DoubleEditView v = (DoubleEditView) inflate(getContext(), R.layout.double_edit_view, null);
                v.setTag(DOUBLE_VIEW);

                String hint = entry.getEntry(i);
                if (hint != null && hint.length() > 0) v.setHint(1, hint);
                hint = entry.getEntry(++i); //note the increment
                if (hint != null && hint.length() > 0) v.setHint(2, hint);
                fieldEntryContainer.addView(v);
            }
        }
    }

    private EditText createEditText(FieldType type, int iteration){
        EditText v = new EditText(context);
        v.setTag(EDIT);
        v.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_ENTRY_CHARS)});
        switch (type) {
            case NUMBER:
                v.setInputType(InputType.TYPE_CLASS_NUMBER);
                break;
            case DECIMAL:
                v.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                break;
            case DATE:
                v.setHint(dateFormat.format(new Date()));
                break;
            case TIME:
                v.setHint(timeFormat.format(new Date()));
                break;
            case DATE_TIME:
                if (iteration == 0){
                    v.setHint(dateFormat.format(new Date()));
                } else {
                    v.setHint(timeFormat.format(new Date()));
                }
                break;
            case PHONE:
                v.setInputType(InputType.TYPE_CLASS_PHONE);
                break;
        }
        return v;
    }

    private View getChild(int element){
        return fieldEntryContainer.getChildAt(element);
    }

    private void disableTitleButtons(){
        disableImpermanentButtons();
        editFieldTitle.setOnClickListener(null);

        editFieldTitle.setVisibility(GONE);
        fieldTitle.setVisibility(GONE);
    }

    private void disableImpermanentButtons(){
        deleteFieldView.setOnClickListener(null);
        deleteFieldView.setVisibility(GONE);
    }

    private void disableColorButton(){
        colorFieldView.setOnClickListener(null);
        colorFieldView.setVisibility(GONE);
    }

    private void setPermanentInterface(){
        disableImpermanentButtons();
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) editFieldTitle.getLayoutParams();
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        editFieldTitle.setLayoutParams(params);
    }

    @Override
    public void setTitle(String title) {
        if (title != null)
            fieldTitle.setText(Html.fromHtml(title));
    }

    public List<String> getEntryFromChildView(int element){
        View child = getChild(element);
        String tag = String.valueOf(child.getTag());
        List<String> list = new LinkedList();

        switch(tag){
            case PHOTO:
                return null;

            case RATING: {
                list.add(ViewUtils.getStringFromRatingBar((RatingBar) child));
                break;
            }

            case EDIT: {
                list.add(ViewUtils.getStringFromEditText((EditText) child));
                break;
            }

            case CHECKBOX: {
                list.add(ViewUtils.getStringFromCheckBox((CheckBox) child));
                break;
            }

            case DOUBLE_VIEW: {
                ViewUtils.getStringsFromDoubleEditView(list, (DoubleEditView) child);
                break;
            }
        }
        return list;
    }

}
