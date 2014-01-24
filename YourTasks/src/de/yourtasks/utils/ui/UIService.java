package de.yourtasks.utils.ui;

import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.widget.EditText;

import com.google.api.client.json.GenericJson;

public class UIService {

	public static void bind(EditText t, final GenericJson obj, final String fieldName) {
		Object val = obj.get(fieldName);
		final int type = t.getInputType();
		t.setText(val == null ? "" : "" + obj.get(fieldName));
		t.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			
			@Override
			public void afterTextChanged(Editable s) {
				obj.set(fieldName, convert(s.toString(), type));
			}

		});
	}
	
	private static Object convert(String string, int type) {
		if (string == null || "".equals(string)) return null;
		if ((type & InputType.TYPE_CLASS_NUMBER) != 0) {
			return Integer.parseInt(string);
		}
		return string;
	}

}
