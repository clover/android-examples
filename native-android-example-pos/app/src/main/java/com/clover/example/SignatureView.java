/*
 * Copyright (C) 2018 Clover Network, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.clover.example;

import com.clover.sdk.v3.base.Point;
import com.clover.sdk.v3.base.Points;
import com.clover.sdk.v3.base.Signature;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.List;

public class SignatureView extends View {

  Signature signature;

  public SignatureView(Context ctx, AttributeSet attrSet) {
    super(ctx, attrSet);
  }

  public SignatureView(Context ctx) {
    super(ctx);
  }

  public void onDraw(Canvas canvas) {
    Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    linePaint.setColor(Color.BLACK);
    linePaint.setStrokeWidth(1.5f);
    if (signature != null && signature.getStrokes() != null) {
      List<Points> strokes = signature.getStrokes();
      for ( Points stroke : strokes) {
        for (int i = 0; i < stroke.getPoints().size() - 1; i++) {
          Point pt1 = stroke.getPoints().get(i);
          Point pt2 = stroke.getPoints().get(i + 1);
          canvas.drawLine(pt1.getX(), pt1.getY(), pt2.getX(), pt2.getY(), linePaint);
        }
      }
    }
  }

  public Signature getSignature() {
    return signature;
  }

  public void setSignature(Signature signature) {
    this.signature = signature;

    post(new Runnable(){
      @Override public void run() {
        invalidate();
      }
    });
  }
}
