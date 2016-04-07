/*
 * Copyright (C) 2013 Clover Network, Inc.
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
package com.test.all_examples.receipts;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import com.clover.sdk.v1.printer.ReceiptContract;

import java.io.FileNotFoundException;

public class TextReceiptRegistrationProvider extends ContentProvider {
  public static final String AUTHORITY = "com.clover.example.receipteditexample";
  public static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);

  public static final String CONTENT_DIRECTORY_TEXT = "text";
  public static final Uri CONTENT_URI_TEXT = Uri.withAppendedPath(AUTHORITY_URI, CONTENT_DIRECTORY_TEXT);

  private static final int TEXT = 0;

  private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

  private static String addOnText = "";

  static {
    uriMatcher.addURI(AUTHORITY, CONTENT_DIRECTORY_TEXT, TEXT);
  }

  public static void updateReceiptAddOnText(String text) {
    TextReceiptRegistrationProvider.addOnText = text;
  }

  @Override
  public boolean onCreate() {
    return true;
  }

  @Override
  public Cursor query(Uri uri, String[] strings, String s, String[] strings2, String s2) {
    switch (uriMatcher.match(uri)) {
      case TEXT:
        MatrixCursor cursor = new MatrixCursor(new String[]{ReceiptContract.Text._ID, ReceiptContract.Text.TEXT});

        cursor.addRow(new Object[]{Integer.valueOf(0), addOnText});
        return cursor;

      default:
        throw new IllegalArgumentException("unknown uri: " + uri);
    }
  }

  @Override
  public String getType(Uri uri) {
    switch (uriMatcher.match(uri)) {
      case TEXT:
        return ReceiptContract.Text.CONTENT_TYPE;
      default:
        throw new IllegalArgumentException("unknown URI " + uri);
    }
  }

  @Override
  public Uri insert(Uri uri, ContentValues contentValues) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int delete(Uri uri, String s, String[] strings) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
    throw new UnsupportedOperationException();
  }

  @Override
  public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
    return null;
  }
}