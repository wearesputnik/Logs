package mobi.kolibri.messager.object;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;
import mobi.kolibri.messager.R;


public class TextViewBold extends TextView {

    public TextViewBold(Context context) {
        this(context, null, 0);
    }

    public TextViewBold(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }


    public TextViewBold(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setFont(context);
    }

    private void setFont(Context context) {
        Typeface face = Typefaces.get(context, context.getText(R.string.font_helvetica_roman).toString());
        setTypeface(face);
    }
}
