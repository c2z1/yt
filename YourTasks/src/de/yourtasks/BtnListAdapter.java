package de.yourtasks;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

public abstract class BtnListAdapter<T> extends ArrayAdapter<T> {
	public BtnListAdapter(Context context, List<T> objects) {
		super(context, R.layout.btn_list_item, objects);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) getContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View rowView = convertView == null 
				? inflater.inflate(R.layout.btn_list_item, parent, false) : convertView;
		TextView textView = (TextView) rowView.findViewById(R.id.textView1);
		Button btn = (Button) rowView.findViewById(R.id.button1);
		final T item = getItem(position);
		textView.setText(item.toString());
		
		textView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				itemClicked(rowView, v, item);
			}
		});

		btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				itemBtnClicked(rowView, v, item);
			}
		});

		return rowView;
	}
	
	public abstract void itemBtnClicked(View rowView, View btn, T item);
	public abstract void itemClicked(View rowView, View textView, T item);
}