package com.clover.cfp.examples.objects;

import android.os.Parcel;
import android.os.Parcelable;
import java.io.Serializable;

/**
 * Created by glennbedwell on 5/4/17.
 */
public class Rating implements Parcelable, Serializable {
  public String id;
  public String question;
  public int value;

  protected Rating(Parcel in) {
    id = (String)in.readValue(null);
    question = (String)in.readValue(null);
    value = Integer.valueOf((String)in.readValue(null));
  }

  public static final Creator<Rating> CREATOR = new Creator<Rating>() {
    @Override
    public Rating createFromParcel(Parcel in) {
      return new Rating(in);
    }

    @Override
    public Rating[] newArray(int size) {
      return new Rating[size];
    }
  };

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(id);
    dest.writeString(question);
    dest.writeInt(value);
  }
}