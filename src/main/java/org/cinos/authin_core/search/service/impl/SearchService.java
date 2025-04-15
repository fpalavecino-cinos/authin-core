package org.cinos.authin_core.search.service.impl;

import lombok.RequiredArgsConstructor;
import org.cinos.authin_core.search.dto.SearchResultDTO;
import org.cinos.authin_core.search.service.ISearchService;
import org.cinos.authin_core.users.entity.AccountEntity;
import org.cinos.authin_core.users.service.impl.AccountService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchService implements ISearchService {

    private final AccountService accountService;

    @Override
    public List<SearchResultDTO> search(String query) {
        List<SearchResultDTO> results = new ArrayList<>();

        // Buscar usuarios
        List<AccountEntity> users = accountService.findByUsernameContainingIgnoreCase(query);
        users.forEach(user -> results.add(new SearchResultDTO(user.getId(), user.getUser().getUsername(), user.getAvatarImg(), "user")));

        return results;
    }
}
