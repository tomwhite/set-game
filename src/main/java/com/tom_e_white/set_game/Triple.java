/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tom_e_white.set_game;

/**
 * A set of three {@link Card}s, not necessarily a Set.
 */
public class Triple {
  
  private final Card first, second, third;
  
  public Triple(Card first, Card second, Card third) {
    this.first = first;
    this.second = second;
    this.third = third;
  }
  
  public Card first() { return first; }
  public Card second() { return second; }
  public Card third() { return third; }
  
  public boolean contains(Card card) {
    return card == first || card == second || card == third;
  }
  
  /**
   * @return true if the three cards comprise a Set, false otherwise.
   */
  public boolean isASet() {
    return Cards.isASet(this);
  }

  public boolean equals(Triple that) {
    return this.first == that.first && this.second == that.second && this.third == that.third;
  }
  
  public String toString() {
    return String.format("{%s, %s, %s}", first, second, third);
  }

}
