/*
 * Copyright (C) 2017 Angad Singh
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.aurora.filepicker.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import com.aurora.adroid.R;

public class MaterialCheckbox extends View {

    private Context context;
    private int min;
    private Paint paint;
    private RectF bounds;
    private OnCheckedChangeListener onCheckedChangeListener;
    private Path checkedPath;
    private boolean checked;

    public MaterialCheckbox(Context context) {
        super(context);
        initView(context);
    }

    public MaterialCheckbox(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public MaterialCheckbox(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    public void initView(Context context) {
        this.context = context;
        checked = false;
        checkedPath = new Path();
        paint = new Paint();
        bounds = new RectF();

        OnClickListener onClickListener = v -> {
            setChecked(!checked);
            onCheckedChangeListener.onCheckedChanged(MaterialCheckbox.this, isChecked());
        };

        setOnClickListener(onClickListener);
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isChecked()) {
            paint.reset();
            paint.setAntiAlias(true);
            bounds.set(min / 10f, min / 10f, min - (min / 10f), min - (min / 10f));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                paint.setColor(getResources().getColor(R.color.colorAccent, context.getTheme()));
            } else {
                paint.setColor(getResources().getColor(R.color.colorAccent));
            }
            canvas.drawRoundRect(bounds, min / 8f, min / 8f, paint);

            paint.setColor(Color.WHITE);
            paint.setStrokeWidth(min / 10f);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.BEVEL);
            canvas.drawPath(checkedPath, paint);
        } else {
            paint.reset();
            paint.setAntiAlias(true);
            bounds.set(min / 10f, min / 10f, min - (min / 10f), min - (min / 10f));
            paint.setColor(Color.GRAY);
            canvas.drawRoundRect(bounds, min / 8f, min / 8f, paint);

            bounds.set(min / 5f, min / 5f, min - (min / 5f), min - (min / 5f));
            paint.setColor(Color.WHITE);
            canvas.drawRect(bounds, paint);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int height = getMeasuredHeight();
        int width = getMeasuredWidth();

        min = Math.min(width, height);
        bounds.set(min / 10f, min / 10f, min - (min / 10f), min - (min / 10f));
        checkedPath.moveTo(min / 4f, min / 2f);
        checkedPath.lineTo(min / 2.5f, min - (min / 3f));

        checkedPath.moveTo(min / 2.75f, min - (min / 3.25f));
        checkedPath.lineTo(min - (min / 4f), min / 3f);
        setMeasuredDimension(width, height);
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
        invalidate();
    }

    public void setOnCheckedChangedListener(OnCheckedChangeListener onCheckedChangeListener) {
        this.onCheckedChangeListener = onCheckedChangeListener;
    }
}
