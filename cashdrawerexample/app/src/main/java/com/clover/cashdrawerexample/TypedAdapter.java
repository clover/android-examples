package com.clover.cashdrawerexample;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class TypedAdapter<T, H extends TypedAdapter.ViewHolder> extends BaseAdapter {

  private final int itemLayoutId;
  private final List<T> data = new ArrayList<T>();

  public TypedAdapter(final int itemLayoutId) {
    this.itemLayoutId = itemLayoutId;
  }

  public void addData(T d) {
    data.add(d);
    notifyDataSetChanged();
  }

  public void addData(Collection<T> ds) {
    data.addAll(ds);
    notifyDataSetChanged();
  }

  public List<T> getData() {
    return new ArrayList<T>(data);
  }

  public void clearData() {
    data.clear();
    notifyDataSetChanged();
  }

  public void setData(Collection<T> data) {
    this.data.clear();
    addData(data);
  }

  @Override
  public int getCount() {
    return data.size();
  }

  @Override
  public T getItem(final int position) {
    return data.get(position);
  }

  @Override
  public long getItemId(final int position) {
    return position;
  }

  @Override
  public View getView(final int position, final View convertView, final ViewGroup parent) {
    final H holder = obtainHolder(convertView, parent);
    bind(holder, getItem(position), position);
    return holder.view;
  }

  protected abstract void bind(final H holder, final T item, final int position);

  private H obtainHolder(final View convertView, final ViewGroup parent) {
    if (convertView == null) {
      final View view = LayoutInflater.from(parent.getContext()).inflate(itemLayoutId, parent, false);
      return createHolder(view);
    } else {
      return (H) convertView.getTag();
    }
  }

  protected abstract H createHolder(final View view);

  public static class ViewHolder {
    public final View view;

    public ViewHolder(final View view) {
      this.view = view;
      view.setTag(this);
    }
  }
}
