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

package com.clover.example.adapter;

import com.clover.example.R;
import com.clover.example.model.POSCard;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class CardsListViewAdapter extends ArrayAdapter<POSCard>{

    public CardsListViewAdapter(Context context, int resource) {
        super(context, resource);
    }

    public CardsListViewAdapter(Context context, int resource, List<POSCard> items) {
        super(context, resource, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.cards_row, null);
        }

        POSCard posCard = getItem(position);

        if (posCard != null) {
            TextView nameColumn = (TextView) v.findViewById(R.id.CardsNameColumn);
            TextView first6Column = (TextView) v.findViewById(R.id.CardsFirst6Column);
            TextView last4Column = (TextView) v.findViewById(R.id.CardsLast4Column);
            TextView expColumn = (TextView) v.findViewById(R.id.CardsExpColumn);


            nameColumn.setText(posCard.getVaultedName());
            first6Column.setText(posCard.getFirst6());
            last4Column.setText(posCard.getLast4());
            expColumn.setText(posCard.getMonth() + "/" + posCard.getYear());
        }

        return v;
    }
}
