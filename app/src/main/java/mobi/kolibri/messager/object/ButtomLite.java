package mobi.kolibri.messager.object;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.Button;
import mobi.kolibri.messager.R;

public class ButtomLite extends Button{
    public ButtomLite(Context context) {
        this(context, null, 0);
    }

    public ButtomLite(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }


    public ButtomLite(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setFont(context);
    }

    private void setFont(Context context) {
        Typeface face = Typefaces.get(context, context.getText(R.string.font_helvetica_lite).toString());
        setTypeface(face);
    }
}
