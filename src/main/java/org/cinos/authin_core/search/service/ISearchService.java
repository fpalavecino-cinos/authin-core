package org.cinos.authin_core.search.service;

import org.cinos.authin_core.search.dto.SearchResultDTO;

import java.util.List;

public interface ISearchService {
    List<SearchResultDTO> search(String query);
}
