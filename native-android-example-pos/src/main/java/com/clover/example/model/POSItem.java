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

package com.clover.example.model;

import java.io.Serializable;

public class POSItem implements Serializable{
  private boolean tippable;
  private boolean taxable;
  private String id;
  private long price;
  private String name;



  public POSItem(String id, String name, long price, boolean taxable, boolean tippable) {
    this.id = id;
    this.name = name;
    this.price = price;
    this.taxable = taxable;
    this.tippable = tippable; //
  }

  public String getName() {
    return name;
  }

  public long getPrice() {
    return price;
  }

  public boolean isTippable() {
    return tippable;
  }

  public boolean isTaxable() {
    return taxable;
  }

  public String getId() {
    return id;
  }
}
