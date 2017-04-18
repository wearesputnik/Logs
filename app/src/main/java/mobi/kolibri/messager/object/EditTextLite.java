package mobi.kolibri.messager.object;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.EditText;

import mobi.kolibri.messager.R;

/**
 * Created by root on 04.02.16.
 */
public class EditTextLite extends EditText {

    public EditTextLite(Context context) {
        this(context, null, 0);
    }

    public EditTextLite(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }


    public EditTextLite(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setFont(context);
    }

    private void setFont(Context context) {
        Typeface face = Typefaces.get(context, context.getText(R.string.font_helvetica_lite).toString());
        setTypeface(face);
    }
}
