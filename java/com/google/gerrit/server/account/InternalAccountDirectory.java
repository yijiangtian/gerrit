// Copyright (C) 2013 The Android Open Source Project
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

package com.google.gerrit.server.account;

import static java.util.stream.Collectors.toList;

import com.google.common.base.Strings;
import com.google.gerrit.extensions.common.AccountInfo;
import com.google.gerrit.extensions.common.AvatarInfo;
import com.google.gerrit.extensions.registration.DynamicItem;
import com.google.gerrit.reviewdb.client.Account;
import com.google.gerrit.server.IdentifiedUser;
import com.google.gerrit.server.account.externalids.ExternalId;
import com.google.gerrit.server.avatar.AvatarProvider;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Singleton
public class InternalAccountDirectory extends AccountDirectory {
  static final Set<FillOptions> ID_ONLY = Collections.unmodifiableSet(EnumSet.of(FillOptions.ID));

  public static class Module extends AbstractModule {
    @Override
    protected void configure() {
      bind(AccountDirectory.class).to(InternalAccountDirectory.class);
    }
  }

  private final AccountCache accountCache;
  private final DynamicItem<AvatarProvider> avatar;
  private final IdentifiedUser.GenericFactory userFactory;

  @Inject
  InternalAccountDirectory(
      AccountCache accountCache,
      DynamicItem<AvatarProvider> avatar,
      IdentifiedUser.GenericFactory userFactory) {
    this.accountCache = accountCache;
    this.avatar = avatar;
    this.userFactory = userFactory;
  }

  @Override
  public void fillAccountInfo(Iterable<? extends AccountInfo> in, Set<FillOptions> options)
      throws DirectoryException {
    if (options.equals(ID_ONLY)) {
      return;
    }
    for (AccountInfo info : in) {
      Account.Id id = new Account.Id(info._accountId);
      Optional<AccountState> state = accountCache.get(id);
      if (state.isPresent()) {
        fill(info, state.get(), options);
      } else {
        info._accountId = options.contains(FillOptions.ID) ? id.get() : null;
      }
    }
  }

  private void fill(AccountInfo info, AccountState accountState, Set<FillOptions> options) {
    Account account = accountState.getAccount();
    if (options.contains(FillOptions.ID)) {
      info._accountId = account.getId().get();
    } else {
      // Was previously set to look up account for filling.
      info._accountId = null;
    }
    if (options.contains(FillOptions.NAME)) {
      info.name = Strings.emptyToNull(account.getFullName());
      if (info.name == null) {
        info.name = accountState.getUserName().orElse(null);
      }
    }
    if (options.contains(FillOptions.EMAIL)) {
      info.email = account.getPreferredEmail();
    }
    if (options.contains(FillOptions.SECONDARY_EMAILS)) {
      info.secondaryEmails = getSecondaryEmails(account, accountState.getExternalIds());
    }
    if (options.contains(FillOptions.USERNAME)) {
      info.username = accountState.getUserName().orElse(null);
    }

    if (options.contains(FillOptions.STATUS)) {
      info.status = account.getStatus();
    }

    if (options.contains(FillOptions.AVATARS)) {
      AvatarProvider ap = avatar.get();
      if (ap != null) {
        info.avatars = new ArrayList<>(3);
        IdentifiedUser user = userFactory.create(account.getId());

        // GWT UI uses DEFAULT_SIZE (26px).
        addAvatar(ap, info, user, AvatarInfo.DEFAULT_SIZE);

        // PolyGerrit UI prefers 32px and 100px.
        if (!info.avatars.isEmpty()) {
          if (32 != AvatarInfo.DEFAULT_SIZE) {
            addAvatar(ap, info, user, 32);
          }
          if (100 != AvatarInfo.DEFAULT_SIZE) {
            addAvatar(ap, info, user, 100);
          }
        }
      }
    }
  }

  public List<String> getSecondaryEmails(Account account, Collection<ExternalId> externalIds) {
    return ExternalId.getEmails(externalIds)
        .filter(e -> !e.equals(account.getPreferredEmail()))
        .sorted()
        .collect(toList());
  }

  private static void addAvatar(
      AvatarProvider provider, AccountInfo account, IdentifiedUser user, int size) {
    String url = provider.getUrl(user, size);
    if (url != null) {
      AvatarInfo avatar = new AvatarInfo();
      avatar.url = url;
      avatar.height = size;
      account.avatars.add(avatar);
    }
  }
}
