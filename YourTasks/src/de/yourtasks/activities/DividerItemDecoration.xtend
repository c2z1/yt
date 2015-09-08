package de.yourtasks.activities

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.ItemDecoration
import de.yourtasks.R

class DividerItemDecoration extends ItemDecoration {
	private Drawable mDivider;

    public new(Context context) {
        mDivider = context.getResources().getDrawable(R.drawable.line_divider);
    }

   override onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        val left = parent.getPaddingLeft();
        val right = parent.getWidth() - parent.getPaddingRight();

        val childCount = parent.getChildCount();
        if (childCount > 1) {
	        for (var i = 1; i < childCount-1; i++) {
	            val child = parent.getChildAt(i);
	
	            val params = child.getLayoutParams() as RecyclerView.LayoutParams;
	
	            val top = child.getBottom() + params.bottomMargin;
	            val bottom = top + mDivider.getIntrinsicHeight();
	
	            mDivider.setBounds(left, top, right, bottom);
	            mDivider.draw(c);
            }
        }
    }
}