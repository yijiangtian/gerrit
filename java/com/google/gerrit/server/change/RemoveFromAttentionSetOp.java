// Copyright (C) 2020 The Android Open Source Project
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.gerrit.server.change;

import static java.util.Objects.requireNonNull;

import com.google.gerrit.entities.Account;
import com.google.gerrit.entities.AttentionSetUpdate;
import com.google.gerrit.entities.AttentionSetUpdate.Operation;
import com.google.gerrit.extensions.restapi.RestApiException;
import com.google.gerrit.server.notedb.ChangeUpdate;
import com.google.gerrit.server.update.BatchUpdateOp;
import com.google.gerrit.server.update.ChangeContext;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/** Remove a specified user from the attention set. */
public class RemoveFromAttentionSetOp implements BatchUpdateOp {

  public interface Factory {
    RemoveFromAttentionSetOp create(Account.Id attentionUserId, String reason);
  }

  private final Account.Id attentionUserId;
  private final String reason;

  /**
   * Remove a specified user from the attention set.
   *
   * @param attentionUserId the id of the user we want to add to the attention set.
   * @param reason The reason for adding that user.
   */
  @Inject
  RemoveFromAttentionSetOp(@Assisted Account.Id attentionUserId, @Assisted String reason) {
    this.attentionUserId = requireNonNull(attentionUserId, "user");
    this.reason = requireNonNull(reason, "reason");
  }

  @Override
  public boolean updateChange(ChangeContext ctx) throws RestApiException {
    ChangeUpdate update = ctx.getUpdate(ctx.getChange().currentPatchSetId());
    update.addToPlannedAttentionSetUpdates(
        AttentionSetUpdate.createForWrite(attentionUserId, Operation.REMOVE, reason));
    return true;
  }
}
